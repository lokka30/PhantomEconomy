package io.github.lokka30.phantomeconomy_v2.api.exceptions;

public class InvalidCurrencyException extends Exception {

    String txt;

    public InvalidCurrencyException(String txt) {
        this.txt = txt;
    }

    public String toString() {
        return "InvalidCurrencyException: " + txt;
    }
}
