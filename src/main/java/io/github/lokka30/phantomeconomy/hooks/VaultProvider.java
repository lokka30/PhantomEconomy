package io.github.lokka30.phantomeconomy.hooks;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.AccountManager;
import io.github.lokka30.phantomeconomy.api.CurrencyManager;
import io.github.lokka30.phantomeconomy.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy.api.exceptions.OversizedWithdrawAmountException;
import io.github.lokka30.phantomeconomy.enums.AccountType;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

@SuppressWarnings("unused")
public class VaultProvider extends AbstractEconomy {

    private PhantomEconomy instance;
    private CurrencyManager currencyManager;
    private AccountManager accountManager;

    public VaultProvider(final PhantomEconomy instance) {
        this.instance = instance;
        this.currencyManager = instance.getCurrencyManager();
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
        return true;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double balance) {
        try {
            return currencyManager.getVaultCurrency().formatFinalBalance(balance);
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return Double.toString(balance);
    }

    @Override
    public String currencyNamePlural() {
        try {
            return currencyManager.getVaultCurrency().getPlural();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return "dollars";
    }

    @Override
    public String currencyNameSingular() {
        try {
            return currencyManager.getVaultCurrency().getSingular();
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
                return accountManager.hasPlayerAccount(offlinePlayer, instance.getCurrencyManager().getVaultCurrency());
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.hasNonPlayerAccount(name, instance.getCurrencyManager().getVaultCurrency());
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        try {
            return accountManager.hasPlayerAccount(offlinePlayer, instance.getCurrencyManager().getVaultCurrency());
        } catch (InvalidCurrencyException e) {
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
                return accountManager.getPlayerAccount(offlinePlayer).getBalance(currencyManager.getVaultCurrency());
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.getNonPlayerAccount(name).getBalance(currencyManager.getVaultCurrency());
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
            return accountManager.getPlayerAccount(offlinePlayer).getBalance(currencyManager.getVaultCurrency());
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
                return accountManager.getPlayerAccount(offlinePlayer).has(currencyManager.getVaultCurrency(), amount);
            } catch (InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return accountManager.getNonPlayerAccount(name).has(currencyManager.getVaultCurrency(), amount);
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
            return accountManager.getPlayerAccount(offlinePlayer).has(currencyManager.getVaultCurrency(), amount);
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
                    accountManager.getPlayerAccount(offlinePlayer).withdraw(currencyManager.getVaultCurrency(), amount);
                } catch (NegativeAmountException | OversizedWithdrawAmountException | InvalidCurrencyException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    accountManager.getNonPlayerAccount(name).withdraw(currencyManager.getVaultCurrency(), amount);
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
            accountManager.getPlayerAccount(offlinePlayer).withdraw(currencyManager.getVaultCurrency(), amount);
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
                accountManager.getPlayerAccount(Bukkit.getOfflinePlayer(name)).deposit(currencyManager.getVaultCurrency(), amount);
            } catch (NegativeAmountException | InvalidCurrencyException e) {
                e.printStackTrace();
            }
        } else {
            try {
                accountManager.getNonPlayerAccount(name).deposit(currencyManager.getVaultCurrency(), amount);
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
            accountManager.getPlayerAccount(offlinePlayer).deposit(currencyManager.getVaultCurrency(), amount);
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
    @SuppressWarnings("deprecation")
    public EconomyResponse createBank(String bankId, String ownerId) {
        AccountType accountType;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerId);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            accountType = AccountType.PlayerAccount;
            ownerId = offlinePlayer.getUniqueId().toString();
        } else {
            accountType = AccountType.NonPlayerAccount;
        }
        try {
            instance.getAccountManager().createBankAccount(bankId, instance.getCurrencyManager().getVaultCurrency(), accountType, ownerId);
        } catch (AccountAlreadyExistsException | InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse createBank(String bankId, OfflinePlayer player) {
        try {
            instance.getAccountManager().createBankAccount(bankId, instance.getCurrencyManager().getVaultCurrency(), AccountType.PlayerAccount, player.getUniqueId().toString());
        } catch (AccountAlreadyExistsException | InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse deleteBank(String ownerId) {
        //TODO
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse bankBalance(String ownerId) {
        double balance = 0;
        try {
            balance = instance.getAccountManager().getBankAccountFromId(ownerId).getBalance(instance.getCurrencyManager().getVaultCurrency());
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }
        return new EconomyResponse(balance, balance, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        try {
            if (instance.getAccountManager().getBankAccountFromId(s).getBalance(instance.getCurrencyManager().getVaultCurrency()) >= v) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Bank has more than or equal to specified funds");
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank does not have specified funds");
            }
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
        }

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An internal error occured.");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, ""); //todo
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
    }

    @Override
    public EconomyResponse isBankMember(String name, String worldName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "");
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
                if (accountManager.hasNonPlayerAccount(name, instance.getCurrencyManager().getVaultCurrency())) {
                    return false;
                } else {
                    try {
                        accountManager.createNonPlayerAccount(name, instance.getCurrencyManager().getVaultCurrency());
                        return true;
                    } catch (AccountAlreadyExistsException | InvalidCurrencyException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } catch (InvalidCurrencyException exception) {
                exception.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        try {
            if (accountManager.hasPlayerAccount(offlinePlayer, instance.getCurrencyManager().getVaultCurrency())) {
                return false;
            } else {
                try {
                    accountManager.createPlayerAccount(offlinePlayer, instance.getCurrencyManager().getVaultCurrency());
                    return true;
                } catch (AccountAlreadyExistsException | InvalidCurrencyException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (InvalidCurrencyException exception) {
            exception.printStackTrace();
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
