package repository;
import domain.Station;
import domain.exception.PersistenceException;

import java.sql.*;
import java.util.Optional;

public class StationDBRepository extends DBRepository<Integer, Station>
{

    public StationDBRepository(String url, String username, String password)
    {
        super(url, username, password);
    }

    @Override
    protected String getFindAllSQL()
    {
        return "SELECT * FROM stations";
    }

    @Override
    protected String getFindOneSQL()
    {
        return "SELECT * FROM stations WHERE id = ?";
    }

    @Override
    protected String getSaveSQL()
    {
        return "INSERT INTO stations (name, location) VALUES (?, ?)";
    }

    @Override
    protected String getDeleteSQL()
    {
        return "DELETE FROM stations WHERE id = ?";
    }

    @Override
    protected String getUpdateSQL()
    {
        return "UPDATE stations SET name = ?, location = ? WHERE id = ?";
    }

    @Override
    protected String getSizeSQL()
    {
        return "SELECT COUNT(*) FROM stations";
    }

    @Override
    protected Station extractEntity(ResultSet rs) throws SQLException
    {
        Integer id = rs.getInt("id");
        String name = rs.getString("name");
        String location = rs.getString("location");

        Station station = new Station(name, location);
        station.setId(id);
        return station;
    }

    @Override
    protected void setSaveParameters(PreparedStatement statement, Station entity) throws SQLException
    {
        statement.setString(1, entity.getName());
        statement.setString(2, entity.getLocation());
    }

    @Override
    public Optional<Station> save(Station entity)
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
            throw new PersistenceException("Error saving station", e);
        }
    }

    @Override
    protected void setFindOneDeleteParameters(PreparedStatement statement, Integer id) throws SQLException
    {
        statement.setInt(1, id);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Station entity) throws SQLException
    {
        statement.setString(1, entity.getName());
        statement.setString(2, entity.getLocation());
        statement.setInt(3, entity.getId());
    }
}