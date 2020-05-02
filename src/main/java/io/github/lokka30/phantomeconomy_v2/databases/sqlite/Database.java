package io.github.lokka30.phantomeconomy_v2.databases.sqlite;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class Database {

    PhantomEconomy instance;
    String table;
    Connection connection;

    public Database(final PhantomEconomy instance) {
        this.instance = instance;
        this.table = instance.fileCache.SETTINGS_DATABASE_TABLE;
    }

    public abstract void load();

    public abstract Connection getSQLConnection();

    public double getBalance(String currencyName, UUID uuid) {
        Connection connection = null;
        final String uuidStr = uuid.toString();
        PreparedStatement statement = null;
        ResultSet resultSet;

        try {
            connection = getSQLConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE currency = ?, uuid = ?;");
            statement.setString(1, currencyName);
            statement.setString(2, uuidStr);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("uuid").equals(uuidStr)) {
                    return resultSet.getDouble("balance");
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

        return 0.00;
    }

    public void setBalance(String currencyName, UUID uuid) {
        Connection connection = null;
        PreparedStatement statement = null;
        final String uuidStr = uuid.toString();
        double balance = 0.00;
        try {
            connection = getSQLConnection();
            statement = connection.prepareStatement("REPLACE INTO " + table + " (currency,uuid,balance) VALUES(?,?,?)");
            statement.setString(1, currencyName);
            statement.setString(2, uuidStr);
            statement.setDouble(3, balance);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }
}
