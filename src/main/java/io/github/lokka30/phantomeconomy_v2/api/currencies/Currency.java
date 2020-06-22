package io.github.lokka30.phantomeconomy_v2.api.currencies;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.EconomyManager;

import java.text.DecimalFormat;

public class Currency {

    private PhantomEconomy instance;
    private String name;

    public Currency(EconomyManager economyManager, String name) {
        this.name = name;
        this.instance = economyManager.getInstance();
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    // The *decimal* readable format, this is the string that the DecimalFormat uses
    public String getDecimalReadableFormat() {
        return instance.getFileCache().SETTINGS_CURRENCY_FORMATTING_DECIMAL_READABLE_FORMAT_MAP.get(this);
    }

    // The *final* readable format, the human-readable form which adds dollar signs and whatever the user has configured for the currency
    public String getFinalFormat() {
        return instance.getFileCache().SETTINGS_CURRENCY_FORMATTING_FINAL_READABLE_FORMAT_MAP.get(this);
    }

    // Returns 'plural' word from settings
    public String getPlural() {
        return instance.getFileCache().SETTINGS_CURRENCY_FORMATTING_WORDS_PLURAL_MAP.get(this);
    }

    // Returns 'singular' word from settings
    public String getSingular() {
        return instance.getFileCache().SETTINGS_CURRENCY_FORMATTING_WORDS_SINGULAR_MAP.get(this);
    }

    // Gets the 'singular' or 'plural' word for the balance.
    // e.g. so $1 = singular, $1.01 or $5 = plural
    public String getSingularOrPlural(double balance) {
        if (balance == 1.00) {
            return getSingular();
        } else {
            return getPlural();
        }
    }

    // Formats the number
    // e.g. so 2.222222 will return as '2.22' - by default
    public String formatNumberBalance(double balance) {
        DecimalFormat decimalFormat = new DecimalFormat(getDecimalReadableFormat());
        return decimalFormat.format(balance);
    }

    // Formats the final number
    // e.g. so 2.22222 will return as '$2.22' by defualt
    public String formatFinalBalance(double balance) {
        return getFinalFormat()
                .replaceAll("%balance%", formatNumberBalance(balance))
                .replaceAll("%word%", getSingularOrPlural(balance));
    }

    /**
     * @return the default balance set in the 'settings.yml' file by the user. Given to all players when they first use the currency.
     */
    public double getDefaultBalance() {
        return instance.getFileCache().SETTINGS_CURRENCY_DEFAULT_BALANCE_MAP.get(this);
    }
}
