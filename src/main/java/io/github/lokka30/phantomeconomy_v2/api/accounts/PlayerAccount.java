package io.github.lokka30.phantomeconomy_v2.api.accounts;

import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.OversizedWithdrawAmountException;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlayerAccount {

    private AccountManager accountManager;

    private UUID uuid;
    private String uuidStr;

    public PlayerAccount(AccountManager accountManager, OfflinePlayer offlinePlayer) {
        this.accountManager = accountManager;
        this.uuid = offlinePlayer.getUniqueId();
        this.uuidStr = uuid.toString();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUUIDStr() {
        return uuidStr;
    }

    public double getBalance(Currency currency) {
        //TODO
        return 0.0;
    }

    public void setBalance(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException(amount + " is lower than 0");
        } else {
            //TODO
        }
    }

    public void resetToDefaultBalance(Currency currency) {
        //TODO
    }

    public void deposit(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException(amount + " is lower than 0");
        } else {
            //TODO
        }
    }

    public void withdraw(Currency currency, double amount) throws NegativeAmountException, OversizedWithdrawAmountException {
        if (amount < 0) {
            throw new NegativeAmountException(amount + " is lower than 0");
        } else {
            if (amount > getBalance(currency)) {
                throw new OversizedWithdrawAmountException(amount + " is greater than player's balance of " + getBalance(currency));
            } else {
                //TODO
            }
        }
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
            throw new AccountAlreadyExistsException("Tried to create account for UUID '" + uuidStr + "' but they already have an account.");
        } else {
            //TODO
        }
    }
}
