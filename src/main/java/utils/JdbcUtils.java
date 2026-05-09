package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {
    private String url;
    private Connection instance = null;

    public JdbcUtils(Properties props)
    {
        this.url = props.getProperty("jdbc:sqlite:train_ticketing.db");
    }

    public Connection getConnection()
    {
        try
        {
            if (instance == null || instance.isClosed())
            {
                instance = DriverManager.getConnection(url);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error getting connection " + e);
        }
        return instance;
    }
}