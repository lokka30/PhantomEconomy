package io.github.lokka30.phantomeconomy_v2.api.accounts;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;

public class AccountManager {

    public HashMap<PlayerAccount, Double> cachedPlayerAccounts = new HashMap<>();
    public HashMap<TownyAccount, Double> cachedTownyAccounts = new HashMap<>();
    private PhantomEconomy instance;

    public AccountManager(final PhantomEconomy instance) {
        this.instance = instance;
    }

    //This is here so the 'Account' classes can get the main class if required.
    public PhantomEconomy getInstance() {
        return instance;
    }

    public PlayerAccount getPlayerAccount(final OfflinePlayer offlinePlayer) {
        return new PlayerAccount(this, offlinePlayer);
    }

    public TownyAccount getTownyAccount(final String name) {
        return new TownyAccount(this, name);
    }

    public boolean hasTownyAccount(final String name) {
        //TODO
        return false;
    }

    public boolean hasPlayerAccount(final OfflinePlayer offlinePlayer) {
        //TODO
        return false;
    }
}
