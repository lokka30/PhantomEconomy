package io.github.lokka30.phantomeconomy.api;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.accounts.BankAccount;
import io.github.lokka30.phantomeconomy.api.accounts.NonPlayerAccount;
import io.github.lokka30.phantomeconomy.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("unused")
public class AccountManager {

    public HashMap<UUID, HashMap<String, Double>> cachedPlayerAccountBalances = new HashMap<>();
    public HashMap<String, HashMap<String, Double>> cachedNonPlayerAccountBalances = new HashMap<>();
    public HashMap<String, HashMap<String, Double>> cachedBankAccountBalances = new HashMap<>();
    private PhantomEconomy instance;

    public AccountManager(final PhantomEconomy instance) {
        this.instance = instance;
    }

    public PhantomEconomy getInstance() {
        return instance;
    }

    public PlayerAccount getPlayerAccount(final OfflinePlayer offlinePlayer) {
        return new PlayerAccount(this, offlinePlayer);
    }

    public NonPlayerAccount getNonPlayerAccount(final String name) {
        return new NonPlayerAccount(this, name);
    }

    public BankAccount getBankAccount(final String name) {
        return new BankAccount(this, name);
    }

    public boolean hasPlayerAccount(final OfflinePlayer offlinePlayer, final Currency currency) {
        return (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) && instance.getDatabase().hasAccount("PlayerAccount", offlinePlayer.getUniqueId().toString(), currency);
    }

    public boolean hasNonPlayerAccount(final String name, final Currency currency) {
        return instance.getDatabase().hasAccount("NonPlayerAccount", name, currency);
    }

    public boolean hasBankAccount(final String name, final Currency currency) {
        return instance.getDatabase().hasAccount("BankAccount", name, currency);
    }

    public void createPlayerAccount(final OfflinePlayer offlinePlayer, final Currency currency) throws AccountAlreadyExistsException, InvalidCurrencyException {
        if (hasPlayerAccount(offlinePlayer, currency)) {
            throw new AccountAlreadyExistsException("Tried to create PlayerAccount with uuid '" + offlinePlayer.getUniqueId().toString() + "' but its account already exists.");
        } else {
            instance.getDatabase().createAccount("PlayerAccount", offlinePlayer.getUniqueId().toString());
        }
    }

    public void createNonPlayerAccount(final String name, final Currency currency) throws AccountAlreadyExistsException, InvalidCurrencyException {
        if (hasNonPlayerAccount(name, currency)) {
            throw new AccountAlreadyExistsException("Tried to create NonPlayerAccount with name '" + name + "' but its account already exists.");
        } else {
            instance.getDatabase().createAccount("NonPlayerAccount", name);
        }
    }

    public void createBankAccount(final String name, final Currency currency) throws AccountAlreadyExistsException, InvalidCurrencyException {
        if (hasBankAccount(name, currency)) {
            throw new AccountAlreadyExistsException("Tried to create BankAccount with name '" + name + "' but its account already exists.");
        } else {
            instance.getDatabase().createAccount("BankAccount", name);
        }
    }
}
