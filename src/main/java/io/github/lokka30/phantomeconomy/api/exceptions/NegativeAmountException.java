package io.github.lokka30.phantomeconomy.api.exceptions;

public class NegativeAmountException extends Exception {

    String txt;

    public NegativeAmountException(String txt) {
        this.txt = txt;
    }

    public String toString() {
        return "NegativeAmountException: " + txt;
    }
}
