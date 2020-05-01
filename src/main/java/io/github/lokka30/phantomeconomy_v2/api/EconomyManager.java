package io.github.lokka30.phantomeconomy_v2.api;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;

import java.text.DecimalFormat;

public class EconomyManager {

    private PhantomEconomy instance;
    private DecimalFormat decimalFormat;

    public EconomyManager(final PhantomEconomy instance) {
        this.instance = instance;
        this.decimalFormat = new DecimalFormat(instance.fileCache.SETTINGS_DECIMAL_FORMAT);
    }

    public String getSingularOrPlural(double balance) {
        if (balance == 1.00) {
            return instance.fileCache.SETTINGS_CURRENCY_SINGULAR;
        } else {
            return instance.fileCache.SETTINGS_CURRENCY_PLURAL;
        }
    }

    public String formatBalance(final double balance) {
        final String formattedBalance = decimalFormat.format(balance);
        return instance.fileCache.SETTINGS_CURRENCY_FORMAT
                .replaceAll("%money%", formattedBalance
                        .replaceAll("%word%", getSingularOrPlural(balance)));
    }


}
