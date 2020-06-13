package io.github.lokka30.phantomeconomy.utils;

import org.apache.commons.math3.util.Precision;

import java.text.NumberFormat;
import java.util.Locale;

public class Utils {

    public static String getRecommendedServerVersion() {
        return "1.15";
    }

    public static int getRecommendedSettingsVersion() {
        return 5;
    }

    public static int getRecommendedMessagesVersion() {
        return 4;
    }

    public static int getRecommendedDataVersion() {
        return 1;
    }

    public static double round(double value) {
        /*
        Important! Avoid conversion from 'double -> String -> double' wherever possible.

        Known methods used previously that don't play nicely with PhantomEconomy:
        - Math.round
        - DecimalFormat, and other number formatters alike

        Try to use a method which is fast as well. DecimalFormat is very, very slow at its calculations.
         */
        return Precision.round(value, 2);
    }

    public static String roundToString(double value) {
        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        return numberFormat.format(value);
    }

}
