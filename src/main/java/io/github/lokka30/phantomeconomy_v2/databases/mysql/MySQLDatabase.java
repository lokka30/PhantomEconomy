package io.github.lokka30.phantomeconomy_v2.databases.mysql;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy_v2.api.accounts.TownyAccount;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;

import java.sql.*;

public class MySQLDatabase {

    public Connection connection;
    public String username;
    public String password;
    public String database;
    public String host;
    public Integer port;
    public String use_ssl;
    public String url;
    private PhantomEconomy instance;

    public MySQLDatabase(PhantomEconomy instance) {
        this.instance = instance;
    }

    public void updateSettings() {
        this.username = instance.fileCache.SETTINGS_DATABASE_MYSQL_USERNAME;
        this.password = instance.fileCache.SETTINGS_DATABASE_MYSQL_PASSWORD;
        this.database = instance.fileCache.SETTINGS_DATABASE_MYSQL_DATABASE;
        this.port = instance.fileCache.SETTINGS_DATABASE_MYSQL_PORT;
        this.host = instance.fileCache.SETTINGS_DATABASE_MYSQL_HOST;
        this.use_ssl = instance.fileCache.SETTINGS_DATABASE_MYSQL_SSL;
    }

    public void startSQLConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            instance.utils.log(LogLevel.SEVERE, "MySQL JDBC Driver unavailable");
            exception.printStackTrace();
            return;
        }
        try {
            url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + use_ssl;
            connection = DriverManager.getConnection(url, username, password);
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + instance.fileCache.SETTINGS_DATABASE_TABLE + " (accounttype VARCHAR(32) NOT NULL, currency VARCHAR(32) NOT NULL, identifier VARCHAR(32) NOT NULL, balance DOUBLE(48) NOT NULL, UNIQUE (identifier));");
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        } catch (SQLException exception) {
            if (exception.getSQLState().equals("28000")) {
                instance.utils.log(LogLevel.SEVERE, "MySQL login information is wrong, check settings");
            } else if (exception.getSQLState().equals("08S01")) {
                instance.utils.log(LogLevel.SEVERE, "Coudln't establish a connection to mysql");
            } else {
                instance.utils.log(LogLevel.SEVERE, "MySQL error: " + exception.getSQLState());
                exception.printStackTrace();
            }
        }
    }

    public void stopSQLConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public double getBalance(Currency currency, PlayerAccount playerAccount) {
        PreparedStatement preparedStatement = null;
        double balance = 0.00;
        try {
            preparedStatement = connection.prepareStatement("SELECT balance FROM " + instance.fileCache.SETTINGS_DATABASE_TABLE + " WHERE accounttype = ?,currency = ?,identifier = ?;");
            preparedStatement.setString(1, "PlayerAccount");
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, playerAccount.getUUIDStr());
            ResultSet result = preparedStatement.executeQuery();
            if (result == null) {
                setBalance(currency, playerAccount, 0.00D); //TODO DEFAULT BALANCE INSTEAD OF 0.00D
            } else {
                while (result.next()) {
                    balance = result.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return balance;
    }

    public void setBalance(Currency currency, PlayerAccount playerAccount, double balance) {
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        try {
            String query = "UPDATE " + instance.fileCache.SETTINGS_DATABASE_TABLE + " SET balance = ? WHERE accounttype = ?,currency = ?,identifier = ?;";
            preparedStatement1 = connection.prepareStatement(query);
            preparedStatement1.setDouble(1, balance);
            preparedStatement1.setString(2, "PlayerAccount");
            preparedStatement1.setString(3, currency.getName());
            preparedStatement1.setString(4, playerAccount.getUUIDStr());
            int changed = preparedStatement1.executeUpdate();
            if (changed == 0) {
                String query2 = "INSERT INTO " + instance.fileCache.SETTINGS_DATABASE_TABLE + " (accounttype, currency, identifier, amount) VALUES (?, ?, ?, ?);";
                preparedStatement2 = connection.prepareStatement(query2);
                preparedStatement2.setString(1, "PlayerAccount");
                preparedStatement2.setString(2, currency.getName());
                preparedStatement2.setString(3, playerAccount.getUUIDStr());
                preparedStatement2.setDouble(4, balance);
                preparedStatement2.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (preparedStatement1 != null)
                    preparedStatement1.close();
                if (preparedStatement2 != null)
                    preparedStatement2.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    public double getBalance(Currency currency, TownyAccount townyAccount) {
        PreparedStatement preparedStatement = null;
        double balance = 0.00;
        try {
            preparedStatement = connection.prepareStatement("SELECT balance FROM " + instance.fileCache.SETTINGS_DATABASE_TABLE + " WHERE accounttype = ?,currency = ?,identifier = ?;");
            preparedStatement.setString(1, "TownyAccount");
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, townyAccount.getName());
            ResultSet result = preparedStatement.executeQuery();
            if (result == null) {
                setBalance(currency, townyAccount, 0.00D); //TODO DEFAULT BALANCE INSTEAD OF 0.00D
            } else {
                while (result.next()) {
                    balance = result.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
        return balance;
    }

    public void setBalance(Currency currency, TownyAccount townyAccount, double balance) {
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        try {
            String query = "UPDATE " + instance.fileCache.SETTINGS_DATABASE_TABLE + " SET balance = ? WHERE accounttype = ?,currency = ?,identifier = ?;";
            preparedStatement1 = connection.prepareStatement(query);
            preparedStatement1.setDouble(1, balance);
            preparedStatement1.setString(2, "TownyAccount");
            preparedStatement1.setString(3, currency.getName());
            preparedStatement1.setString(4, townyAccount.getName());
            int changed = preparedStatement1.executeUpdate();
            if (changed == 0) {
                String query2 = "INSERT INTO " + instance.fileCache.SETTINGS_DATABASE_TABLE + " (accounttype, currency, identifier, amount) VALUES (?, ?, ?, ?);";
                preparedStatement2 = connection.prepareStatement(query2);
                preparedStatement2.setString(1, "TownyAccount");
                preparedStatement2.setString(2, currency.getName());
                preparedStatement2.setString(3, townyAccount.getName());
                preparedStatement2.setDouble(4, balance);
                preparedStatement2.executeUpdate();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (preparedStatement1 != null)
                    preparedStatement1.close();
                if (preparedStatement2 != null)
                    preparedStatement2.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }
}
