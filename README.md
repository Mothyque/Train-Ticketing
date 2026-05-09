
# Train Ticketing System

A console-based Java application for managing train bookings, routes, trips, and delay notifications. Built with a layered architecture (domain → repository → service → UI) and backed by a SQLite database.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Building and Running](#building-and-running)
- [Database](#database)
- [Features and Usage](#features-and-usage)
    - [1. Book a Ticket](#1-book-a-ticket)
    - [2. Find Departure / Arrival Times](#2-find-departure--arrival-times)
    - [3. Admin Panel](#3-admin-panel)
        - [3.1 Manage Trains](#31-manage-trains)
        - [3.2 Manage Stations](#32-manage-stations)
        - [3.3 Manage Routes](#33-manage-routes)
        - [3.4 Manage Trips](#34-manage-trips)
        - [3.5 View Bookings](#35-view-bookings)
        - [3.6 Handle Delays](#36-handle-delays)
- [Design Patterns](#design-patterns)
- [Project Structure](#project-structure)

---

## Overview

The Train Ticketing System lets passengers search for routes and book seats on scheduled train trips. A protected admin panel gives operators full control over the train fleet, station list, route definitions, trip schedules, and real-time delay management with automatic email notifications to affected passengers.

---

## Architecture

```
┌──────────────────────────────────────┐
│              UI (Console)            │
├──────────────────────────────────────┤
│   Service Layer (business logic)     │
│  AdminService  TripService           │
│  BookingService  TrainService ...    │
├──────────────────────────────────────┤
│   Repository Layer (data access)     │
│  DBRepository  TrainDBRepository ... │
├──────────────────────────────────────┤
│   Domain (entities + validators)     │
│  Train  Station  Route  Trip         │
│  Booking  + validators               │
├──────────────────────────────────────┤
│   Utils                              │
│  DatabaseConfig  EmailService        │
│  Observer / Observable               │
├──────────────────────────────────────┤
│         SQLite Database              │
└──────────────────────────────────────┘
```

**Key design decisions:**
- Each entity has a dedicated repository that extends the generic `DBRepository<ID, Entity>`.
- Services contain all business logic and call into repositories; the UI only calls services.
- `EmailService` is a singleton; delay notifications are sent directly to the console (simulated SMTP).
- The Observer / Observable utilities are included for extensibility.

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java JDK    | 17 or later |
| Gradle      | 9.x (wrapper included) |

No external database installation is required — SQLite is bundled via the `org.xerial:sqlite-jdbc` dependency.

---

## Building and Running

**Clone / extract the project, then from the project root:**

```bash
# Build
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows

# Run
./gradlew run            # Linux / macOS
gradlew.bat run          # Windows
```

The application reads and writes `train_ticketing.db` in the project root. The database is initialised automatically on first run using `sql/schema.sql`, which also seeds three stations, two trains, one route, and one sample trip.

---

## Database

Schema file: `sql/schema.sql`

| Table | Description |
|---|---|
| `trains` | Train fleet (id, capacity) |
| `stations` | Stations (id, name, location) |
| `routes` | Route definitions (id) |
| `route_stations` | Ordered stops per route (route_id, station_id, stop_order) |
| `trips` | Scheduled trips (train, route, departure, arrival, delay) |
| `bookings` | Passenger bookings (trip, email, seat count) |

Foreign keys are enforced with `CASCADE` deletes where applicable.

**Seed data loaded on first run:**

| Entity | Data |
|---|---|
| Stations | Bucuresti Nord, Brasov, Cluj-Napoca |
| Trains | Train 1 (120 seats), Train 2 (200 seats) |
| Route 1 | Bucuresti Nord → Brasov → Cluj-Napoca |
| Trip 1 | Train 1 on Route 1, departs 06:00, arrives 14:30 |

---

## Features and Usage

On launch, the application displays the main menu:

```
[TRAIN TICKETING APPLICATION]
[Welcome to Train Booking System]

========== MAIN MENU ==========
1. Book a ticket
2. Find departure/arrival times
3. Admin Panel
4. Exit
Choose an option:
```

---

### 1. Book a Ticket

Allows a passenger to select an available trip and reserve seats.

**Flow:**
1. All available trips are listed with their ID, train, departure/arrival times, and number of route stops.
2. The user selects a trip number, enters their email address, and specifies how many seats they need.
3. On success, a booking ID is returned and a confirmation email is printed to the console.

**Example input:**

```
========== BOOK TICKET ==========

--- Available Trips ---
1. Trip ID: 1 | Train: 1 | Departure: 06:00 | Arrival: 14:30 | Route stations: 3

Select trip number: 1
Enter email: alice@example.com
Enter number of seats: 2
```

**Example output (success):**

```
Booking successful!
Booking ID: 1
Confirmation email sent to: alice@example.com
```

**Example output (validation failure — not enough seats):**

```
Booking failed: Not enough available seats on this train
```

**Example output (invalid input):**

```
Invalid input. Please enter valid numbers.
```

---

### 2. Find Departure / Arrival Times

Searches for all routes (direct and one-transfer) between two chosen stations and displays timing and seat availability for each leg.

**Flow:**
1. All stations are listed.
2. The user selects a departure station number and an arrival station number.
3. The system searches for direct trips and trips with one changeover station where the connecting trip departs after the first leg arrives.
4. Each result shows individual trip legs with departure time, arrival time, current delay, and available seats.

**Example input:**

```
========== FIND ROUTE ==========

--- Available Stations ---
1. Bucuresti Nord (Bucuresti)
2. Brasov (Brasov)
3. Cluj-Napoca (Cluj)

Select departure station number: 1
Select arrival station number: 3
```

**Example output:**

```
--- Available Routes ---

Route 1:
  Leg 1: Train 1 | Departure: 06:00 | Arrival: 14:30 | Delay: 0 min
    Available seats: 118/120
Total journey time: 06:00 to 14:30
```

**Example output (no routes found):**

```
No routes found between Brasov and Bucuresti Nord
```

**Example output (same station selected for both):**

```
Departure and arrival stations must be different
```

---

### 3. Admin Panel

Accessed from the main menu with option **3**. Requires a password.

**Default password:** `admin123`

```
Enter admin password: admin123

[ADMIN PANEL]

========== ADMIN MENU ==========
1. Manage Trains
2. Manage Stations
3. Manage Routes
4. Manage Trips
5. View Bookings
6. Handle Delays
7. Exit Admin Panel
Choose an option:
```

**Wrong password:**

```
Incorrect password
```

---

#### 3.1 Manage Trains

```
========== MANAGE TRAINS ==========
1. Add train
2. Remove train
3. Modify train
4. View all trains
Choose an option:
```

**Add train — example:**

```
Choose an option: 1
Enter train capacity: 150

Train added successfully with ID: 3
```

**View all trains — example output:**

```
===== ALL TRAINS =====
ID: 1 | Capacity: 120
ID: 2 | Capacity: 200
ID: 3 | Capacity: 150
```

**Modify train — example:**

```
Choose an option: 3

===== ALL TRAINS =====
ID: 1 | Capacity: 120
...

Enter train ID to modify: 1
Enter new capacity: 130

Train modified successfully
```

**Remove train — example:**

```
Choose an option: 2

===== ALL TRAINS =====
ID: 3 | Capacity: 150

Enter train ID to remove: 3

Train removed successfully
```

**Remove train — not found:**

```
Train not found
```

---

#### 3.2 Manage Stations

```
========== MANAGE STATIONS ==========
1. Add station
2. Remove station
3. Modify station
4. View all stations
Choose an option:
```

**Add station — example:**

```
Choose an option: 1
Enter station name: Sinaia
Enter station location: Prahova

Station added successfully with ID: 4
```

**View all stations — example output:**

```
===== ALL STATIONS =====
ID: 1 | Bucuresti Nord (Bucuresti)
ID: 2 | Brasov (Brasov)
ID: 3 | Cluj-Napoca (Cluj)
ID: 4 | Sinaia (Prahova)
```

**Modify station — example:**

```
Choose an option: 3

Enter station ID to modify: 4
Enter new name: Sinaia Central
Enter new location: Prahova Valley

Station modified successfully
```

**Remove station — example:**

```
Choose an option: 2

Enter station ID to remove: 4

Station removed successfully
```

---

#### 3.3 Manage Routes

A route is an ordered list of stations that trains travel through.

```
========== MANAGE ROUTES ==========
1. Add route
2. Remove route
3. View all routes
Choose an option:
```

**Add route — example:**

```
Choose an option: 1

===== ALL STATIONS =====
ID: 1 | Bucuresti Nord (Bucuresti)
ID: 2 | Brasov (Brasov)
ID: 3 | Cluj-Napoca (Cluj)

Enter station IDs separated by commas (e.g., 1,2,3): 3,2,1

Route added successfully with ID: 2
```

**View all routes — example output:**

```
===== ALL ROUTES =====
Route ID: 1
Route with 3 stops:
- Bucuresti Nord (Bucuresti)
- Brasov (Brasov)
- Cluj-Napoca (Cluj)

Route ID: 2
Route with 3 stops:
- Cluj-Napoca (Cluj)
- Brasov (Brasov)
- Bucuresti Nord (Bucuresti)
```

**Remove route — example:**

```
Choose an option: 2

Enter route ID to remove: 2

Route removed successfully
```

**Add route — station not found:**

```
Station with ID 99 not found
```

---

#### 3.4 Manage Trips

A trip assigns a train to a route with a specific departure and arrival time.

```
========== MANAGE TRIPS ==========
1. Add trip
2. Remove trip
3. View all trips
Choose an option:
```

**Add trip — example:**

```
Choose an option: 1

===== ALL TRAINS =====
ID: 1 | Capacity: 120
ID: 2 | Capacity: 200

Enter train ID: 2

===== ALL ROUTES =====
Route ID: 1
...

Enter route ID: 1
Enter departure time (HH:mm): 08:00
Enter arrival time (HH:mm): 16:45

Trip added successfully with ID: 2
```

**View all trips — example output:**

```
===== ALL TRIPS =====
Trip ID: 1 | Train: 1 | Route: 1 | Departure: 06:00 | Arrival: 14:30 | Delay: 0 min
Trip ID: 2 | Train: 2 | Route: 1 | Departure: 08:00 | Arrival: 16:45 | Delay: 0 min
```

**Remove trip — example:**

```
Choose an option: 2

Enter trip ID to remove: 2

Trip removed successfully
```

**Add trip — invalid time format:**

```
Booking failed: Departure time must be in HH:mm format
```

---

#### 3.5 View Bookings

```
========== VIEW BOOKINGS ==========
1. View all bookings
2. View bookings for specific train
Choose an option:
```

**View all bookings — example output:**

```
===== ALL BOOKINGS =====
Booking ID: 1 | Customer: alice@example.com | Tickets: 2 | Trip ID: 1
Booking ID: 2 | Customer: bob@example.com   | Tickets: 1 | Trip ID: 1
```

**View bookings for a specific train — example:**

```
Choose an option: 2

===== ALL TRAINS =====
ID: 1 | Capacity: 120

Enter train ID: 1

===== BOOKINGS FOR TRAIN 1 =====
Booking ID: 1 | Email: alice@example.com | Tickets: 2 | Trip ID: 1
Booking ID: 2 | Email: bob@example.com   | Tickets: 1 | Trip ID: 1
Total Tickets Booked: 3
```

**No bookings found:**

```
No bookings found for this train
```

---

#### 3.6 Handle Delays

Updates the delay for a trip in minutes and automatically sends an email notification to every passenger booked on that trip.

**Flow:**
1. All trips are displayed with their current delay.
2. Admin selects a trip and enters the new delay in minutes.
3. The delay is saved to the database and notification emails are printed to the console for each affected booking.

**Example input:**

```
========== HANDLE DELAYS ==========

--- Available Trips ---
1. Trip ID: 1 | Departure: 06:00 | Current Delay: 0 min

Select trip number: 1
Enter delay in minutes: 30
```

**Example output (with two bookings on the trip):**

```
========== EMAIL NOTIFICATION ==========
To: alice@example.com
Subject: Train Delay Notification
Message: Important: Your train has been delayed by 30 minutes.

Route: Route with 3 stops:
- Bucuresti Nord (Bucuresti)
- Brasov (Brasov)
- Cluj-Napoca (Cluj)

Original Departure: 06:00
New Estimated Departure: 06:30
Booking Reference: 1
========================================

========== EMAIL NOTIFICATION ==========
To: bob@example.com
Subject: Train Delay Notification
Message: Important: Your train has been delayed by 30 minutes.

Route: Route with 3 stops:
...
Booking Reference: 2
========================================

Trip delay updated and customers notified
```

---

## Design Patterns

| Pattern | Where used |
|---|---|
| **Repository** | `DBRepository<ID, E>` — generic CRUD operations; specialised by `TrainDBRepository`, `StationDBRepository`, etc. |
| **Service layer** | `AdminService`, `TripService`, `BookingService` — business logic isolated from UI and persistence |
| **Singleton** | `EmailService.getInstance()` — single shared email sender |
| **Observer / Observable** | `utils.Observer` / `utils.Observable` — event hook for future extensibility (e.g., real-time seat updates) |
| **Validator** | `Validator<T>` interface implemented per entity — keeps validation rules out of constructors |

---

## Project Structure

```
Train_Ticketing/
├── sql/
│   └── schema.sql                  # DDL + seed data
├── src/main/java/
│   ├── Main.java                   # Entry point; wires dependencies
│   ├── domain/
│   │   ├── Entity.java             # Base class with generic ID
│   │   ├── Train.java
│   │   ├── Station.java
│   │   ├── Route.java
│   │   ├── Trip.java
│   │   ├── Booking.java
│   │   ├── validator/
│   │   │   ├── Validator.java      # Interface
│   │   │   ├── TrainValidator.java
│   │   │   ├── StationValidator.java
│   │   │   ├── RouteValidator.java
│   │   │   ├── TripValidator.java
│   │   │   └── BookingValidator.java
│   │   └── exception/
│   │       ├── ValidationException.java
│   │       └── PersistenceException.java
│   ├── repository/
│   │   ├── Repository.java         # CRUD interface
│   │   ├── DBRepository.java       # Generic JDBC base
│   │   ├── TrainDBRepository.java
│   │   ├── StationDBRepository.java
│   │   ├── RouteDBRepository.java
│   │   ├── TripDBRepository.java
│   │   └── BookingDBRepository.java
│   ├── service/
│   │   ├── Service.java            # Generic service base
│   │   ├── AdminService.java       # All admin operations
│   │   ├── TrainService.java
│   │   ├── StationService.java
│   │   ├── RouteService.java
│   │   ├── TripService.java        # Route search + delay logic
│   │   └── BookingService.java
│   ├── ui/
│   │   └── Console.java            # All console I/O
│   └── utils/
│       ├── DatabaseConfig.java     # Reads db.properties; initialises schema
│       ├── JdbcUtils.java
│       ├── EmailService.java       # Singleton; prints email to stdout
│       ├── Observable.java
│       └── Observer.java
├── src/main/resources/
│   └── db.properties               # JDBC URL for the SQLite file
├── build.gradle.kts
├── settings.gradle.kts
└── train_ticketing.db              # Auto-created SQLite database
```