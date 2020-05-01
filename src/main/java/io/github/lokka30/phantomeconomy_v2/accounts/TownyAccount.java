package io.github.lokka30.phantomeconomy_v2.accounts;

import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;

public class TownyAccount {

    private AccountManager accountManager;

    private String name;
    private double balance;

    public TownyAccount(AccountManager accountManager, String name) {
        this.accountManager = accountManager;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        //TODO
    }

    public void setBalance(double amount) {
        this.balance = amount;
        //TODO
    }

    public void deposit(double amount) {
        //TODO
    }

    public void withdraw(double amount) {
        //TODO
    }

    public boolean has(double amount) {
        return balance >= amount;
    }

    public boolean hasAccount() {
        //TODO
    }

    public void createAccount() throws AccountAlreadyExistsException {
        if (hasAccount()) {
            throw new AccountAlreadyExistsException("Tried to create account for Towny string '" + name + "' but it already has an account.");
        } else {
            setBalance(accountManager.getInstance().fileCache.SETTINGS_DEFAULT_MONEY);
            //TODO
        }
    }
}
