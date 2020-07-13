package io.github.lokka30.phantomeconomy.api.exceptions;

public class OversizedWithdrawAmountException extends Exception {

    String txt;

    public OversizedWithdrawAmountException(String txt) {
        this.txt = txt;
    }

    public String toString() {
        return "OversizedWithdrawAmountException: " + txt;
    }
}
