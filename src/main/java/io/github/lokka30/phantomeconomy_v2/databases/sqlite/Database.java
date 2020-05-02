package io.github.lokka30.phantomeconomy_v2.databases.sqlite;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy_v2.api.accounts.TownyAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class Database {

    PhantomEconomy instance;
    String table;

    public Database(final PhantomEconomy instance) {
        this.instance = instance;
        this.table = instance.fileCache.SETTINGS_DATABASE_TABLE;
    }

    public abstract void load();

    public abstract Connection getSQLConnection();

    public double getBalance(String currencyName, PlayerAccount playerAccount) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet;

        try {
            connection = getSQLConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE accounttype = ?, currency = ?, identifier = ?;");
            statement.setString(1, "PlayerAccount");
            statement.setString(2, currencyName);
            statement.setString(3, playerAccount.getUUIDStr());
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("identifier").equals(playerAccount.getUUIDStr())) {
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

    public void setBalance(String currencyName, PlayerAccount playerAccount) {
        Connection connection = null;
        PreparedStatement statement = null;
        double balance = 0.00;
        try {
            connection = getSQLConnection();
            statement = connection.prepareStatement("REPLACE INTO " + table + " (accounttype,currency,identifier,balance) VALUES(?,?,?,?)");
            statement.setString(1, "PlayerAccount");
            statement.setString(2, currencyName);
            statement.setString(3, playerAccount.getUUIDStr());
            statement.setDouble(4, balance);
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

    public double getBalance(String currencyName, TownyAccount townyAccount) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet;

        try {
            connection = getSQLConnection();
            statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE accounttype = ?, currency = ?, identifier = ?;");
            statement.setString(1, "TownyAccount");
            statement.setString(2, currencyName);
            statement.setString(3, townyAccount.getName());
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString("identifier").equals(townyAccount.getName())) {
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

    public void setBalance(String currencyName, TownyAccount townyAccount) {
        Connection connection = null;
        PreparedStatement statement = null;
        double balance = 0.00;
        try {
            connection = getSQLConnection();
            statement = connection.prepareStatement("REPLACE INTO " + table + " (accounttype,currency,identifier,balance) VALUES(?,?,?,?)");
            statement.setString(1, "TownyAccount");
            statement.setString(2, currencyName);
            statement.setString(3, townyAccount.getName());
            statement.setDouble(4, balance);
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
