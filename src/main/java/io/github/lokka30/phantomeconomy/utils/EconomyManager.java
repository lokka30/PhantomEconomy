package io.github.lokka30.phantomeconomy.utils;

import io.github.lokka30.phantomeconomy.PhantomEconomy;

import java.util.UUID;

public class EconomyManager {

    private PhantomEconomy instance;

    public EconomyManager(PhantomEconomy instance) {
        this.instance = instance;
    }

    //getDefaultBalance() returns double
    //Gets the default balance.
    public double getDefaultBalance() {
        final String path = "default-balance";
        final double defaultBalance = instance.settings.get(path, 50.0D);

        if (defaultBalance >= 0) {
            return defaultBalance;
        } else {
            instance.log(LogLevel.SEVERE, "Modified an interaction with the economy!");
            instance.log(LogLevel.SEVERE, "&8 - &7Attempted to getDefaultBalance where amount < 0.");
            instance.log(LogLevel.SEVERE, "&8 - &7Specified amount: &a" + defaultBalance + "&7.");
            instance.log(LogLevel.SEVERE, "&8 - &7Returned a default balance of 50.");
            instance.log(LogLevel.SEVERE, "&8 - &7Please change 'default-balance' in your config to a value >= 0.");
            return 50.0;
        }
    }

    //getBalance(uuid) returns double
    //Gets the user's balance.
    public double getBalance(UUID uuid) {
        final String path = "players." + uuid.toString() + ".balance";

        return instance.data.get(path, getDefaultBalance());
    }

    //setBalance(uuid, (double) amount)
    //Sets the user's balance to the amount.
    public void setBalance(UUID uuid, double amount) {
        if (amount >= 0) {
            final String path = "players." + uuid.toString() + ".balance";
            instance.data.set(path, amount);
        } else {
            instance.log(LogLevel.SEVERE, "Prevented an external plugin from an interaction with the economy!");
            instance.log(LogLevel.SEVERE, "&8 - &7Attempted to setBalance where amount < 0.");
            instance.log(LogLevel.SEVERE, "&8 - &7Specified amount: &a" + amount + "&7, Specified UUID: &a" + uuid.toString() + "&7.");
            instance.log(LogLevel.SEVERE, "&8 - &7Please notify the author of such plugin of this as they are using the method incorrectly.");
        }
    }

    //addBalance(uuid, (double) amount)
    //Adds an amount to the user's balance.
    public void addBalance(UUID uuid, double amount) {
        if (amount > 0) {
            final String path = "players." + uuid.toString() + ".balance";
            final double currentBalance = getBalance(uuid);

            instance.data.set(path, currentBalance + amount);
        } else {
            instance.log(LogLevel.SEVERE, "Prevented an external plugin from an interaction with the economy!");
            instance.log(LogLevel.SEVERE, "&8 - &7Attempted to addBalance where amount =< 0.");
            instance.log(LogLevel.SEVERE, "&8 - &7Specified amount: &a" + amount + "&7, Specified UUID: &a" + uuid.toString() + "&7.");
            instance.log(LogLevel.SEVERE, "&8 - &7Please notify the author of such plugin of this as they are using the method incorrectly.");
        }
    }

    //removeBalance(uuid, (double) amount)
    //Removes an amount from the user's balance.
    public void removeBalance(UUID uuid, double amount) {
        if (amount > 0) {
            final String path = "players." + uuid.toString() + ".balance";
            final double currentBalance = getBalance(uuid);
            final double newBalance = currentBalance - amount;

            if (newBalance > 0) {
                instance.data.set(path, currentBalance - amount);
            } else {
                instance.log(LogLevel.SEVERE, "Prevented an external plugin from an interaction with the economy!");
                instance.log(LogLevel.SEVERE, "&8 - &7Attempted to removeBalance where new balance < 0.");
                instance.log(LogLevel.SEVERE, "&8 - &7Specified amount: &a" + amount + "&7, Specified UUID: &a" + uuid.toString() + "&7, Current balance: &a" + currentBalance + "&7, New balance: &a" + newBalance);
                instance.log(LogLevel.SEVERE, "&8 - &7Please notify the author of such plugin of this as they are using the method incorrectly.");
            }
        } else {
            instance.log(LogLevel.SEVERE, "Prevented an external plugin from an interaction with the economy!");
            instance.log(LogLevel.SEVERE, "&8 - &7Attempted to removeBalance where amount =< 0.");
            instance.log(LogLevel.SEVERE, "&8 - &7Specified amount: &a" + amount + "&7, Specified UUID: &a" + uuid.toString() + "&7.");
            instance.log(LogLevel.SEVERE, "&8 - &7Please notify the author of such plugin of this as they are using the method incorrectly.");
        }
    }

    //resetBalance(uuid, (double) amount)
    //Sets the user's balance to the default balance.
    public void resetBalance(UUID uuid, double amount) {
        setBalance(uuid, getDefaultBalance());
    }
}
