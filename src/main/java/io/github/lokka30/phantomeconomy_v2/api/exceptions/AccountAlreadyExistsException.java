package io.github.lokka30.phantomeconomy_v2.api.exceptions;

public class AccountAlreadyExistsException extends Exception {

    String txt;

    public AccountAlreadyExistsException(String txt) {
        this.txt = txt;
    }

    public String toString() {
        return "AccountAlreadyExistsException: " + txt;
    }
}
