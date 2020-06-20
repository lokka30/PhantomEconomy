package io.github.lokka30.phantomeconomy_v2.databases;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class SQLiteDatabase {

    private PhantomEconomy instance;
    private Connection connection;

    HashMap<UUID, Double> baltopMap;
    double serverTotal;

    public SQLiteDatabase(PhantomEconomy instance) {
        this.instance = instance;
        baltopMap = new HashMap<>();
        serverTotal = -1;
    }

    public Connection getSQLConnection() {
        if (!instance.getDataFolder().exists()) {
            try {
                if (!instance.getDataFolder().createNewFile()) {
                    instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7Unable to create data folder");
                    return null;
                }
            } catch (IOException exception) {
                instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7Unable to create data folder for the SQLite database. Exception:");
                exception.printStackTrace();
                return null;
            }

            try {
                if (connection != null && !connection.isClosed()) {
                    return connection;
                }

                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7Unable to connect to the SQLite database - You do not have the SQLite JDBC library installed.");
                    e.printStackTrace();
                    return null;
                }

                connection = DriverManager.getConnection("jdbc:sqlite:" + instance.getDataFolder());
                return connection;
            } catch (SQLException e) {
                instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to connect to the SQLite database. Stack trace:");
                e.printStackTrace();
            }
        }

        return null;
    }

    public void load() {
        connection = getSQLConnection();

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS phantomeconomy (`accounttype` varchar(32) NOT NULL, `accountid` varchar(32) NOT NULL, `currencyname` varchar(32) NOT NULL, `balance` double(64) NOT NULL, PRIMARY KEY (`accountid`));");
            statement.close();
        } catch (SQLException exception) {
            instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to load the SQLite database. Stack trace:");
            exception.printStackTrace();
        }

        initialise();
    }

    public void initialise() {
        connection = getSQLConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM phantomeconomy WHERE accountid = ?");
            ResultSet resultSet = preparedStatement.executeQuery();
            close(preparedStatement, resultSet);
        } catch (SQLException exception) {
            instance.utils.log(LogLevel.SEVERE, "&cSQLiteDatabase Error: &7An SQLException occured whilst trying to initialise the SQLite database. Stack trace:");
            exception.printStackTrace();
        }
    }

    public double getBalance(String accountType, String accountId, String currencyName) {
        //TODO if the account doesn't have this currency set in the database, then set it with the default amount.

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;

        try {
            connection = getSQLConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM phantomeconomy WHERE accounttype = '" + accountType + "', accountid = '" + accountId + "', currencyname = '" + currencyName + "';");
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getString("accountid").equalsIgnoreCase(accountId)) {
                    return resultSet.getDouble("balance");
                }
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
            connection = getSQLConnection();
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

    public HashMap<UUID, Double> getBaltopMap() {
        if (baltopMap.size() == 0) {
            //TODO Set the baltop map from the database
        }

        return baltopMap;
    }

    public double getBaltopServerTotal() {
        if (serverTotal == -1) {
            //TODO Set the server total from the database
        }

        return serverTotal;
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
