package io.github.lokka30.phantomeconomy.utils;

import java.text.NumberFormat;
import java.util.Locale;

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
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(value);
    }

}
