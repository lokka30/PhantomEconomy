package io.github.lokka30.phantomeconomy.cache;

import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy.enums.DatabaseType;
import io.github.lokka30.phantomlib.enums.LogLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileCache {

    public DatabaseType databaseType;

    public boolean SETTINGS_OTHER_USE_UPDATE_CHECKER;
    public String SETTINGS_DATABASE_TYPE;
    public String SETTINGS_DATABASE_MYSQL_HOST;
    public String SETTINGS_DATABASE_MYSQL_DATABASE;
    public String SETTINGS_DATABASE_MYSQL_USERNAME;
    public String SETTINGS_DATABASE_MYSQL_PASSWORD;
    public String SETTINGS_DATABASE_MYSQL_SSL;
    public int SETTINGS_DATABASE_MYSQL_PORT;
    public String SETTINGS_DATABASE_TABLE; //TODO REMOVE
    public String SETTINGS_DATABASE_TABLE_PLAYERACCOUNT;
    public String SETTINGS_DATABASE_TABLE_NONPLAYERACCOUNT;
    public String SETTINGS_DATABASE_TABLE_BANKACCOUNT;
    public List<String> SETTINGS_CURRENCIES_ENABLED_CURRENCIES;
    public String SETTINGS_DEFAULT_CURRENCY;
    public String SETTINGS_VAULT_CURRENCY;
    public boolean SETTINGS_STARTUP_TASKS_CLEAR_BALTOP_CACHE;
    public boolean SETTINGS_STARTUP_TASKS_CLEAR_PLAYER_ACCOUNT_CACHE;
    public boolean SETTINGS_STARTUP_TASKS_CLEAR_NON_PLAYER_ACCOUNT_CACHE;
    public boolean SETTINGS_STARTUP_TASKS_CLEAR_BANK_ACCOUNT_CACHE;

    public HashMap<String, Double> SETTINGS_CURRENCY_DEFAULT_BALANCE_MAP;
    public HashMap<String, String> SETTINGS_CURRENCY_FORMATTING_DECIMAL_FORMAT_MAP, SETTINGS_CURRENCY_FORMATTING_FINAL_FORMAT_MAP, SETTINGS_CURRENCY_FORMATTING_WORDS_SINGULAR_MAP, SETTINGS_CURRENCY_FORMATTING_WORDS_PLURAL_MAP;

    private PhantomEconomy instance;

    public FileCache(final PhantomEconomy instance) {
        this.instance = instance;
    }

    //Load or reload values from files
    public void loadFromFiles() {
        FlatFile settings = instance.getSettings();

        @SuppressWarnings("unused")
        FlatFile messages = instance.getMessages(); //TODO

        SETTINGS_OTHER_USE_UPDATE_CHECKER = settings.get("other-options.use-update-checker", true);
        SETTINGS_DATABASE_TYPE = settings.get("database.database-type", "SQLite");
        switch (SETTINGS_DATABASE_TYPE.toLowerCase()) {
            case "sqlite":
                databaseType = DatabaseType.SQLITE;
                break;
            case "mysql":
                databaseType = DatabaseType.MYSQL;
                break;
            default:
                instance.getPhantomLogger().log(LogLevel.SEVERE, "Invalid database type set in the settings file! Temporarily using SQLite. Fix this as soon as possible!");
                databaseType = DatabaseType.SQLITE;
                break;
        }
        SETTINGS_DATABASE_MYSQL_HOST = settings.get("database.mysql.host", "localhost");
        SETTINGS_DATABASE_MYSQL_DATABASE = settings.get("database.mysql.database", "minecraft");
        SETTINGS_DATABASE_MYSQL_USERNAME = settings.get("database.mysql.username", "root");
        SETTINGS_DATABASE_MYSQL_PASSWORD = settings.get("database.mysql.password", "password");
        SETTINGS_DATABASE_MYSQL_SSL = settings.get("database.mysql.ssl", "true");
        SETTINGS_DATABASE_MYSQL_PORT = settings.get("database.mysql.port", 3306);
        SETTINGS_DATABASE_TABLE = settings.get("database.table", "phantomeconomy");
        SETTINGS_DATABASE_TABLE_PLAYERACCOUNT = settings.get("database.account-tables.playerAccount", "phantomeconomy_global");
        SETTINGS_DATABASE_TABLE_NONPLAYERACCOUNT = settings.get("database.account-tables.nonPlayerAccount", "phantomeconomy_global");
        SETTINGS_DATABASE_TABLE_BANKACCOUNT = settings.get("database.account-tables.bankAccount", "phantomeconomy_global");
        SETTINGS_CURRENCIES_ENABLED_CURRENCIES = settings.get("currencies.enabled-currencies", null);
        SETTINGS_DEFAULT_CURRENCY = settings.get("default-currency", "invalidSetting");
        SETTINGS_VAULT_CURRENCY = settings.get("vault-currency", "invalidSetting");
        SETTINGS_STARTUP_TASKS_CLEAR_BALTOP_CACHE = settings.get("startup-tasks.clear-baltop-cache", true);
        SETTINGS_STARTUP_TASKS_CLEAR_PLAYER_ACCOUNT_CACHE = settings.get("startup-tasks.clear-player-account-cache", false);
        SETTINGS_STARTUP_TASKS_CLEAR_NON_PLAYER_ACCOUNT_CACHE = settings.get("startup-tasks.clear-non-player-account-cache", true);
        SETTINGS_STARTUP_TASKS_CLEAR_BANK_ACCOUNT_CACHE = settings.get("startup-tasks.clear-bank-account-cache", true);

        SETTINGS_CURRENCY_DEFAULT_BALANCE_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_DECIMAL_FORMAT_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_FINAL_FORMAT_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_WORDS_SINGULAR_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_WORDS_PLURAL_MAP = new HashMap<>();
        for (String currencyName : settings.get("currencies.enabled-currencies", new ArrayList<String>())) {
            final String path = "currencies.currency-settings." + currencyName + ".";
            try {
                instance.getCurrencyManager().getCurrency(currencyName);
            } catch (InvalidCurrencyException exception) {
                instance.getPhantomLogger().log(LogLevel.SEVERE, "Currency '" + currencyName + "' was listed in 'enabled currencies' in the settings file, but the currency doesn't exist.");
                continue;
            }

            SETTINGS_CURRENCY_DEFAULT_BALANCE_MAP.put(currencyName, settings.get(path + "default-balance", 0.00));
            SETTINGS_CURRENCY_FORMATTING_DECIMAL_FORMAT_MAP.put(currencyName, settings.get(path + "formatting.decimal-format", "0.00"));
            SETTINGS_CURRENCY_FORMATTING_FINAL_FORMAT_MAP.put(currencyName, settings.get(path + "formatting.final-format", "$%balance%"));
            SETTINGS_CURRENCY_FORMATTING_WORDS_SINGULAR_MAP.put(currencyName, settings.get(path + "formatting.words.singular", "dollar"));
            SETTINGS_CURRENCY_FORMATTING_WORDS_PLURAL_MAP.put(currencyName, settings.get(path + "formatting.words.plural", "dollars"));
        }
    }
}
