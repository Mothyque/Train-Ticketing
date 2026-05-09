package repository;

import domain.Booking;
import domain.Trip;
import domain.exception.PersistenceException;

import java.sql.*;
import java.util.Optional;

public class BookingDBRepository extends DBRepository<Integer, Booking>
{

    private final TripDBRepository tripRepo;

    public BookingDBRepository(String url, String username, String password, TripDBRepository tripRepo)
    {
        super(url, username, password);
        this.tripRepo = tripRepo;
    }

    @Override
    protected String getFindAllSQL()
    {
        return "SELECT * FROM bookings";
    }

    @Override
    protected String getFindOneSQL()
    {
        return "SELECT * FROM bookings WHERE id = ?";
    }

    @Override
    protected String getSaveSQL()
    {
        return "INSERT INTO bookings (trip_id, customer_email, ticket_count) VALUES (?, ?, ?)";
    }

    @Override
    protected String getDeleteSQL()
    {
        return "DELETE FROM bookings WHERE id = ?";
    }

    @Override
    protected String getUpdateSQL()
    {
        return "UPDATE bookings SET trip_id = ?, customer_email = ?, ticket_count = ? WHERE id = ?";
    }

    @Override
    protected String getSizeSQL()
    {
        return "SELECT COUNT(*) FROM bookings";
    }

    @Override
    protected Booking extractEntity(ResultSet rs) throws SQLException
    {
        Integer id = rs.getInt("id");
        int tripId = rs.getInt("trip_id");
        String email = rs.getString("customer_email");
        int seatsCount = rs.getInt("ticket_count");

        Trip trip = tripRepo.findOne(tripId).orElse(null);

        Booking booking = new Booking(email, trip, seatsCount);
        booking.setId(id);
        return booking;
    }

    @Override
    protected void setSaveParameters(PreparedStatement statement, Booking entity) throws SQLException
    {
        statement.setInt(1, entity.getTrip().getId());
        statement.setString(2, entity.getEmail());
        statement.setInt(3, entity.getSeatCount());
    }

    @Override
    public Optional<Booking> save(Booking entity)
    {
        String sql = getSaveSQL();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            setSaveParameters(statement, entity);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys())
            {
                if (generatedKeys.next())
                {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

            return Optional.of(entity);
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error saving booking", e);
        }
    }

    @Override
    protected void setFindOneDeleteParameters(PreparedStatement statement, Integer id) throws SQLException
    {
        statement.setInt(1, id);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Booking entity) throws SQLException
    {
        setSaveParameters(statement, entity);
        statement.setInt(4, entity.getId());
    }
}