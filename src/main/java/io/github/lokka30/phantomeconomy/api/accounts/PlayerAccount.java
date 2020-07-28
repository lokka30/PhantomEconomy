package io.github.lokka30.phantomeconomy.api.accounts;

import io.github.lokka30.phantomeconomy.api.AccountManager;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy.api.exceptions.OversizedWithdrawAmountException;
import io.github.lokka30.phantomeconomy.enums.AccountType;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerAccount {

    private AccountManager accountManager;
    private UUID uuid;

    public PlayerAccount(AccountManager accountManager, UUID uuid) {
        this.accountManager = accountManager;
        this.uuid = uuid;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUUIDAsString() {
        return getUUID().toString();
    }

    public double getBalance(Currency currency) {
        cacheCurrencyBalanceIfUnset(currency);
        return accountManager.cachedPlayerAccountBalances.get(getUUID()).get(currency.getName());
    }

    public void setBalance(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to PlayerAccount with uuid '" + getUUIDAsString() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            cacheCurrencyBalanceIfUnset(currency);
            HashMap<String, Double> currencyBalanceMap = accountManager.cachedPlayerAccountBalances.get(getUUID());
            amount = accountManager.getInstance().getUtils().trimDecimals(amount);
            currencyBalanceMap.put(currency.getName(), amount);
            accountManager.getInstance().getDatabase().setBalance(AccountType.PlayerAccount, getUUIDAsString(), currency.getName(), amount);
        }
    }

    public void deposit(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to PlayerAccount with uuid '" + getUUIDAsString() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            setBalance(currency, getBalance(currency) + amount);
        }
    }

    public void withdraw(Currency currency, double amount) throws NegativeAmountException, OversizedWithdrawAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to PlayerAccount with uuid '" + getUUIDAsString() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            if (getBalance(currency) < amount) {
                throw new OversizedWithdrawAmountException(amount + " is too large for the PlayerAccount with uuid '" + getUUIDAsString() + "' which has a balance of '" + getBalance(currency) + "'.");
            } else {
                setBalance(currency, getBalance(currency) - amount);
            }
        }
    }

    public boolean has(Currency currency, double amount) {
        return getBalance(currency) >= amount;
    }

    public void cacheCurrencyBalanceIfUnset(final Currency currency) {
        if (!accountManager.cachedPlayerAccountBalances.containsKey(getUUID())) {
            HashMap<String, Double> balanceMap = new HashMap<>();
            balanceMap.put(currency.getName(), accountManager.getInstance().getDatabase().getBalance(AccountType.PlayerAccount, getUUIDAsString(), currency.getName()));
            accountManager.cachedPlayerAccountBalances.put(getUUID(), balanceMap);
        } else if (!accountManager.cachedPlayerAccountBalances.get(getUUID()).containsKey(currency.getName())) {
            HashMap<String, Double> balanceMap = accountManager.cachedPlayerAccountBalances.get(getUUID());
            balanceMap.put(currency.getName(), accountManager.getInstance().getDatabase().getBalance(AccountType.PlayerAccount, getUUIDAsString(), currency.getName()));
            accountManager.cachedPlayerAccountBalances.put(getUUID(), balanceMap);
        }
    }
}
