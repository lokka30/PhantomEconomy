package io.github.lokka30.phantomeconomy_v2.database;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private PhantomEconomy instance;
    private Connection connection;

    public DatabaseManager(final PhantomEconomy instance) {
        this.instance = instance;
        if (instance.fileCache.SETTINGS_DATABASE_USE_MYSQL) {
            try {
                openConnection();
            } catch (SQLException e) {
                instance.utils.log(LogLevel.SEVERE, "An SQLException occured whilst trying to open a connection to the MySQL database, check all details in the settings file.");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                instance.utils.log(LogLevel.SEVERE, "ClassNotFoundException occured whilst trying to open a connection to the MySQL database, make sure your system has the proper MySQL driver running.");
                e.printStackTrace();
            }
        }
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + instance.fileCache.SETTINGS_DATABASE_MYSQL_HOST + ":" + instance.fileCache.SETTINGS_DATABASE_MYSQL_PORT + "/" + instance.fileCache.SETTINGS_DATABASE_MYSQL_DATABASE, instance.fileCache.SETTINGS_DATABASE_MYSQL_USERNAME, instance.fileCache.SETTINGS_DATABASE_MYSQL_PASSWORD);
        }
    }

    //TODO
}
