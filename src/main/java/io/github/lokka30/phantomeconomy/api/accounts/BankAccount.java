package io.github.lokka30.phantomeconomy.api.accounts;

import io.github.lokka30.phantomeconomy.api.AccountManager;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy.api.exceptions.OversizedWithdrawAmountException;
import io.github.lokka30.phantomeconomy.enums.AccountType;

import java.util.HashMap;

@SuppressWarnings("unused")
public class BankAccount {

    private AccountManager accountManager;

    private String name;
    private AccountType ownerAccountType;
    private String ownerId;

    public BankAccount(AccountManager accountManager, String name, AccountType ownerAccountType, String ownerId) {
        this.accountManager = accountManager;
        this.name = name;
        this.ownerAccountType = ownerAccountType;
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public double getBalance(Currency currency) {
        if (!accountManager.cachedBankAccountBalances.containsKey(getName())) {
            HashMap<String, Double> balanceMap = new HashMap<>();
            balanceMap.put(currency.getName(), accountManager.getInstance().getDatabase().getBankBalance(getName(), currency.getName(), ownerAccountType, ownerId));
            accountManager.cachedBankAccountBalances.put(getName(), balanceMap);
        } else if (!accountManager.cachedBankAccountBalances.get(getName()).containsKey(currency.getName())) {
            HashMap<String, Double> balanceMap = accountManager.cachedBankAccountBalances.get(getName());
            balanceMap.put(currency.getName(), accountManager.getInstance().getDatabase().getBankBalance(getName(), currency.getName(), ownerAccountType, ownerId));
            accountManager.cachedBankAccountBalances.put(getName(), balanceMap);
        }

        return accountManager.cachedBankAccountBalances.get(getName()).get(currency.getName());
    }

    public void setBalance(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to BankAccount with name '" + getName() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            HashMap<String, Double> balanceMap = new HashMap<>();
            amount = accountManager.getInstance().getUtils().trimDecimals(amount);
            balanceMap.put(currency.getName(), amount);
            accountManager.cachedBankAccountBalances.put(getName(), balanceMap);
            accountManager.getInstance().getDatabase().setBankBalance(getName(), currency.getName(), amount, ownerAccountType, ownerId);
        }
    }

    public void deposit(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to BankAccount with name '" + getName() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            setBalance(currency, getBalance(currency) + amount);
        }
    }

    public void withdraw(Currency currency, double amount) throws NegativeAmountException, OversizedWithdrawAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to BankAccount with name '" + getName() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            if (getBalance(currency) < amount) {
                throw new OversizedWithdrawAmountException(amount + " is too large for the BankAccount named '" + getName() + "' which has a balance of '" + getBalance(currency) + "'.");
            } else {
                setBalance(currency, getBalance(currency) - amount);
            }
        }
    }

    public boolean has(Currency currency, double amount) {
        return getBalance(currency) >= amount;
    }
}
