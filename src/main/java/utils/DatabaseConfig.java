package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig
{
    private static final String PROPERTIES_FILE = "db.properties";
    private static final String DB_URL_KEY = "jdbc.url";
    private static final String DEFAULT_URL = "jdbc:sqlite:train_ticketing.db";

    private static DatabaseConfig instance;
    private Properties properties;

    private DatabaseConfig()
    {
        properties = new Properties();
        loadProperties();
    }

    public static DatabaseConfig getInstance()
    {
        if (instance == null)
        {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadProperties()
    {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE))
        {
            if (input == null)
            {
                System.out.println("Warning: db.properties not found. Using default configuration.");
                properties.setProperty(DB_URL_KEY, DEFAULT_URL);
                return;
            }
            properties.load(input);
        }
        catch (IOException e)
        {
            System.out.println("Warning: Failed to load db.properties. Using default configuration.");
            properties.setProperty(DB_URL_KEY, DEFAULT_URL);
        }
    }

    public String getDatabaseUrl()
    {
        return properties.getProperty(DB_URL_KEY, DEFAULT_URL);
    }

    public String getProperty(String key)
    {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }
}

