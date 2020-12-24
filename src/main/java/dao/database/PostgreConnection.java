package dao.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class PostgreConnection implements ConnectionBuilder {
    private final Properties config;
    private final Properties utilityQueries;
    private final String LOCAL_CONFIG_PATH = "src/main/resources/localConfig.properties";
    private final String PRODUCTION_CONFIG_PATH = "src/main/resources/productionConfig.properties";
    private final String PATH_TO_UTILITY_QUERIES = "src/main/resources/utilityQueries.properties";
    private final String DATABASE_NAME = "contacts";

    /*
     throw all exceptions because if we don't have a config file
     or we have an invalid driver we cannot keep program running
     */
    public PostgreConnection() throws IOException, ClassNotFoundException {
        config = new Properties();
        utilityQueries = new Properties();
        if (new File(LOCAL_CONFIG_PATH).exists())
            config.load(new FileInputStream(LOCAL_CONFIG_PATH));
        else
            config.load(new FileInputStream(PRODUCTION_CONFIG_PATH));
        utilityQueries.load(new FileInputStream(PATH_TO_UTILITY_QUERIES));

        Class.forName(config.getProperty("driver"));
    }

    private boolean isDatabaseExist() throws SQLException {
        var isExist = false;
        try (
            var connection = DriverManager.getConnection(
                config.getProperty("host"),
                config.getProperty("login"),
                config.getProperty("password"));
            var ps = connection.prepareStatement(utilityQueries.getProperty("GET_LIST_DATABASES")
            );
            var rs = ps.executeQuery()) {
            while (rs.next()) {
                if (rs.getString(1).equals(DATABASE_NAME))
                    isExist = true;
            }
        }
        return isExist;
    }

    private void createDatabase() throws SQLException {
        var connection = DriverManager.getConnection(
            config.getProperty("host"),
            config.getProperty("login"),
            config.getProperty("password"));

        //create "ContactsDB"
        var stm = connection.prepareStatement(utilityQueries.getProperty("CREATE_DATABASE"));
        stm.executeUpdate();
    }

    private void createTables(Connection connection) throws SQLException {
        //create users
        var stm = connection.prepareStatement(utilityQueries.getProperty("CREATE_TABLE_USERS"));
        stm.executeUpdate();

        //create records
        stm = connection.prepareStatement(utilityQueries.getProperty("CREATE_TABLE_RECORDS"));
        stm.executeUpdate();

        //create numbers
        stm = connection.prepareStatement(utilityQueries.getProperty("CREATE_TABLE_NUMBERS"));
        stm.executeUpdate();
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection;
        if (isDatabaseExist())
            connection = DriverManager.getConnection(
                config.getProperty("host") + DATABASE_NAME,
                config.getProperty("login"),
                config.getProperty("password"));
        else {
            createDatabase();
            connection = DriverManager.getConnection(
                config.getProperty("host") + DATABASE_NAME,
                config.getProperty("login"),
                config.getProperty("password"));
            createTables(connection);
        }
        return connection;
    }
}
