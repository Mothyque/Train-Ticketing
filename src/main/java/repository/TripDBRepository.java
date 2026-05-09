package repository;

import domain.Route;
import domain.Train;
import domain.Trip;
import domain.exception.PersistenceException;

import java.sql.*;
import java.time.LocalTime;
import java.util.Optional;

public class TripDBRepository extends DBRepository<Integer, Trip>
{

    private final TrainDBRepository trainRepo;
    private final RouteDBRepository routeRepo;

    public TripDBRepository(String url, String username, String password,
                            TrainDBRepository trainRepo, RouteDBRepository routeRepo)
    {
        super(url, username, password);
        this.trainRepo = trainRepo;
        this.routeRepo = routeRepo;
    }

    @Override
    protected String getFindAllSQL()
    {
        return "SELECT * FROM trips";
    }

    @Override
    protected String getFindOneSQL()
    {
        return "SELECT * FROM trips WHERE id = ?";
    }

    @Override
    protected String getSaveSQL()
    {
        return "INSERT INTO trips (train_id, route_id, departure_time, arrival_time, delay_minutes) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getDeleteSQL()
    {
        return "DELETE FROM trips WHERE id = ?";
    }

    @Override
    protected String getUpdateSQL()
    {
        return "UPDATE trips SET train_id = ?, route_id = ?, departure_time = ?, arrival_time = ?, delay_minutes = ? WHERE id = ?";
    }

    @Override
    protected String getSizeSQL()
    {
        return "SELECT COUNT(*) FROM trips";
    }

    @Override
    protected Trip extractEntity(ResultSet rs) throws SQLException
    {
        Integer id = rs.getInt("id");
        int trainId = rs.getInt("train_id");
        int routeId = rs.getInt("route_id");

        LocalTime depTime = LocalTime.parse(rs.getString("departure_time"));
        LocalTime arrTime = LocalTime.parse(rs.getString("arrival_time"));
        Integer delay = rs.getInt("delay_minutes");

        Train train = trainRepo.findOne(trainId).orElse(null);
        Route route = routeRepo.findOne(routeId).orElse(null);

        Trip trip = new Trip(train, route, depTime, delay, arrTime);
        trip.setId(id);
        return trip;
    }

    @Override
    protected void setSaveParameters(PreparedStatement statement, Trip entity) throws SQLException
    {
        statement.setInt(1, entity.getTrain().getId());
        statement.setInt(2, entity.getRoute().getId());
        statement.setString(3, entity.getDepartureTime().toString());
        statement.setString(4, entity.getArrivalTime().toString());
        statement.setInt(5, entity.getDelay());
    }

    @Override
    public Optional<Trip> save(Trip entity)
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
            throw new PersistenceException("Error saving trip", e);
        }
    }

    @Override
    protected void setFindOneDeleteParameters(PreparedStatement statement, Integer id) throws SQLException
    {
        statement.setInt(1, id);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Trip entity) throws SQLException
    {
        setSaveParameters(statement, entity);
        statement.setInt(6, entity.getId());
    }
}