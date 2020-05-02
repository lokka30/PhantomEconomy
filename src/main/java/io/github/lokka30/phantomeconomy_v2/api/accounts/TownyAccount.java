package io.github.lokka30.phantomeconomy_v2.api.accounts;

import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;

public class TownyAccount {

    private AccountManager accountManager;

    private String name;

    public TownyAccount(AccountManager accountManager, String name) {
        this.accountManager = accountManager;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getBalance(Currency currency) {
        //TODO
        return 0.0;
    }

    public void setBalance(Currency currency, double amount) {
        //TODO
    }

    public void deposit(Currency currency, double amount) {
        //TODO
    }

    public void withdraw(Currency currency, double amount) {
        //TODO
    }

    public boolean has(Currency currency, double amount) {
        return getBalance(currency) >= amount;
    }

    public boolean hasAccount() {
        //TODO
        return false;
    }

    public void createAccount() throws AccountAlreadyExistsException {
        if (hasAccount()) {
            throw new AccountAlreadyExistsException("Tried to create account for Towny string '" + name + "' but it already has an account.");
        } else {
            //TODO
        }
    }
}
