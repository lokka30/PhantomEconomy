package io.github.lokka30.phantomeconomy_v2.databases;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDatabase {

    private PhantomEconomy instance;
    private Connection connection;

    public MySQLDatabase(PhantomEconomy instance) {
        this.instance = instance;
    }

    public void load() {
        openConnection();
        initialize();
    }

    public void openConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                //TODO MySQLDatabase Warning: openConnection was called but the connection is already open
                return;
            }
        } catch (SQLException exception) {
            //TODO MySQLDatabase Error: Unable to check if connection is already available in openConnection
            return;
        }

        synchronized (this) {
            try {
                if (connection != null && !connection.isClosed()) {
                    //TODO MySQLDatabase Warning: openConnection was called but the connection is already open
                    return;
                }
            } catch (SQLException exception) {
                //TODO MySQLDatabase Error: Unable to check if connection is already available in openConnection
                return;
            }

            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException exception) {
                //TODO MySQLDatabase Error: MySQL JDBC driver is not installed, you must have it installed to use this database.
                return;
            }

            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + instance.fileCache.SETTINGS_DATABASE_MYSQL_HOST + ":" + instance.fileCache.SETTINGS_DATABASE_MYSQL_PORT + "/" + instance.fileCache.SETTINGS_DATABASE_MYSQL_DATABASE, instance.fileCache.SETTINGS_DATABASE_MYSQL_USERNAME, instance.fileCache.SETTINGS_DATABASE_MYSQL_PASSWORD);
            } catch (SQLException exception) {
                //TODO MySQLDatabase Error: Unable to establish connection to MySQL database. Ensure that you have entered the correct details in the MySQL configuration section in the settings file.
            }
        }
    }

    public void initialize() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS phantomeconomy (`accounttype` varchar(32) NOT NULL, `accountid` varchar(32) NOT NULL, `currencyname` varchar(32) NOT NULL, `balance` double(64) NOT NULL, PRIMARY KEY (`accountid`));");
            statement.close();
        } catch (SQLException exception) {
            //TODO MySQLDatabase Error: Unable to create statement in initialize method.
        }
    }
}
