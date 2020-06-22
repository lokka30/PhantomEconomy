package io.github.lokka30.phantomeconomy_v2.hooks;

import io.github.lokka30.phantomeconomy_v2.PhantomEconomy;
import io.github.lokka30.phantomeconomy_v2.api.EconomyManager;
import io.github.lokka30.phantomeconomy_v2.api.accounts.AccountManager;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy_v2.api.exceptions.OversizedWithdrawAmountException;
import io.github.lokka30.phantomeconomy_v2.utils.LogLevel;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("unused")
public class VaultProvider extends AbstractEconomy {

    private PhantomEconomy instance;
    private EconomyManager economyManager;
    private AccountManager accountManager;

    public VaultProvider(final PhantomEconomy instance) {
        this.instance = instance;
        this.economyManager = instance.getEconomyManager();
        this.accountManager = instance.getAccountManager();
    }

    @Override
    public boolean isEnabled() {
        return instance.isEnabled();
    }

    @Override
    public String getName() {
        return "PhantomEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    } //TODO this should change when bank support is actually added.

    @Override
    public int fractionalDigits() {
        return -1;
    }

    @Override
    public String format(double balance) {
        try {
            return economyManager.getVaultCurrency().formatFinalBalance(balance);
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return Double.toString(balance);
    }

    @Override
    public String currencyNamePlural() {
        try {
            return economyManager.getVaultCurrency().getPlural();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return "dollars";
    }

    @Override
    public String currencyNameSingular() {
        try {
            return economyManager.getVaultCurrency().getSingular();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return "dollar";
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAccount(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        // Check if it is a player or not first.
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            try {
                return accountManager.hasPlayerAccount(offlinePlayer);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.hasNonPlayerAccount(name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        try {
            return accountManager.hasPlayerAccount(offlinePlayer);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean hasAccount(String name, String worldName) {
        return hasAccount(name);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String worldName) {
        return hasAccount(offlinePlayer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public double getBalance(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        //Check if it is a player account first
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            try {
                return accountManager.getPlayerAccount(offlinePlayer).getBalance(economyManager.getVaultCurrency());
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.getNonPlayerAccount(name).getBalance(economyManager.getVaultCurrency());
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        }

        //In case the vault currency setting is invalid, it will return 0.00.
        return 0.00;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        try {
            return accountManager.getPlayerAccount(offlinePlayer).getBalance(economyManager.getVaultCurrency());
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }

        //In case the vault currency setting is invalid, it will return 0.00.
        return 0.00;
    }

    @Override
    public double getBalance(String name, String worldName) {
        return getBalance(name);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String world) {
        return getBalance(offlinePlayer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean has(String name, double amount) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            try {
                return accountManager.getPlayerAccount(offlinePlayer).has(economyManager.getVaultCurrency(), amount);
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.getNonPlayerAccount(name).has(economyManager.getVaultCurrency(), amount);
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        }

        //In case the vault currency setting is invalid, it will return false.
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        try {
            return accountManager.getPlayerAccount(offlinePlayer).has(economyManager.getVaultCurrency(), amount);
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }

        //In case the vault currency setting is invalid, it will return false.
        return false;
    }

    @Override
    public boolean has(String name, String worldName, double amount) {
        return has(name, amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String worldName, double amount) {
        return has(offlinePlayer, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse withdrawPlayer(String name, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "You can't withdraw a negative amount.");
        }

        if (has(name, amount)) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

            if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                try {
                    accountManager.getPlayerAccount(offlinePlayer).withdraw(economyManager.getVaultCurrency(), amount);
                } catch (NegativeAmountException | OversizedWithdrawAmountException | InvalidCurrencyException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    accountManager.getNonPlayerAccount(name).withdraw(economyManager.getVaultCurrency(), amount);
                } catch (InvalidCurrencyException | NegativeAmountException | OversizedWithdrawAmountException e) {
                    e.printStackTrace();
                }
            }
            return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.SUCCESS, "Success");
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "The account holder has insufficient funds");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "You can't withdraw a negative amount.");
        }

        try {
            accountManager.getPlayerAccount(offlinePlayer).withdraw(economyManager.getVaultCurrency(), amount);
        } catch (NegativeAmountException | InvalidCurrencyException | OversizedWithdrawAmountException e) {
            e.printStackTrace();
        }
        return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Success");
    }

    @Override
    public EconomyResponse withdrawPlayer(String name, String world, double amount) {
        return withdrawPlayer(name, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse depositPlayer(String name, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "You can't deposit a negative amount.");
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            try {
                accountManager.getPlayerAccount(Bukkit.getOfflinePlayer(name)).deposit(economyManager.getVaultCurrency(), amount);
            } catch (NegativeAmountException | InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                accountManager.getNonPlayerAccount(name).deposit(economyManager.getVaultCurrency(), amount);
            } catch (InvalidCurrencyException | NegativeAmountException e) {
                e.printStackTrace();
            }
        }
        return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.SUCCESS, "Success");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "You can't deposit a negative amount.");
        }

        try {
            accountManager.getPlayerAccount(offlinePlayer).deposit(economyManager.getVaultCurrency(), amount);
        } catch (NegativeAmountException | InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Success");
    }

    @Override
    public EconomyResponse depositPlayer(String name, String worldName, double amount) {
        return depositPlayer(name, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String worldName, double amount) {
        return depositPlayer(offlinePlayer, amount);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse isBankMember(String name, String worldName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "PE does not support banks.");
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean createPlayerAccount(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        if (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore()) {
            createPlayerAccount(Bukkit.getOfflinePlayer(name));
            return true;
        } else {
            try {
                if (accountManager.hasNonPlayerAccount(name)) {
                    return false;
                } else {
                    try {
                        accountManager.createNonPlayerAccount(name);
                        return true;
                    } catch (AccountAlreadyExistsException | SQLException | InvalidCurrencyException e) {
                        instance.getUtils().log(LogLevel.WARNING, "A plugin using the Vault API has tried to run createPlayerAccount(Str) but the NonPlayerAccount already exists. The developer should check if this is the case before doing so.");
                        e.printStackTrace();
                        return false;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        try {
            if (accountManager.hasPlayerAccount(offlinePlayer)) {
                return false;
            } else {
                try {
                    accountManager.createPlayerAccount(offlinePlayer);
                    return true;
                } catch (AccountAlreadyExistsException | SQLException | InvalidCurrencyException e) {
                    instance.getUtils().log(LogLevel.WARNING, "A plugin using the Vault API has tried to run createPlayerAccount(offP) but the player already has an account. The developer should check if this is the case before doing so.");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean createPlayerAccount(String name, String worldName) {
        return createPlayerAccount(name);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String worldName) {
        return createPlayerAccount(offlinePlayer);
    }
}
