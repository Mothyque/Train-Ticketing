package repository;

import domain.Train;
import domain.exception.PersistenceException;

import java.sql.*;
import java.util.Optional;

public class TrainDBRepository extends DBRepository<Integer, Train>
{

    public TrainDBRepository(String url, String username, String password)
    {
        super(url, username, password);
    }

    @Override
    protected String getFindAllSQL()
    {
        return "SELECT * FROM trains";
    }

    @Override
    protected String getFindOneSQL()
    {
        return "SELECT * FROM trains WHERE id = ?";
    }

    @Override
    protected String getSaveSQL()
    {
        return "INSERT INTO trains (capacity) VALUES (?)";
    }

    @Override
    protected String getDeleteSQL()
    {
        return "DELETE FROM trains WHERE id = ?";
    }

    @Override
    protected String getUpdateSQL()
    {
        return "UPDATE trains SET capacity = ? WHERE id = ?";
    }

    @Override
    protected String getSizeSQL()
    {
        return "SELECT COUNT(*) FROM trains";
    }

    @Override
    protected Train extractEntity(ResultSet rs) throws SQLException
    {
        Integer id = rs.getInt("id");
        Integer capacity = rs.getInt("capacity");

        Train train = new Train(capacity);
        train.setId(id);
        return train;
    }

    @Override
    protected void setSaveParameters(PreparedStatement statement, Train entity) throws SQLException
    {
        statement.setInt(1, entity.getCapacity());
    }

    @Override
    public Optional<Train> save(Train entity)
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
            throw new PersistenceException("Error saving train", e);
        }
    }

    @Override
    protected void setFindOneDeleteParameters(PreparedStatement statement, Integer id) throws SQLException
    {
        statement.setInt(1, id);
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, Train entity) throws SQLException
    {
        statement.setInt(1, entity.getCapacity());
        statement.setInt(2, entity.getId());
    }
}