package io.github.lokka30.phantomeconomy_v2.api.exceptions;

public class OversizedWithdrawAmountException extends Exception {

    String txt;

    public OversizedWithdrawAmountException(String txt) {
        this.txt = txt;
    }

    public String toString() {
        return "OversizedWithdrawAmountException: " + txt;
    }
}
