package repository;

import domain.Entity;
import domain.exception.PersistenceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DBRepository<ID, E extends Entity<ID>> implements Repository<ID, E>
{
    private final String url;
    private final String username;
    private final String password;

    public DBRepository(String url, String username, String password)
    {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    protected Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Iterable<E> findAll()
    {
        List<E> entities = new ArrayList<>();
        String sql = getFindAllSQL();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery())
        {
            while (rs.next())
            {
                E entity = extractEntity(rs);
                entities.add(entity);
            }
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error finding all entities", e);
        }
        return entities;
    }

    public Iterable<E> findAll(int pageNumber, int pageSize)
    {
        List<E> entities = new ArrayList<>();
        String sql = getFindAllSQL() + " LIMIT ? OFFSET ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, pageSize);
            statement.setInt(2, (pageNumber - 1) * pageSize);

            try (ResultSet rs = statement.executeQuery())
            {
                while (rs.next())
                {
                    E entity = extractEntity(rs);
                    entities.add(entity);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return entities;
    }

    @Override
    public Optional<E> findOne(ID id)
    {
        String sql = getFindOneSQL();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            setFindOneDeleteParameters(statement, id);
            try (ResultSet rs = statement.executeQuery())
            {
                if (rs.next())
                {
                    return Optional.of(extractEntity(rs));
                }
            }
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error finding entity", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<E> save(E entity)
    {
        String sql = getSaveSQL();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            setSaveParameters(statement, entity);
            statement.executeUpdate();
            return Optional.of(entity);
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error saving entity", e);
        }
    }

    @Override
    public Optional<E> delete(ID id)
    {
        Optional<E> entityToDelete = findOne(id);
        if (entityToDelete.isPresent())
        {
            String sql = getDeleteSQL();
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql))
            {
                setFindOneDeleteParameters(statement, id);
                statement.executeUpdate();
                return entityToDelete;
            }
            catch (SQLException e)
            {
                throw new PersistenceException("Error deleting entity", e);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<E> update(E entity)
    {
        String sql = getUpdateSQL();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql))
        {
            setUpdateParameters(statement, entity);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0)
            {
                return Optional.of(entity);
            }
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error updating entity", e);
        }
        return Optional.empty();
    }

    @Override
    public int size()
    {
        String sql = getSizeSQL();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery())
        {
            if (rs.next())
            {
                return rs.getInt(1);
            }
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error counting entities", e);
        }
        return 0;
    }

    protected abstract String getFindAllSQL();
    protected abstract String getFindOneSQL();
    protected abstract String getSaveSQL();
    protected abstract String getDeleteSQL();
    protected abstract String getUpdateSQL();
    protected abstract String getSizeSQL();

    protected abstract E extractEntity(ResultSet rs) throws SQLException;
    protected abstract void setSaveParameters(PreparedStatement statement, E entity) throws SQLException;
    protected abstract void setFindOneDeleteParameters(PreparedStatement statement, ID id) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement statement, E entity) throws SQLException;
}