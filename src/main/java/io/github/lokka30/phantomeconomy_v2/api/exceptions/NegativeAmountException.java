package io.github.lokka30.phantomeconomy_v2.api.exceptions;

public class NegativeAmountException extends Exception {

    String txt;

    public NegativeAmountException(String txt) {
        this.txt = txt;
    }

    public String toString() {
        return "NegativeAmountException: " + txt;
    }
}
