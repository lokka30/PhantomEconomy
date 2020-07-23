package io.github.lokka30.phantomeconomy.utils;

import org.apache.commons.math3.util.Precision;

import java.util.Arrays;
import java.util.List;

public class Utils {

    public Utils() {
    }

    public List<String> getSupportedServerVersions() {
        return Arrays.asList(
                "1.16",
                "1.15",
                "1.14,",
                "1.13",
                "1.12",
                "1.11",
                "1.10",
                "1.9",
                "1.8",
                "1.7"
        );
    }

    public int getLatestSettingsFileVersion() {
        return 6;
    }

    public int getLatestMessagesFileVersion() {
        return 5;
    }

    public double trimDecimals(final double balance) {
        return Precision.round(balance, 2);
    }
}
