package io.github.lokka30.phantomeconomy.api.accounts;

import io.github.lokka30.phantomeconomy.api.AccountManager;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy.api.exceptions.OversizedWithdrawAmountException;
import io.github.lokka30.phantomeconomy.enums.AccountType;

import java.util.HashMap;

@SuppressWarnings("unused")
public class NonPlayerAccount {

    private AccountManager accountManager;

    private String name;

    public NonPlayerAccount(AccountManager accountManager, String name) {
        this.accountManager = accountManager;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getBalance(Currency currency) {
        if (!accountManager.cachedNonPlayerAccountBalances.containsKey(getName())) {
            HashMap<String, Double> balanceMap = new HashMap<>();
            balanceMap.put(currency.getName(), accountManager.getInstance().getDatabase().getBalance(AccountType.NonPlayerAccount, getName(), currency.getName()));
            accountManager.cachedNonPlayerAccountBalances.put(getName(), balanceMap);
        } else if (!accountManager.cachedNonPlayerAccountBalances.get(getName()).containsKey(currency.getName())) {
            HashMap<String, Double> balanceMap = accountManager.cachedNonPlayerAccountBalances.get(getName());
            balanceMap.put(currency.getName(), accountManager.getInstance().getDatabase().getBalance(AccountType.NonPlayerAccount, getName(), currency.getName()));
            accountManager.cachedNonPlayerAccountBalances.put(getName(), balanceMap);
        }

        return accountManager.cachedNonPlayerAccountBalances.get(getName()).get(currency.getName());
    }

    public void setBalance(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to NonPlayerAccount with name '" + getName() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            HashMap<String, Double> balanceMap = new HashMap<>();
            balanceMap.put(currency.getName(), amount);
            accountManager.cachedNonPlayerAccountBalances.put(getName(), balanceMap);
            accountManager.getInstance().getDatabase().setBalance(AccountType.NonPlayerAccount, getName(), currency.getName(), amount);
        }
    }

    public void deposit(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to NonPlayerAccount with name '" + getName() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            setBalance(currency, getBalance(currency) + amount);
        }
    }

    public void withdraw(Currency currency, double amount) throws NegativeAmountException, OversizedWithdrawAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to NonPlayerAccount with name '" + getName() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            if (getBalance(currency) < amount) {
                throw new OversizedWithdrawAmountException(amount + " is too large for the NonPlayerAccount named '" + getName() + "' which has a balance of '" + getBalance(currency) + "'.");
            } else {
                setBalance(currency, getBalance(currency) - amount);
            }
        }
    }

    public boolean has(Currency currency, double amount) {
        return getBalance(currency) >= amount;
    }
}
