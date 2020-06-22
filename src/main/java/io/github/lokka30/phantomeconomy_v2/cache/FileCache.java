package io.github.lokka30.phantomeconomy_v2.cache;

import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;

import java.util.ArrayList;
import java.util.HashMap;
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

    public HashMap<Currency, Double> SETTINGS_CURRENCY_DEFAULT_BALANCE_MAP;
    public HashMap<Currency, Integer> SETTINGS_CURRENCY_FORMATTING_STORAGE_ROUNDING_MAP;
    public HashMap<Currency, String> SETTINGS_CURRENCY_FORMATTING_DECIMAL_READABLE_FORMAT_MAP;
    public HashMap<Currency, String> SETTINGS_CURRENCY_FORMATTING_FINAL_READABLE_FORMAT_MAP;
    public HashMap<Currency, String> SETTINGS_CURRENCY_FORMATTING_WORDS_SINGULAR_MAP;
    public HashMap<Currency, String> SETTINGS_CURRENCY_FORMATTING_WORDS_PLURAL_MAP;

    private PhantomEconomy instance;

    public FileCache(final PhantomEconomy instance) {
        this.instance = instance;
    }

    //Load or reload values from files
    public void loadFromFiles() {
        FlatFile settings = instance.getSettings();
        FlatFile messages = instance.getMessages();

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


        SETTINGS_CURRENCY_DEFAULT_BALANCE_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_STORAGE_ROUNDING_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_DECIMAL_READABLE_FORMAT_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_FINAL_READABLE_FORMAT_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_WORDS_SINGULAR_MAP = new HashMap<>();
        SETTINGS_CURRENCY_FORMATTING_WORDS_PLURAL_MAP = new HashMap<>();
        for (String currencyName : settings.get("currencies.enabled-currencies", new ArrayList<String>())) {
            Currency currency;
            final String path = "currencies.currency-settings." + currencyName + ".";
            try {
                currency = instance.getEconomyManager().getCurrency(currencyName);
            } catch (InvalidCurrencyException exception) {
                instance.getUtils().log(LogLevel.SEVERE, "Currency '" + currencyName + "' was listed in 'enabled currencies' in the settings file, but the currency doesn't exist.");
                continue;
            }

            SETTINGS_CURRENCY_DEFAULT_BALANCE_MAP.put(currency, settings.get(path + "default-balance", 50.00));
            SETTINGS_CURRENCY_FORMATTING_STORAGE_ROUNDING_MAP.put(currency, settings.get(path + "formatting.storage-rounding", 2));
            SETTINGS_CURRENCY_FORMATTING_DECIMAL_READABLE_FORMAT_MAP.put(currency, settings.get(path + "formatting.decimal-readable-format", "0.00"));
            SETTINGS_CURRENCY_FORMATTING_FINAL_READABLE_FORMAT_MAP.put(currency, settings.get(path + "formatting.final-readable-format", "$%balance% %word%"));
            SETTINGS_CURRENCY_FORMATTING_WORDS_SINGULAR_MAP.put(currency, settings.get(path + "formatting.words.singular", "dollar"));
            SETTINGS_CURRENCY_FORMATTING_WORDS_PLURAL_MAP.put(currency, settings.get(path + "formatting.words.plural", "dollars"));
        }
    }
}
