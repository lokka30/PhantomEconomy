package io.github.lokka30.phantomeconomy_v2.databases;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy_v2.enums.DatabaseType;
import io.github.lokka30.phantomlib.enums.LogLevel;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class Database {

    /*
    Thanks to Hugo5551 for their contributions in this class!
     */

    private PhantomEconomy instance;
    private Connection connection;

    private HashMap<UUID, Double> baltopMap;
    private double serverTotal;

    public Database(PhantomEconomy instance) {
        this.instance = instance;
        baltopMap = new HashMap<>();
        serverTotal = -1;
    }

    public DatabaseType getDatabaseType() {
        return instance.getFileCache().databaseType;
    }

    public Connection getConnection() {
        switch (getDatabaseType()) {
            case SQLITE:
                File databaseFile = new File(instance.getDataFolder() + File.separator + "database.db");

                synchronized (this) {
                    if (!instance.getDataFolder().exists()) {
                        if (!instance.getDataFolder().mkdir()) {
                            instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to create data folder");
                            return null;
                        }
                    }

                    if (!databaseFile.exists()) {
                        try {
                            if (databaseFile.createNewFile()) {
                                instance.getPhantomLogger().log(LogLevel.INFO, "&b&lPhantomEconomy: &7", "File &bdatabase.db&7 didn't exist, it has now been created.");
                            }
                        } catch (IOException exception) {
                            instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to create database file");
                            exception.printStackTrace();
                        }
                    }

                    try {
                        if (connection != null && !connection.isClosed()) {
                            return connection;
                        }

                        try {
                            Class.forName("org.sqlite.JDBC");
                        } catch (ClassNotFoundException e) {
                            instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to connect to the SQLite database - You do not have the SQLite JDBC library installed.");
                            e.printStackTrace();
                            return null;
                        }

                        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
                    } catch (SQLException e) {
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to connect to the SQLite database. Stack trace:");
                        e.printStackTrace();
                    }

                    return connection;
                }
            case MYSQL:
                synchronized (this) {
                    try {
                        if (connection != null && !connection.isClosed()) {
                            return connection;
                        }
                    } catch (SQLException exception) {
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to check if connection is already available in synch getsqlconn");
                        return null;
                    }

                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                    } catch (ClassNotFoundException exception) {
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7 MySQL JDBC driver is not installed, you must have it installed to use this database.");
                        return null;
                    }

                    try {
                        connection = DriverManager.getConnection("jdbc:mysql://" + instance.getFileCache().SETTINGS_DATABASE_MYSQL_HOST + ":" + instance.getFileCache().SETTINGS_DATABASE_MYSQL_PORT + "/" + instance.getFileCache().SETTINGS_DATABASE_MYSQL_DATABASE, instance.getFileCache().SETTINGS_DATABASE_MYSQL_USERNAME, instance.getFileCache().SETTINGS_DATABASE_MYSQL_PASSWORD);
                    } catch (SQLException exception) {
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7Unable to establish connection to MySQL database. Ensure that you have entered the correct details in the MySQL configuration section in the settings file.");
                    }

                    return connection;
                }
            default:
                throw new IllegalStateException("Invalid database type " + getConnection().toString());
        }
    }

    public void load() throws SQLException {
        connection = getConnection();

        Statement statement1 = connection.createStatement();
        statement1.executeUpdate("CREATE TABLE IF NOT EXISTS " + instance.getFileCache().SETTINGS_DATABASE_TABLE_PLAYERACCOUNT + "('accountType' VARCHAR(32) NOT NULL, 'accountId' VARCHAR(48) NOT NULL, 'currencyName' VARCHAR(48) NOT NULL, 'balance' DECIMAL(48,2) NOT NULL, PRIMARY KEY('accountType', 'accountId', 'currencyName'));");
        statement1.close();

        Statement statement2 = connection.createStatement();
        statement2.executeUpdate("CREATE TABLE IF NOT EXISTS " + instance.getFileCache().SETTINGS_DATABASE_TABLE_NONPLAYERACCOUNT + "('accountType' VARCHAR(32) NOT NULL, 'accountId' VARCHAR(48) NOT NULL, 'currencyName' VARCHAR(48) NOT NULL, 'balance' DECIMAL(48,2) NOT NULL, PRIMARY KEY('accountType', 'accountId', 'currencyName'));");
        statement2.close();

        Statement statement3 = connection.createStatement();
        statement3.executeUpdate("CREATE TABLE IF NOT EXISTS " + instance.getFileCache().SETTINGS_DATABASE_TABLE_BANKACCOUNT + "('accountType' VARCHAR(32) NOT NULL, 'accountId' VARCHAR(48) NOT NULL, 'currencyName' VARCHAR(48) NOT NULL, 'balance' DECIMAL(48,2) NOT NULL, PRIMARY KEY('accountType', 'accountId', 'currencyName'));");
        statement3.close();

        if (connection != null) {
            connection.close();
        }
    }

    public String getTableNameFromAccountTypeStr(String accountType) {
        switch (accountType.toLowerCase()) {
            case "playeraccount":
                return instance.getFileCache().SETTINGS_DATABASE_TABLE_PLAYERACCOUNT;
            case "nonplayeraccount":
                return instance.getFileCache().SETTINGS_DATABASE_TABLE_NONPLAYERACCOUNT;
            case "bankaccount":
                return instance.getFileCache().SETTINGS_DATABASE_TABLE_BANKACCOUNT;
            default:
                instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7Illegal account type '" + accountType + "'! using fallback NonPlayerAccount. this should be fixed immediately!");
                return instance.getFileCache().SETTINGS_DATABASE_TABLE_NONPLAYERACCOUNT;
        }
    }

    public double getBalance(String accountType, String accountId, String currencyName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;
        String table = getTableNameFromAccountTypeStr(accountType);

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + table + " WHERE accountType=?,accountId=?,currencyName=?;");
            preparedStatement.setString(1, accountType);
            preparedStatement.setString(2, accountId);
            preparedStatement.setString(3, currencyName);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                double defaultBalance = instance.getEconomyManager().getCurrency(currencyName).getDefaultBalance();
                setBalance(accountType, accountId, currencyName, defaultBalance);
                return defaultBalance;
            }

            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            }
        } catch (SQLException | InvalidCurrencyException exception) {
            exception.printStackTrace();
            instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to getBalance for accountType '" + accountType + "', accountId '" + accountId + "', currencyName '" + currencyName + "'. Stack trace:");
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
                instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to close SQLConnection for getBalance with accountType '" + accountType + "', accountId '" + accountId + "', currencyName '" + currencyName + "'. Stack trace:");
                exception.printStackTrace();
            }
        }

        return 0;
    }

    public void setBalance(String accountType, String accountId, String currencyName, double newBalance) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String table = getTableNameFromAccountTypeStr(accountType);

        try {
            connection = getConnection();
            switch (getDatabaseType()) {
                case MYSQL:
                    preparedStatement = connection.prepareStatement("INSERT INTO " + table + " (accountType,accountId,currencyName,balance) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE balance=?;"); //Thanks to Hugo5551 for providing this command.
                    break;
                case SQLITE:
                    preparedStatement = connection.prepareStatement("INSERT INTO " + table + " (accountType,accountId,currencyName,balance) VALUES (?,?,?,?) ON CONFLICT(accountType,accountId,currencyName) DO UPDATE SET balance=?;"); //Thanks to Hugo5551 for providing this command.
                    break;
                default:
                    preparedStatement.close();
                    connection.close();
                    throw new IllegalStateException("Unknown database type " + getDatabaseType().toString());
            }

            preparedStatement.setString(1, accountType);
            preparedStatement.setString(2, accountId);
            preparedStatement.setString(3, currencyName);
            preparedStatement.setDouble(4, newBalance);
            preparedStatement.setDouble(5, newBalance);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to setBalance for accountType '" + accountType + "', accountId '" + accountId + "', currencyName '" + currencyName + "', balance '" + newBalance + "'. Stack trace:");
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
                instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to close SQLConnection for setBalance with accountType '" + accountType + "', accountId '" + accountId + "', currencyName '" + currencyName + "', balance '" + newBalance + "'. Stack trace:");
                exception.printStackTrace();
            }
        }
    }

    public HashMap<UUID, Double> getBaltopMap(Currency currency) throws SQLException {
        if (baltopMap.size() == 0) {
            Connection connection = getConnection();
            String table = getTableNameFromAccountTypeStr("PlayerAccount");
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + table + " WHERE accountType='PlayerAccount',currencyName=?;");
            preparedStatement.setString(1, currency.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("accountId"));
                double balance = resultSet.getDouble("balance");
                baltopMap.put(uuid, balance);
            }

            close(connection, preparedStatement, resultSet);
        }

        return baltopMap;
    }

    public double getBaltopServerTotal(Currency currency) throws SQLException {
        if (serverTotal == -1) {
            serverTotal = 0.0; //This value is shown if nobody has a balance yet
            connection = getConnection();
            String table = getTableNameFromAccountTypeStr("PlayerAccount");
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT SUM(balance) FROM " + table + " WHERE currencyName=?;");
            preparedStatement.setString(1, currency.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                serverTotal = resultSet.getDouble("balance");
            }

            close(connection, preparedStatement, resultSet);
        }

        return serverTotal;
    }

    public boolean hasAccount(String accountType, String accountId) {
        boolean hasAccount = false;
        Connection connection = getConnection();
        String table = getTableNameFromAccountTypeStr(accountType);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE accountType=?,accountId=?;");
            preparedStatement.setString(1, accountType);
            preparedStatement.setString(2, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                hasAccount = resultSet.getString("accountId") != null;
                close(connection, preparedStatement, resultSet);
            }
        } catch (SQLException exception) {
            instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7A database error has occured whilst running hasAccount. Details: accountType = '" + accountType + "', accountId = '" + accountId + "', table = '" + table + "'. Stack trace:");
            exception.printStackTrace();
        }

        return hasAccount;
    }

    public void createAccount(String accountType, String accountId) throws InvalidCurrencyException {
        for (Currency currency : instance.getEconomyManager().getEnabledCurrencies()) {
            setBalance(accountType, accountId, currency.getName(), currency.getDefaultBalance());
        }
    }

    public void clearBaltopCacheAndServerTotal() {
        baltopMap.clear();
        serverTotal = -1;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException exception) {
            instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to close the SQL connection. Stack trace:");
            exception.printStackTrace();
        }
    }

    public void close(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException exception) {
            instance.getPhantomLogger().log(LogLevel.SEVERE, "&cDatabase Error: &7An SQLException occured whilst trying to close PreparedStatement and ResultSet. Stack trace:");
            exception.printStackTrace();
        }
    }
}
