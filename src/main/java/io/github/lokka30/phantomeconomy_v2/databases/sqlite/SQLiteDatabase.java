package io.github.lokka30.phantomeconomy_v2.databases.sqlite;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabase extends Database {

    Connection connection;
    private PhantomEconomy instance;

    public SQLiteDatabase(PhantomEconomy instance) {
        super(instance);
        this.instance = instance;
    }

    @Override
    public Connection getSQLConnection() {
        File dataFile = new File(instance.getDataFolder(), "database.db");

        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile()) {
                    instance.utils.log(LogLevel.SEVERE, "Unable to create file 'database.db'");
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + instance.getDataFolder());
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void load() {
        connection = getSQLConnection();
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + instance.fileCache.SETTINGS_DATABASE_TABLE + " (`accounttype` varchar(32) NOT NULL,`currency` varchar(32) NOT NULL,`identifier` varchar(32) NOT NULL,`balance` double(48) NOT NULL,PRIMARY KEY (`accounttype`));");
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
