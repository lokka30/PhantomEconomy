package io.github.lokka30.phantomeconomy.api;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.accounts.BankAccount;
import io.github.lokka30.phantomeconomy.api.accounts.NonPlayerAccount;
import io.github.lokka30.phantomeconomy.api.accounts.PlayerAccount;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy.enums.AccountType;

import java.sql.SQLException;
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

    public PlayerAccount getPlayerAccount(final UUID uuid) {
        return new PlayerAccount(this, uuid);
    }

    public NonPlayerAccount getNonPlayerAccount(final String name) {
        return new NonPlayerAccount(this, name);
    }

    public BankAccount getBankAccount(final String bankId, final AccountType ownerAccountType, final String ownerId) {
        return new BankAccount(this, bankId, ownerAccountType, ownerId);
    }

    public BankAccount getBankAccountFromId(final String bankId) throws SQLException, InvalidCurrencyException {
        return instance.getDatabase().getBankAccountFromId(bankId);
    }

    public boolean hasPlayerAccount(final UUID uuid, final Currency currency) {
        return instance.getDatabase().hasAccount(AccountType.PlayerAccount, uuid.toString(), currency);
    }

    public boolean hasNonPlayerAccount(final String name, final Currency currency) {
        return instance.getDatabase().hasAccount(AccountType.NonPlayerAccount, name, currency);
    }

    public boolean hasBankAccount(final String name, final Currency currency) {
        return instance.getDatabase().hasAccount(AccountType.BankAccount, name, currency);
    }

    public void createPlayerAccount(final UUID uuid, final Currency currency) throws AccountAlreadyExistsException, InvalidCurrencyException {
        if (hasPlayerAccount(uuid, currency)) {
            throw new AccountAlreadyExistsException("Tried to create PlayerAccount with uuid '" + uuid.toString() + "' but its account already exists.");
        } else {
            instance.getDatabase().createAccount(AccountType.PlayerAccount, uuid.toString());
        }
    }

    public void createNonPlayerAccount(final String name, final Currency currency) throws AccountAlreadyExistsException, InvalidCurrencyException {
        if (hasNonPlayerAccount(name, currency)) {
            throw new AccountAlreadyExistsException("Tried to create NonPlayerAccount with name '" + name + "' but its account already exists.");
        } else {
            instance.getDatabase().createAccount(AccountType.NonPlayerAccount, name);
        }
    }

    public void createBankAccount(final String bankId, final Currency currency, final AccountType ownerAccountType, final String ownerId) throws AccountAlreadyExistsException, InvalidCurrencyException {
        if (hasBankAccount(bankId, currency)) {
            throw new AccountAlreadyExistsException("Tried to create BankAccount with bankId '" + bankId + "' but its account already exists.");
        } else {
            instance.getDatabase().createBankAccount(bankId, ownerAccountType, ownerId);
        }
    }
}
