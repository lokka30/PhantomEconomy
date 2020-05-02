package io.github.lokka30.phantomeconomy.utils;

import java.text.NumberFormat;

public class Utils {

    public static String getRecommendedServerVersion() {
        return "1.15";
    }

    public static int getRecommendedSettingsVersion() {
        return 4;
    }

    public static int getRecommendedMessagesVersion() {
        return 4;
    }

    public static int getRecommendedDataVersion() {
        return 1;
    }

    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static String roundToString(double value) {
        //New Code:
        //Credit to duffymo and daiscog on StackOverflow.
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String formattedValue = formatter.format(value);

        if (formattedValue.endsWith(".00")) {
            int centsIndex = formattedValue.lastIndexOf(".00");
            if (centsIndex != -1) {
                formattedValue = formattedValue.substring(1, centsIndex);
            }
        }

        return formattedValue;
    }

}
