package io.github.lokka30.phantomeconomy_v2.api.currencies;

import io.github.lokka30.phantomeconomy_v2.api.EconomyManager;

import java.text.DecimalFormat;

public class Currency {

    private EconomyManager economyManager;
    private String name;

    public Currency(EconomyManager economyManager, String name) {
        this.economyManager = economyManager;
        this.name = name;
    }

    @SuppressWarnings("unused")
    public String getName() {
        return name;
    }

    // The *number* format, this is the string that the DecimalFormat uses
    public String getNumberFormat() {
        //TODO Return 'format-number' from the config.
        return "0.00";
    }

    // The *final* format, the human-readable form which adds dollar signs and whatever the user has configured for the currency
    public String getFinalFormat() {
        //TODO Return 'format-final' from the config.
        return "$%balance%";
    }

    // Returns 'plural' word from settings
    public String getPlural() {
        return "dollars";
        //TODO
    }

    // Returns 'singular' word from settings
    public String getSingular() {
        return "dollar";
        //TODO
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
        DecimalFormat decimalFormat = new DecimalFormat(getNumberFormat());
        return decimalFormat.format(balance);
    }

    // Formats the final number
    // e.g. so 2.22222 will return as '$2.22' by defualt
    public String formatFinalBalance(double balance) {
        return getFinalFormat()
                .replaceAll("%balance%", formatNumberBalance(balance))
                .replaceAll("%word%", getSingularOrPlural(balance));
    }
}
