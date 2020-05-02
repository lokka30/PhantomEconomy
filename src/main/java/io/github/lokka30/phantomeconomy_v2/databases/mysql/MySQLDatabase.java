package io.github.lokka30.phantomeconomy_v2.databases.mysql;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;
import org.bukkit.OfflinePlayer;

import java.sql.*;

public class MySQLDatabase {

    //todo add towny support

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
                PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS balance ( currency VARCHAR(32) NOT NULL, uuid VARCHAR(32) NOT NULL, amount DOUBLE(32) NOT NULL, UNIQUE (uuid));");
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

    public double getBalance(Currency currency, OfflinePlayer offlinePlayer) {
        PreparedStatement preparedStatement = null;
        double balance = 0.00;
        try {
            preparedStatement = connection.prepareStatement("SELECT amount FROM balance WHERE currency = ?,uuid = ?");
            preparedStatement.setString(1, currency.getName());
            preparedStatement.setString(2, offlinePlayer.getUniqueId().toString());
            ResultSet result = preparedStatement.executeQuery();
            if (result == null) {
                setBalance(currency, offlinePlayer, 0.00D);
            } else {
                while (result.next()) {
                    balance = result.getDouble("amount");
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

    public void setBalance(Currency currency, OfflinePlayer offlinePlayer, double balance) {
        PreparedStatement preparedStatement1 = null;
        PreparedStatement preparedStatement2 = null;
        try {
            String query = "UPDATE balance SET amount = ? WHERE currency = ?,uuid = ?;";
            preparedStatement1 = connection.prepareStatement(query);
            preparedStatement1.setDouble(1, balance);
            preparedStatement1.setString(2, currency.getName());
            preparedStatement1.setString(3, offlinePlayer.getUniqueId().toString());
            int changed = preparedStatement1.executeUpdate();
            if (changed == 0) {
                String query2 = "INSERT INTO balance (currency, uuid, amount) VALUES (?, ?, ?);";
                preparedStatement2 = connection.prepareStatement(query2);
                preparedStatement2.setString(1, currency.getName());
                preparedStatement2.setString(2, offlinePlayer.getUniqueId().toString());
                preparedStatement2.setDouble(3, balance);
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
