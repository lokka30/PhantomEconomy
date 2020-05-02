package io.github.lokka30.phantomeconomy_v2.hooks;

import com.palmergames.bukkit.towny.TownySettings;
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

import java.util.List;

public class VaultHook extends AbstractEconomy {

    private PhantomEconomy instance;
    private EconomyManager economyManager;
    private AccountManager accountManager;

    public VaultHook(final PhantomEconomy instance) {
        this.instance = instance;
        this.economyManager = instance.economyManager;
        this.accountManager = instance.accountManager;
    }

    private boolean isTowny(String name) {
        if (instance.isTownyCompatibilityEnabled) {
            //Check if the name specified has a town or nation account prefix.
            return name.startsWith(TownySettings.getTownAccountPrefix()) || name.startsWith(TownySettings.getNationAccountPrefix());
        } else {
            //Towny isn't installed. Don't try to ask for Towny stuff when it isn't installed!
            return false;
        }
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
    }

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
        if (isTowny(name)) {
            return accountManager.hasTownyAccount(name);
        } else {
            return accountManager.hasPlayerAccount(Bukkit.getOfflinePlayer(name));
        }
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return accountManager.hasPlayerAccount(offlinePlayer);
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
        if (isTowny(name)) {
            try {
                return accountManager.getTownyAccount(name).getBalance(economyManager.getVaultCurrency());
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.getPlayerAccount(Bukkit.getOfflinePlayer(name)).getBalance(economyManager.getVaultCurrency());
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        }
        return 0.00;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        try {
            return accountManager.getPlayerAccount(offlinePlayer).getBalance(economyManager.getVaultCurrency());
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
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
        if (isTowny(name)) {
            try {
                return accountManager.getTownyAccount(name).has(economyManager.getVaultCurrency(), amount);
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.getPlayerAccount(Bukkit.getOfflinePlayer(name)).has(economyManager.getVaultCurrency(), amount);
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        try {
            return accountManager.getPlayerAccount(offlinePlayer).has(economyManager.getVaultCurrency(), amount);
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
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
            if (isTowny(name)) {
                try {
                    accountManager.getTownyAccount(name).withdraw(economyManager.getVaultCurrency(), amount);
                } catch (InvalidCurrencyException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    accountManager.getPlayerAccount(Bukkit.getOfflinePlayer(name)).withdraw(economyManager.getVaultCurrency(), amount);
                } catch (NegativeAmountException | OversizedWithdrawAmountException | InvalidCurrencyException e) {
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

        if (isTowny(name)) {
            try {
                accountManager.getTownyAccount(name).deposit(economyManager.getVaultCurrency(), amount);
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                accountManager.getPlayerAccount(Bukkit.getOfflinePlayer(name)).deposit(economyManager.getVaultCurrency(), amount);
            } catch (NegativeAmountException | InvalidCurrencyException e) {
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
        if (isTowny(name)) {
            if (accountManager.hasTownyAccount(name)) {
                return false;
            } else {
                try {
                    accountManager.getTownyAccount(name).createAccount();
                } catch (AccountAlreadyExistsException e) {
                    instance.utils.log(LogLevel.WARNING, "A plugin using the Vault API has tried to run createPlayerAccount(Str) but the town already has an account. The developer should check if this is the case before doing so.");
                    e.printStackTrace();
                }
                return true;
            }
        } else {
            createPlayerAccount(Bukkit.getOfflinePlayer(name));
        }
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        if (accountManager.hasPlayerAccount(offlinePlayer)) {
            return false;
        } else {
            try {
                accountManager.getPlayerAccount(offlinePlayer).createAccount();
            } catch (AccountAlreadyExistsException e) {
                instance.utils.log(LogLevel.WARNING, "A plugin using the Vault API has tried to run createPlayerAccount(offP) but the player already has an account. The developer should check if this is the case before doing so.");
                e.printStackTrace();
            }
            return true;
        }
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
