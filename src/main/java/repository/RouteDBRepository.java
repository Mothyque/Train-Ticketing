package repository;
import domain.Route;
import domain.Station;
import domain.exception.PersistenceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RouteDBRepository extends DBRepository<Integer, Route>
{

    private final StationDBRepository stationRepo;

    public RouteDBRepository(String url, String username, String password, StationDBRepository stationRepo)
    {
        super(url, username, password);
        this.stationRepo = stationRepo;
    }

    @Override
    protected String getFindAllSQL()
    {
        return "SELECT id FROM routes";
    }

    @Override
    protected String getFindOneSQL()
    {
        return "SELECT id FROM routes WHERE id = ?";
    }

    @Override
    protected String getSaveSQL()
    {
        return "INSERT INTO routes DEFAULT VALUES";
    }

    @Override
    protected String getDeleteSQL()
    {
        return "DELETE FROM routes WHERE id = ?";
    }

    @Override
    protected String getUpdateSQL()
    {
        return "SELECT id FROM routes WHERE id = ?";
    }

    @Override
    protected String getSizeSQL()
    {
        return "SELECT COUNT(*) FROM routes";
    }

    @Override
    protected Route extractEntity(ResultSet rs) throws SQLException
    {
        int routeId = rs.getInt("id");
        Route route = new Route();
        route.setId(routeId);

        String junctionSql = "SELECT station_id FROM route_stations WHERE route_id = ? ORDER BY stop_order ASC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(junctionSql))
        {
            stmt.setInt(1, routeId);
            ResultSet stationsRs = stmt.executeQuery();
            while (stationsRs.next())
            {
                int stationId = stationsRs.getInt("station_id");
                stationRepo.findOne(stationId).ifPresent(route::addStation);
            }
        }
        return route;
    }

    @Override
    public Optional<Route> save(Route entity)
    {
        String sql = "INSERT INTO routes DEFAULT VALUES";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys())
            {
                if (generatedKeys.next())
                {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

            saveRouteStations(entity, connection);
            return Optional.of(entity);
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error saving route and its stations", e);
        }
    }

    private void saveRouteStations(Route entity, Connection connection) throws SQLException
    {
        String sql = "INSERT INTO route_stations (route_id, station_id, stop_order) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            List<Station> stations = entity.getStations();
            for (int i = 0; i < stations.size(); i++)
            {
                statement.setInt(1, entity.getId());
                statement.setInt(2, stations.get(i).getId());
                statement.setInt(3, i);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    protected void setSaveParameters(PreparedStatement statement, Route entity) throws SQLException
    {
    }

    @Override
    protected void setFindOneDeleteParameters(PreparedStatement statement, Integer id) throws SQLException
    {
        statement.setInt(1, id);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Route entity) throws SQLException
    {
    }
}