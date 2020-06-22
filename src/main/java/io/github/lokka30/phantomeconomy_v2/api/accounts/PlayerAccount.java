package io.github.lokka30.phantomeconomy_v2.api.accounts;

import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.OversizedWithdrawAmountException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerAccount {

    private AccountManager accountManager;

    private OfflinePlayer offlinePlayer;

    public PlayerAccount(AccountManager accountManager, OfflinePlayer offlinePlayer) {
        this.accountManager = accountManager;
        this.offlinePlayer = offlinePlayer;
    }

    public PlayerAccount(AccountManager accountManager, UUID uuid) {
        this.accountManager = accountManager;
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
    }

    public OfflinePlayer getPlayer() {
        return offlinePlayer;
    }

    public UUID getUUID() {
        return offlinePlayer.getUniqueId();
    }

    public String getUUIDAsString() {
        return getUUID().toString();
    }

    public double getBalance(Currency currency) {
        if (!accountManager.cachedPlayerAccountBalances.containsKey(getUUID())) {
            HashMap<Currency, Double> balanceMap = new HashMap<>();
            balanceMap.put(currency, accountManager.getInstance().getDatabase().getBalance("PlayerAccount", getUUIDAsString(), currency.getName()));
            accountManager.cachedPlayerAccountBalances.put(getUUID(), balanceMap);
        } else if (!accountManager.cachedPlayerAccountBalances.get(getUUID()).containsKey(currency)) {
            HashMap<Currency, Double> balanceMap = accountManager.cachedPlayerAccountBalances.get(getUUID());
            balanceMap.put(currency, accountManager.getInstance().getDatabase().getBalance("PlayerAccount", getUUIDAsString(), currency.getName()));
            accountManager.cachedPlayerAccountBalances.put(getUUID(), balanceMap);
        }

        return accountManager.cachedPlayerAccountBalances.get(getUUID()).get(currency);
    }

    public void setBalance(Currency currency, double amount) throws NegativeAmountException {
        if (amount < 0) {
            throw new NegativeAmountException("Tried to set balance to PlayerAccount with name '" + getUUID() + "' and amount '" + amount + "' but the amount is lower than 0");
        } else {
            HashMap<Currency, Double> balanceMap = new HashMap<>();
            balanceMap.put(currency, amount);
            accountManager.cachedPlayerAccountBalances.put(getUUID(), balanceMap);
            accountManager.getInstance().getDatabase().setBalance("PlayerAccount", getUUIDAsString(), currency.getName(), amount);
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
}
