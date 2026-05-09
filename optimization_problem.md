# Concurrency Optimization: SQLite → PostgreSQL + HikariCP

## The Problem

When multiple users book tickets simultaneously, the application stalls. The root cause is **SQLite's file-level write lock** — the entire database file is locked for every write operation, forcing all concurrent requests to queue behind each other. This is a fundamental SQLite limitation, not a code one.

Switching to an ORM would not solve this — Hibernate or any other ORM still talks to the same underlying driver and inherits the same locking behaviour.

---

## The Solution

Two targeted changes address the actual bottleneck:

1. **PostgreSQL** — a production-grade database with row-level locking, meaning concurrent writes to different bookings no longer block each other.
2. **HikariCP** — a connection pool that maintains a ready set of database connections, eliminating the overhead of opening a new connection per request and preventing threads from starving each other.

---

## Implementation

### 1. Dependencies (`build.gradle.kts`)

```kotlin
implementation("org.postgresql:postgresql:42.7.3")
implementation("com.zaxxer:HikariCP:5.1.0")
```

### 2. Connection Pool Setup

Replace the current `DatabaseConfig.java` with a HikariCP-managed data source:

```java
public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/train_ticketing");
        config.setUsername("postgres");
        config.setPassword("secret");
        config.setMaximumPoolSize(10);        // max concurrent connections
        config.setMinimumIdle(2);             // connections kept warm
        config.setConnectionTimeout(3000);    // ms before throwing if no connection available
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();  // borrowed from pool, auto-returned on close()
    }
}
```

### 3. Transactional Booking

Wrap the booking insert in an explicit transaction so that the seat-count check and the insert are atomic — no two threads can both see available seats and both succeed if only one seat remains:

```java
public Optional<Booking> add(Booking booking) {
    try (Connection conn = DatabaseConfig.getConnection()) {
        conn.setAutoCommit(false);
        try {
            int bookedSeats = getBookedSeatsForTrip(conn, booking.getTrip().getId());
            int capacity    = booking.getTrip().getTrain().getCapacity();

            if (bookedSeats + booking.getSeatCount() > capacity) {
                throw new ValidationException("Not enough available seats");
            }

            insertBooking(conn, booking);
            conn.commit();
            return Optional.of(booking);

        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }
}
```

---

## What Each Change Buys You

| Change | Problem it solves |
|---|---|
| PostgreSQL | Removes file-level locking; concurrent writes to different rows no longer block |
| HikariCP pool | Eliminates connection-per-request overhead; threads get a connection instantly |
| Explicit transaction | Prevents race conditions in the seat availability check |

---

## What This Is Not

This is not an ORM migration. The repository layer stays as plain JDBC — the only change is *who manages connections* (HikariCP) and *where the data lives* (PostgreSQL). An ORM could be introduced later as a maintainability improvement, but it would not have addressed the concurrency problem on its own.