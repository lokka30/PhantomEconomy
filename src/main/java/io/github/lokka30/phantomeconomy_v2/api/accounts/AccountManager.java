package io.github.lokka30.phantomeconomy_v2.api.accounts;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.currencies.Currency;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.HashMap;

@SuppressWarnings("unused")
public class AccountManager {

    //TODO SCHEDULE REPEATING TASK TO CLEAR NON PLAYER ACCOUNT AND BANK ACCOUNT BALANCES.

    public HashMap<PlayerAccount, HashMap<Currency, Double>> cachedPlayerAccountBalances = new HashMap<>();
    public HashMap<NonPlayerAccount, HashMap<Currency, Double>> cachedNonPlayerAccountBalances = new HashMap<>();
    public HashMap<BankAccount, HashMap<Currency, Double>> cachedBankAccountBalances = new HashMap<>();
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

    public boolean hasPlayerAccount(final OfflinePlayer offlinePlayer) throws SQLException {
        return instance.getDatabase().hasAccount("PlayerAccount", offlinePlayer.getUniqueId().toString());
    }

    public boolean hasNonPlayerAccount(final String name) throws SQLException {
        return instance.getDatabase().hasAccount("NonPlayerAccount", name);
    }

    public boolean hasBankAccount(final String name) throws SQLException {
        return instance.getDatabase().hasAccount("BankAccount", name);
    }

    public void createPlayerAccount(final OfflinePlayer offlinePlayer) throws AccountAlreadyExistsException, SQLException {
        if (hasPlayerAccount(offlinePlayer)) {
            throw new AccountAlreadyExistsException("Tried to create PlayerAccount with uuid '" + offlinePlayer.getUniqueId().toString() + "' but its account already exists.");
        } else {
            //TODO Tell the database to create the account
        }
    }

    public void createNonPlayerAccount(final String name) throws AccountAlreadyExistsException, SQLException {
        if (hasNonPlayerAccount(name)) {
            throw new AccountAlreadyExistsException("Tried to create NonPlayerAccount with name '" + name + "' but its account already exists.");
        } else {
            //TODO Tell the database to create the account
        }
    }

    public void createBankAccount(final String name) throws AccountAlreadyExistsException, SQLException {
        if (hasBankAccount(name)) {
            throw new AccountAlreadyExistsException("Tried to create BankAccount with name '" + name + "' but its account already exists.");
        } else {
            //TODO Tell the database to create the account
        }
    }
}
