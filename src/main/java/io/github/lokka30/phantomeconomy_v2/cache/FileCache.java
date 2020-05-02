package io.github.lokka30.phantomeconomy_v2.cache;

import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;

import java.util.List;

public class FileCache {

    public boolean SETTINGS_OTHER_USE_UPDATE_CHECKER;
    public String SETTINGS_DATABASE_TYPE;
    public String SETTINGS_DATABASE_MYSQL_HOST;
    public String SETTINGS_DATABASE_MYSQL_DATABASE;
    public String SETTINGS_DATABASE_MYSQL_USERNAME;
    public String SETTINGS_DATABASE_MYSQL_PASSWORD;
    public String SETTINGS_DATABASE_MYSQL_SSL;
    public int SETTINGS_DATABASE_MYSQL_PORT;
    public String SETTINGS_DATABASE_TABLE;
    public List<String> SETTINGS_CURRENCIES_ENABLED_CURRENCIES;
    public String SETTINGS_DEFAULT_CURRENCY;
    public String SETTINGS_VAULT_CURRENCY;

    /* TODO CHANGE TO ALLOW FOR MULTI CURRENCY.
    public String SETTINGS_DECIMAL_FORMAT;
    public double SETTINGS_DEFAULT_MONEY;
    public String SETTINGS_CURRENCY_PLURAL;
    public String SETTINGS_CURRENCY_SINGULAR;
    public String SETTINGS_CURRENCY_FORMAT;
    */

    private PhantomEconomy instance;

    public FileCache(final PhantomEconomy instance) {
        this.instance = instance;
    }

    //Load or reload values from files
    public void loadFromFiles() {
        FlatFile settings = instance.settingsYaml;

        SETTINGS_OTHER_USE_UPDATE_CHECKER = settings.get("other-options.use-update-checker", true);
        SETTINGS_DATABASE_TYPE = settings.get("database.database-type", "SQLite");
        SETTINGS_DATABASE_MYSQL_HOST = settings.get("database.mysql.host", "localhost");
        SETTINGS_DATABASE_MYSQL_DATABASE = settings.get("database.mysql.database", "minecraft");
        SETTINGS_DATABASE_MYSQL_USERNAME = settings.get("database.mysql.username", "root");
        SETTINGS_DATABASE_MYSQL_PASSWORD = settings.get("database.mysql.password", "password");
        SETTINGS_DATABASE_MYSQL_SSL = settings.get("database.mysql.ssl", "true");
        SETTINGS_DATABASE_MYSQL_PORT = settings.get("database.mysql.port", 3306);
        SETTINGS_DATABASE_TABLE = settings.get("database.table", "phantomeconomy");
        SETTINGS_CURRENCIES_ENABLED_CURRENCIES = settings.get("currencies.enabled-currencies", null);
        SETTINGS_DEFAULT_CURRENCY = settings.get("default-currency", null);
        SETTINGS_VAULT_CURRENCY = settings.get("vault-currency", null);

        /* TODO CHANGE TO ALLOW FOR MULTI CURRENCY.
        SETTINGS_DECIMAL_FORMAT = settings.get("economy.formatting.decimal-format", "#,##0.00");
        SETTINGS_DEFAULT_MONEY = settings.get("economy.default-money", 50.00);
        SETTINGS_CURRENCY_PLURAL = settings.get("economy.formatting.vault.plural", "dollars");
        SETTINGS_CURRENCY_SINGULAR = settings.get("economy.formatting.vault.singular", "dollar");
        SETTINGS_CURRENCY_FORMAT = settings.get("economy.formatting.format", "$%money%");
         */
    }
}
