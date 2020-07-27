package io.github.lokka30.phantomeconomy.databases;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.accounts.BankAccount;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy.enums.AccountType;
import io.github.lokka30.phantomeconomy.enums.DatabaseType;
import io.github.lokka30.phantomlib.enums.LogLevel;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                            instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7Unable to create data folder");
                            return null;
                        }
                    }

                    if (!databaseFile.exists()) {
                        try {
                            if (databaseFile.createNewFile()) {
                                instance.getPhantomLogger().log(LogLevel.INFO, "&b&lPhantomEconomy: &7", "File &bdatabase.db&7 didn't exist, it has now been created.");
                            }
                        } catch (IOException exception) {
                            instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7Unable to create database file");
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
                            instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7Unable to connect to the SQLite database - You do not have the SQLite JDBC library installed.");
                            e.printStackTrace();
                            return null;
                        }

                        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
                    } catch (SQLException e) {
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7An SQLException occured whilst trying to connect to the SQLite database. Stack trace:");
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
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7Unable to check if connection is already available in synch getsqlconn");
                        return null;
                    }

                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                    } catch (ClassNotFoundException exception) {
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7 MySQL JDBC driver is not installed, you must have it installed to use this database.");
                        return null;
                    }

                    try {
                        connection = DriverManager.getConnection("jdbc:mysql://" + instance.getFileCache().SETTINGS_DATABASE_MYSQL_HOST + ":" + instance.getFileCache().SETTINGS_DATABASE_MYSQL_PORT + "/" + instance.getFileCache().SETTINGS_DATABASE_MYSQL_DATABASE, instance.getFileCache().SETTINGS_DATABASE_MYSQL_USERNAME, instance.getFileCache().SETTINGS_DATABASE_MYSQL_PASSWORD);
                    } catch (SQLException exception) {
                        instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7Unable to establish connection to MySQL database. Ensure that you have entered the correct details in the MySQL configuration section in the settings file.");
                    }

                    return connection;
                }
            default:
                throw new IllegalStateException("Invalid database type " + getConnection().toString());
        }
    }

    public void load() throws SQLException, InvalidCurrencyException {
        connection = getConnection();

        for (Currency currency : instance.getCurrencyManager().getEnabledCurrencies()) {
            Statement statement1 = connection.createStatement();
            statement1.executeUpdate("CREATE TABLE IF NOT EXISTS PlayerAccount" + instance.getFileCache().SETTINGS_DATABASE_TABLES_ACCOUNT_TYPE_SUFFIXES_PLAYERACCOUNT + "_" + instance.getFileCache().SETTINGS_DATABASE_TABLES_CURRENCY_SUFFIXES_MAP.get(currency.getName()) + "('accountId' VARCHAR(48) NOT NULL, 'currencyName' VARCHAR(48) NOT NULL, 'balance' DECIMAL(48,2) NOT NULL, PRIMARY KEY('accountId', 'currencyName'));");
            statement1.close();

            Statement statement2 = connection.createStatement();
            statement2.executeUpdate("CREATE TABLE IF NOT EXISTS NonPlayerAccount" + instance.getFileCache().SETTINGS_DATABASE_TABLES_ACCOUNT_TYPE_SUFFIXES_NONPLAYERACCOUNT + "_" + instance.getFileCache().SETTINGS_DATABASE_TABLES_CURRENCY_SUFFIXES_MAP.get(currency.getName()) + "('accountId' VARCHAR(48) NOT NULL, 'currencyName' VARCHAR(48) NOT NULL, 'balance' DECIMAL(48,2) NOT NULL, PRIMARY KEY('accountId', 'currencyName'));");
            statement2.close();

            Statement statement3 = connection.createStatement();
            statement3.executeUpdate("CREATE TABLE IF NOT EXISTS BankAccount" + instance.getFileCache().SETTINGS_DATABASE_TABLES_ACCOUNT_TYPE_SUFFIXES_BANKACCOUNT + "_" + instance.getFileCache().SETTINGS_DATABASE_TABLES_CURRENCY_SUFFIXES_MAP.get(currency.getName()) + "('accountId' VARCHAR(48) NOT NULL, 'currencyName' VARCHAR(48) NOT NULL, 'balance' DECIMAL(48,2) NOT NULL, 'ownerAccountType' VARCHAR(48), 'ownerId' VARCHAR(48), PRIMARY KEY('accountId', 'currencyName'));");
            statement3.close();
        }

        if (connection != null) {
            connection.close();
        }
    }

    public String getTableName(AccountType accountType, String currencyName) {
        String accountTypeTablePrefix;

        switch (accountType) {
            case PlayerAccount:
                accountTypeTablePrefix = accountType.toString() + instance.getFileCache().SETTINGS_DATABASE_TABLES_ACCOUNT_TYPE_SUFFIXES_PLAYERACCOUNT;
                break;
            case NonPlayerAccount:
                accountTypeTablePrefix = accountType.toString() + instance.getFileCache().SETTINGS_DATABASE_TABLES_ACCOUNT_TYPE_SUFFIXES_NONPLAYERACCOUNT;
                break;
            case BankAccount:
                accountTypeTablePrefix = accountType.toString() + instance.getFileCache().SETTINGS_DATABASE_TABLES_ACCOUNT_TYPE_SUFFIXES_BANKACCOUNT;
                break;
            default:
                instance.getPhantomLogger().log(LogLevel.SEVERE, "&b&lPhantomEconomy: &7", "&cDatabase Error: &7Unexpected account type '" + accountType.toString() + "'! using fallback NonPlayerAccount. this should be fixed immediately!");
                accountTypeTablePrefix = AccountType.NonPlayerAccount.toString() + instance.getFileCache().SETTINGS_DATABASE_TABLES_ACCOUNT_TYPE_SUFFIXES_NONPLAYERACCOUNT;
                break;
        }

        return accountTypeTablePrefix + "_" + instance.getFileCache().SETTINGS_DATABASE_TABLES_CURRENCY_SUFFIXES_MAP.get(currencyName);
    }

    public double getBalance(AccountType accountType, String accountId, String currencyName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;
        String table = getTableName(accountType, currencyName);

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + table + " WHERE accountId=? AND currencyName=?;");
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, currencyName);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            } else {
                double defaultBalance = instance.getCurrencyManager().getCurrency(currencyName).getDefaultBalance();
                setBalance(accountType, accountId, currencyName, defaultBalance);
                return defaultBalance;
            }
        } catch (SQLException | InvalidCurrencyException exception) {
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
                exception.printStackTrace();
            }
        }

        return 0;
    }

    public void setBalance(AccountType accountType, String accountId, String currencyName, double newBalance) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String table = getTableName(accountType, currencyName);

        try {
            connection = getConnection();
            switch (getDatabaseType()) {
                case MYSQL:
                    preparedStatement = connection.prepareStatement("INSERT INTO " + table + " (accountId,currencyName,balance) VALUES (?,?,?) ON DUPLICATE KEY UPDATE balance=?;");
                    break;
                case SQLITE:
                    preparedStatement = connection.prepareStatement("INSERT INTO " + table + " (accountId,currencyName,balance) VALUES (?,?,?) ON CONFLICT(accountId,currencyName) DO UPDATE SET balance=?;");
                    break;
                default:
                    preparedStatement.close();
                    connection.close();
                    throw new IllegalStateException("Unknown database type " + getDatabaseType().toString());
            }

            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, currencyName);
            preparedStatement.setDouble(3, newBalance);
            preparedStatement.setDouble(4, newBalance);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
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
                exception.printStackTrace();
            }
        }
    }

    public double getBankBalance(String accountId, String currencyName, AccountType ownerAccountType, String ownerId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet;
        String table = getTableName(AccountType.BankAccount, currencyName);

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement("SELECT * FROM " + table + " WHERE accountId=? AND currencyName=? AND ownerAccountType=? AND ownerId=?;");
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, currencyName);
            preparedStatement.setString(3, ownerAccountType.toString());
            preparedStatement.setString(4, ownerId);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            } else {
                double defaultBalance = instance.getCurrencyManager().getCurrency(currencyName).getDefaultBalance();
                setBankBalance(accountId, currencyName, defaultBalance, ownerAccountType, ownerId);
                return defaultBalance;
            }
        } catch (SQLException | InvalidCurrencyException exception) {
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
                exception.printStackTrace();
            }
        }

        return 0;
    }

    public void setBankBalance(String accountId, String currencyName, double newBalance, AccountType ownerAccountType, String ownerId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String table = getTableName(AccountType.BankAccount, currencyName);

        try {
            connection = getConnection();
            switch (getDatabaseType()) {
                case MYSQL:
                    preparedStatement = connection.prepareStatement("INSERT INTO " + table + " (accountId,currencyName,balance,ownerAccountType,ownerId) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE balance=?;");
                    break;
                case SQLITE:
                    preparedStatement = connection.prepareStatement("INSERT INTO " + table + " (accountId,currencyName,balance,ownerAccountType,ownerId) VALUES (?,?,?,?,?) ON CONFLICT(accountType,accountId,currencyName,ownerAccountType,ownerId) DO UPDATE SET balance=?;");
                    break;
                default:
                    preparedStatement.close();
                    connection.close();
                    throw new IllegalStateException("Unexpected database type " + getDatabaseType().toString());
            }

            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, currencyName);
            preparedStatement.setDouble(3, newBalance);
            preparedStatement.setDouble(4, newBalance);
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
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
                exception.printStackTrace();
            }
        }
    }

    public HashMap<UUID, Double> getBaltopMap(Currency currency) throws SQLException {
        if (baltopMap.size() == 0) {
            Connection connection = getConnection();
            String table = getTableName(AccountType.PlayerAccount, currency.getName());
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + table + " WHERE currencyName=?;");
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
            String table = getTableName(AccountType.PlayerAccount, currency.getName());
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

    public boolean hasAccount(AccountType accountType, String accountId, Currency currency) {
        Connection connection = getConnection();
        String table = getTableName(accountType, currency.getName());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT accountId FROM " + table + " WHERE accountId=?;");
            preparedStatement.setString(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                connection.close();
                resultSet.close();
                return resultSet.getString("accountId") != null;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean hasBankAccount(String accountId, Currency currency, AccountType ownerAccountType, String ownerId) {
        Connection connection = getConnection();
        String table = getTableName(AccountType.BankAccount, currency.getName());

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT accountId FROM " + table + " WHERE accountId=? AND currencyName=? AND ownerAccountType=? AND ownerId=?;");
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, ownerAccountType.toString());
            preparedStatement.setString(4, ownerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                close(connection, preparedStatement, resultSet);
                return true;
            } else {
                return false;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public void createAccount(AccountType accountType, String accountId) throws InvalidCurrencyException {
        for (Currency currency : instance.getCurrencyManager().getEnabledCurrencies()) {
            setBalance(accountType, accountId, currency.getName(), currency.getDefaultBalance());
        }
    }

    public void createBankAccount(String accountId, AccountType ownerAccountType, String ownerId) throws InvalidCurrencyException {
        for (Currency currency : instance.getCurrencyManager().getEnabledCurrencies()) {
            setBankBalance(accountId, currency.getName(), currency.getDefaultBalance(), ownerAccountType, ownerId);
        }
    }

    /**
     * Self-explanatory. Resets the baltop map and server total so they will be calculated again.
     */
    public void clearBaltopCacheAndServerTotal() {
        baltopMap.clear();
        serverTotal = -1;
    }

    /**
     * Deletes all entries in the database with the specified account id.
     *
     * @param accountId the bank account to remove
     * @throws InvalidCurrencyException if the vault currency is unavailable
     * @throws SQLException             if a database error occurred
     */
    public void deleteBankAccount(final String accountId) throws InvalidCurrencyException, SQLException {
        Connection connection = getConnection();

        for (Currency currency : instance.getCurrencyManager().getEnabledCurrencies()) {
            String table = getTableName(AccountType.BankAccount, currency.getName());
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + table + " WHERE accountId=?;");
            preparedStatement.setString(1, accountId);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        }

        connection.close();
    }

    /**
     * STRICTLY FOR VAULT USE ONLY
     *
     * @param accountId the bank account id to lookup
     * @return bank account
     * @throws SQLException             if a database error occurred
     * @throws InvalidCurrencyException if the vault currency is unavailable
     */
    public BankAccount getBankAccountFromId(final String accountId) throws SQLException, InvalidCurrencyException {
        Connection connection = getConnection();
        BankAccount bankAccount = null;
        String table = getTableName(AccountType.BankAccount, instance.getCurrencyManager().getVaultCurrency().getName());
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + table + " WHERE accountId=?;");
        preparedStatement.setString(1, accountId);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            AccountType ownerAccountType = AccountType.valueOf(resultSet.getString(4));
            String ownerId = resultSet.getString(5);
            bankAccount = instance.getAccountManager().getBankAccount(accountId, ownerAccountType, ownerId);
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();

        return bankAccount;
    }

    /**
     * STRICTLY FOR VAULT USE ONLY
     *
     * @param bankAccountId  the id of the bank account (e.g. Jeffs Bank)
     * @param ownerAccountId the id of the owner (e.g. uuid or string)
     * @return if the player/nonplayer owns the bank account
     */
    public boolean isBankOwner(final String bankAccountId, final AccountType ownerAccountType, final String ownerAccountId) throws InvalidCurrencyException, SQLException {
        Connection connection = getConnection();
        String table = getTableName(AccountType.BankAccount, instance.getCurrencyManager().getVaultCurrency().getName());
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE accountId=? AND ownerAccountType=? AND ownerId=?;");
        preparedStatement.setString(1, bankAccountId);
        preparedStatement.setString(2, ownerAccountType.toString());
        preparedStatement.setString(3, ownerAccountId);
        ResultSet resultSet = preparedStatement.executeQuery();
        boolean isOwner = false;

        if (resultSet.next()) {
            isOwner = true;
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();
        return isOwner;
    }

    /**
     * STRICTLY FOR VAULT USE ONLY
     *
     * @return a list of bank ids in the database
     */
    public List<String> getBankAccounts() throws SQLException, InvalidCurrencyException {
        List<String> bankAccounts = new ArrayList<>();
        Connection connection = getConnection();
        String table = getTableName(AccountType.BankAccount, instance.getCurrencyManager().getVaultCurrency().getName());
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + table + ";");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            bankAccounts.add(resultSet.getString(1));
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();

        return bankAccounts;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException exception) {
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
            exception.printStackTrace();
        }
    }
}
