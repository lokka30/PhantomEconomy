package io.github.lokka30.phantomeconomy_v2.databases;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.enums.DatabaseType;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class Database {

    private PhantomEconomy instance;
    private Connection connection;

    HashMap<UUID, Double> baltopMap;
    double serverTotal;

    public Database(PhantomEconomy instance) {
        this.instance = instance;
        baltopMap = new HashMap<>();
        serverTotal = -1;
    }

    public DatabaseType getDatabaseType() {
        switch (instance.fileCache.SETTINGS_DATABASE_TYPE) {
            case "sqlite":
                return DatabaseType.SQLITE;
            case "mysql":
                return DatabaseType.MYSQL;
            default:
                instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7You haven't set a valid database type in your settings file. Temporarily using SQLite. Please update this value to 'sqlite' or 'mysql' as soon as possible!");
                instance.fileCache.SETTINGS_DATABASE_TYPE = "sqlite";
                return DatabaseType.SQLITE;
        }
    }

    public Connection getConnection() {
        switch (getDatabaseType()) {
            case SQLITE:
                if (!instance.getDataFolder().exists()) {
                    try {
                        if (!instance.getDataFolder().createNewFile()) {
                            instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to create data folder");
                            return null;
                        }
                    } catch (IOException exception) {
                        instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to create data folder for the SQLite database. Exception:");
                        exception.printStackTrace();
                        return null;
                    }
                }
                try {
                    if (connection != null && !connection.isClosed()) {
                        return connection;
                    }

                    try {
                        Class.forName("org.sqlite.JDBC");
                    } catch (ClassNotFoundException e) {
                        instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to connect to the SQLite database - You do not have the SQLite JDBC library installed.");
                        e.printStackTrace();
                        return null;
                    }

                    connection = DriverManager.getConnection("jdbc:sqlite:" + instance.getDataFolder());
                    return connection;
                } catch (SQLException e) {
                    instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to connect to the SQLite database. Stack trace:");
                    e.printStackTrace();
                }
                break;
            case MYSQL:
                try {
                    if (connection != null && !connection.isClosed()) {
                        return connection;
                    }
                } catch (SQLException exception) {
                    instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to check if connection is already available in getsqlconnection");
                    return null;
                }

                synchronized (this) {
                    try {
                        if (connection != null && !connection.isClosed()) {
                            return connection;
                        }
                    } catch (SQLException exception) {
                        instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to check if connection is already available in synch getsqlconn");
                        return null;
                    }

                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                    } catch (ClassNotFoundException exception) {
                        instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7 MySQL JDBC driver is not installed, you must have it installed to use this database.");
                        return null;
                    }

                    try {
                        connection = DriverManager.getConnection("jdbc:mysql://" + instance.fileCache.SETTINGS_DATABASE_MYSQL_HOST + ":" + instance.fileCache.SETTINGS_DATABASE_MYSQL_PORT + "/" + instance.fileCache.SETTINGS_DATABASE_MYSQL_DATABASE, instance.fileCache.SETTINGS_DATABASE_MYSQL_USERNAME, instance.fileCache.SETTINGS_DATABASE_MYSQL_PASSWORD);
                    } catch (SQLException exception) {
                        instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to establish connection to MySQL database. Ensure that you have entered the correct details in the MySQL configuration section in the settings file.");
                    }
                }
                break;
            default:
                throw new IllegalStateException("Invalid database type " + getConnection().toString());
        }
        return null;
    }

    public void load() {
        connection = getConnection();

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS phantomeconomy (`accounttype` varchar(32) NOT NULL, `accountid` varchar(32) NOT NULL, `currencyname` varchar(32) NOT NULL, `balance` double(64) NOT NULL, PRIMARY KEY (`accountid`));");
            statement.close();
        } catch (SQLException exception) {
            instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to load the database. Stack trace:");
            exception.printStackTrace();
        }

        initialise();
    }

    public void initialise() {
        connection = getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM phantomeconomy WHERE accountid = ?");
            ResultSet resultSet = preparedStatement.executeQuery();
            close(preparedStatement, resultSet);
        } catch (SQLException exception) {
            instance.utils.log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to initialise the database. Stack trace:");
            exception.printStackTrace();
        }
    }

    public double getBalance(String accountType, String accountId, String currencyName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM phantomeconomy WHERE accounttype = '" + accountType + "', accountid = '" + accountId + "', currencyname = '" + currencyName + "';");
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                //TODO PUT THE DEFAULT BALANCE INTO THE DATABASE.
                //TODO RETURN DEFAULT BALANCE FOR THAT CURRENCY.
                return 0.0;
            }

            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to getBalance for accounttype '" + accountType + "', accountid '" + accountId + "', currency '" + currencyName + "'. Stack trace:");
            exception.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to close SQLConnection for getBalance with accounttype '" + accountType + "', accountid '" + accountId + "', currency '" + currencyName + "'. Stack trace:");
                exception.printStackTrace();
            }
        }

        return 0;
    }

    public void setBalance(String accountType, String accountId, String currencyName, double newBalance) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement("REPLACE INTO phantomeconomy (accounttype,accountid,currencyname,balance) VALUES(?,?,?,?)");
            preparedStatement.setString(1, accountType);
            preparedStatement.setString(2, accountId);
            preparedStatement.setString(3, currencyName);
            preparedStatement.setDouble(4, newBalance);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to setBalance for accounttype '" + accountType + "', accountid '" + accountId + "', currency '" + currencyName + "', balance '" + newBalance + "'. Stack trace:");
            exception.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to close SQLConnection for setBalance with accounttype '" + accountType + "', accountid '" + accountId + "', currency '" + currencyName + "', balance '" + newBalance + "'. Stack trace:");
                exception.printStackTrace();
            }
        }
    }

    public HashMap<UUID, Double> getBaltopMap(Currency currency) throws SQLException {
        if (baltopMap.size() == 0) {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM phantomeconomy WHERE accounttype = PlayerAccount, currency = ?;");
            preparedStatement.setString(1, currency.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("accountid"));
                double balance = resultSet.getDouble("balance");
                baltopMap.put(uuid, balance);
            }
        }

        return baltopMap;
    }

    public double getBaltopServerTotal(Currency currency) throws SQLException {
        if (serverTotal == -1) {
            connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT SUM(balance) FROM phantomeconomy WHERE currency = ?;");
            preparedStatement.setString(1, currency.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                serverTotal = resultSet.getDouble("balance");
                return serverTotal;
            }

            serverTotal = 0.0; //This value is shown if nobody has a balance yet
        }

        return serverTotal;
    }

    public boolean hasAccount(String accountType, String accountId) throws SQLException {
        Connection connection;
        PreparedStatement preparedStatement;

        connection = getConnection();
        preparedStatement = connection.prepareStatement("SELECT 1 FROM phantomeconomy WHERE accounttype = ?, accountid = ?;");
        preparedStatement.setString(1, accountType);
        preparedStatement.setString(2, accountId);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            return resultSet.getString("accountid") != null;
        }

        return false;
    }

    public void close(PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException exception) {
            instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to close PreparedStatement and ResultSet. Stack trace:");
            exception.printStackTrace();
        }
    }
}
