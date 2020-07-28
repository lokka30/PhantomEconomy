package io.github.lokka30.phantomeconomy.hooks;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import io.github.lokka30.phantomeconomy.api.AccountManager;
import io.github.lokka30.phantomeconomy.api.CurrencyManager;
import io.github.lokka30.phantomeconomy.api.currencies.Currency;
import io.github.lokka30.phantomeconomy.api.exceptions.AccountAlreadyExistsException;
import io.github.lokka30.phantomeconomy.api.exceptions.InvalidCurrencyException;
import io.github.lokka30.phantomeconomy.api.exceptions.NegativeAmountException;
import io.github.lokka30.phantomeconomy.api.exceptions.OversizedWithdrawAmountException;
import io.github.lokka30.phantomeconomy.enums.AccountType;
import io.github.lokka30.phantomlib.enums.LogLevel;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
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
            return Double.toString(balance);
        }
    }

    @Override
    public String currencyNamePlural() {
        try {
            return currencyManager.getVaultCurrency().getPlural();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
            return "dollars";
        }
    }

    @Override
    public String currencyNameSingular() {
        try {
            return currencyManager.getVaultCurrency().getSingular();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
            return "dollar";
        }
    }

    @Override
    public boolean hasAccount(String name) {
        Currency currency;

        try {
            currency = currencyManager.getVaultCurrency();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
            return false;
        }

        if (instance.getDatabase().isUsernameCached(name)) {
            return accountManager.hasPlayerAccount(instance.getDatabase().getUUIDFromUsername(name), currency);
        } else {
            return accountManager.hasNonPlayerAccount(name, currency);
        }
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        try {
            return accountManager.hasPlayerAccount(offlinePlayer.getUniqueId(), currencyManager.getVaultCurrency());
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
    public double getBalance(String name) {
        Currency currency;
        try {
            currency = currencyManager.getVaultCurrency();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
            return 0.00;
        }

        if (instance.getDatabase().isUsernameCached(name)) {
            return accountManager.getPlayerAccount(instance.getDatabase().getUUIDFromUsername(name)).getBalance(currency);
        } else {
            return accountManager.getNonPlayerAccount(name).getBalance(currency);
        }
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        Currency currency;
        try {
            currency = currencyManager.getVaultCurrency();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
            return 0.00;
        }

        return accountManager.getPlayerAccount(offlinePlayer.getUniqueId()).getBalance(currency);
    }

    @Override
    public double getBalance(String name, String worldName) {
        return getBalance(name);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String worldName) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String name, double amount) {
        Currency currency;
        try {
            currency = currencyManager.getVaultCurrency();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
            return false;
        }

        if (instance.getDatabase().isUsernameCached(name)) {
            return accountManager.getPlayerAccount(instance.getDatabase().getUUIDFromUsername(name)).has(currency, amount);
        } else {
            return accountManager.getNonPlayerAccount(name).has(currency, amount);
        }
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        Currency currency;
        try {
            currency = currencyManager.getVaultCurrency();
        } catch (InvalidCurrencyException e) {
            e.printStackTrace();
            return false;
        }

        return accountManager.getPlayerAccount(offlinePlayer.getUniqueId()).has(currency, amount);
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
    public EconomyResponse withdrawPlayer(String name, double amount) {
        if (has(name, amount)) {
            Currency currency;
            try {
                currency = currencyManager.getVaultCurrency();
            } catch (InvalidCurrencyException e) {
                instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Invalid vault currency specified in the settings.yml file. Fix this ASAP!");
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Invalid Vault Currency in configuration");
            }

            if (instance.getDatabase().isUsernameCached(name)) {
                try {
                    accountManager.getPlayerAccount(instance.getDatabase().getUUIDFromUsername(name)).withdraw(currency, amount);
                } catch (NegativeAmountException e) {
                    instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Another plugin using the Vault API attempted to withdraw funds from an account, but the amount specified was negative. Please inform the plugin developer.");
                    return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.FAILURE, "Negative amount specified");
                } catch (OversizedWithdrawAmountException e) {
                    instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Another plugin using the Vault API attempted to withdraw funds from an account, but the amount specified was more than the account's balance. Please inform the plugin developer.");
                    return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.FAILURE, "Oversized withdrawal");
                }
            } else {
                try {
                    accountManager.getNonPlayerAccount(name).withdraw(currency, amount);
                } catch (NegativeAmountException e) {
                    instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Another plugin using the Vault API attempted to withdraw funds from an account, but the amount specified was negative. Please inform the plugin developer.");
                    return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.FAILURE, "Negative amount specified");
                } catch (OversizedWithdrawAmountException e) {
                    instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Another plugin using the Vault API attempted to withdraw funds from an account, but the amount specified was more than the account's balance. Please inform the plugin developer.");
                    return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.FAILURE, "Oversized withdrawal");
                }
            }
            return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.SUCCESS, "Success");
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "The account holder has insufficient funds");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        try {
            accountManager.getPlayerAccount(offlinePlayer.getUniqueId()).withdraw(currencyManager.getVaultCurrency(), amount);
        } catch (NegativeAmountException e) {
            instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Another plugin using the Vault API attempted to withdraw funds from an account, but the amount specified was negative. Please inform the plugin developer.");
            return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Negative amount specified");
        } catch (InvalidCurrencyException e) {
            instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Invalid vault currency specified in the settings.yml file. Fix this ASAP!");
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Invalid Vault Currency in configuration");
        } catch (OversizedWithdrawAmountException e) {
            instance.getPhantomLogger().log(LogLevel.SEVERE, instance.PREFIX, "Another plugin using the Vault API attempted to withdraw funds from an account, but the amount specified was more than the account's balance. Please inform the plugin developer.");
            return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Oversized withdrawal");
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
        //TODO UPDATE
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            try {
                accountManager.getPlayerAccount(offlinePlayer.getUniqueId()).deposit(currencyManager.getVaultCurrency(), amount);
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
            accountManager.getPlayerAccount(offlinePlayer.getUniqueId()).deposit(currencyManager.getVaultCurrency(), amount);
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
    public EconomyResponse deleteBank(String accountId) {
        try {
            instance.getDatabase().deleteBankAccount(accountId);
        } catch (InvalidCurrencyException | SQLException e) {
            e.printStackTrace();
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse bankBalance(String ownerId) {
        double balance = 0;
        try {
            balance = instance.getAccountManager().getBankAccountFromId(ownerId).getBalance(instance.getCurrencyManager().getVaultCurrency());
        } catch (InvalidCurrencyException | SQLException e) {
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
        } catch (InvalidCurrencyException | SQLException e) {
            e.printStackTrace();
        }

        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An internal error occurred.");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        try {
            instance.getAccountManager().getBankAccountFromId(s).withdraw(instance.getCurrencyManager().getVaultCurrency(), v);
            return new EconomyResponse(v, 0, EconomyResponse.ResponseType.SUCCESS, "attempt made");
        } catch (NegativeAmountException | OversizedWithdrawAmountException | InvalidCurrencyException | SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Exception occurred");
        }
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        try {
            instance.getAccountManager().getBankAccountFromId(s).deposit(instance.getCurrencyManager().getVaultCurrency(), v);
            return new EconomyResponse(v, 0, EconomyResponse.ResponseType.SUCCESS, "attempt made");
        } catch (NegativeAmountException | InvalidCurrencyException | SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(v, 0, EconomyResponse.ResponseType.FAILURE, "Exception occurred");
        }
    }

    @Override
    public EconomyResponse isBankOwner(String bankId, String ownerId) {
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerId);
        if (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore()) {
            return isBankOwner(bankId, offlinePlayer);
        } else {
            try {
                if (instance.getDatabase().isBankOwner(bankId, AccountType.NonPlayerAccount, ownerId)) {
                    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "is owner");
                } else {
                    return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "not owner");
                }
            } catch (InvalidCurrencyException | SQLException e) {
                e.printStackTrace();
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "an internal error occurred");
            }
        }
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        try {
            if (instance.getDatabase().isBankOwner(name, AccountType.PlayerAccount, player.getUniqueId().toString())) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "is owner");
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "not owner");
            }
        } catch (InvalidCurrencyException | SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "an internal error occurred");
        }
    }

    @Override
    public EconomyResponse isBankMember(String bankId, String ownerId) {
        return isBankOwner(bankId, ownerId);
    }

    @Override
    public EconomyResponse isBankMember(String bankId, OfflinePlayer offlinePlayer) {
        return isBankOwner(bankId, offlinePlayer);
    }

    @Override
    public List<String> getBanks() {
        try {
            return instance.getDatabase().getBankAccounts();
        } catch (SQLException | InvalidCurrencyException e) {
            e.printStackTrace();
        }

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
            if (accountManager.hasPlayerAccount(offlinePlayer.getUniqueId(), instance.getCurrencyManager().getVaultCurrency())) {
                return false;
            } else {
                try {
                    accountManager.createPlayerAccount(offlinePlayer.getUniqueId(), instance.getCurrencyManager().getVaultCurrency());
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
