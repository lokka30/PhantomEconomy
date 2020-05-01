package io.github.lokka30.phantomeconomy_v2.accounts;

import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlayerAccount {

    private AccountManager accountManager;

    private UUID uuid;
    private String uuidStr;
    private double balance;

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

    public double getBalance() {
        //TODO
    }

    public void setBalance(double amount) {
        this.balance = amount;
        //TODO
    }

    public void resetToDefaultBalance() {
        this.balance = accountManager.getInstance().fileCache.SETTINGS_DEFAULT_MONEY;
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
    }

    public void createAccount() throws AccountAlreadyExistsException {
        if (hasAccount()) {
            throw new AccountAlreadyExistsException("Tried to create account for UUID '" + uuidStr + "' but they already have an account.");
        } else {
            setBalance(accountManager.getInstance().fileCache.SETTINGS_DEFAULT_MONEY);
            //TODO
        }
    }
}
