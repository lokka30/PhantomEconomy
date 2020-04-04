package io.github.lokka30.phantomeconomy;

import io.github.lokka30.phantomeconomy.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultImplementer implements Economy {

    private PhantomEconomy instance;

    public VaultImplementer(PhantomEconomy instance) {
        this.instance = instance;
    }

    @Override
    public boolean isEnabled() {
        return instance.isEnabled();
    }

    @Override
    public String getName() {
        return instance.getDescription().getName();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        String amountString = Utils.roundToString(amount);

        boolean useSymbol = false;
        boolean useWord = false;
        //boolean useBoth = false;

        switch (instance.settings.get("currency.mode", "SYMBOL")) {
            case "SYMBOL":
                useSymbol = true;
                break;
            case "WORD":
                useWord = true;
                break;
            case "BOTH":
                //Redundant assignment for the current values. Uncomment if needed, and make sure you check if(useBoth) in the area below.
                //useBoth = true;
                break;
            default:
                instance.getLogger().warning("Invalid setting 'currency.mode'. Using default value 'SYMBOL'.");
                useSymbol = true;
                break;
        }

        if (useSymbol) {
            return String.format("%s%s", instance.settings.get("currency.symbol", "$"), amountString);
        } else if (useWord) {
            String format;

            if (instance.settings.get("currency.word.prefix-space", true)) {
                format = "%s %s";
            } else {
                format = "%s%s";
            }

            if (amount == 1) {
                return String.format(format, amountString, instance.settings.get("currency.word.singular", "dollar"));
            } else {
                return String.format(format, amountString, instance.settings.get("currency.word.plural", "dollars"));
            }
        } else {
            //useBoth = true
            String format;

            if (instance.settings.get("currency.word.prefix-space", true)) {
                format = "%s%s %s"; //$25 dollars
            } else {
                format = "%s%s%s"; //$25dollars
            }

            String symbol = instance.settings.get("currency.symbol", "$");

            if (amount == 1) {
                return String.format(format, symbol, amountString, instance.settings.get("currency.word.singular", "dollar"));
            } else {
                return String.format(format, symbol, amountString, instance.settings.get("currency.word.plural", "dollars"));
            }
        }
    }

    @Override
    public String currencyNamePlural() {
        return instance.settings.get("currency.plural", "dollars");
    }

    @Override
    public String currencyNameSingular() {
        return instance.settings.get("currency.singular", "dollar");
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAccount(String name) {
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        if (!offlinePlayer.hasPlayedBefore()) {
            return false;
        } else {
            return hasAccount(offlinePlayer);
        }
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return instance.data.get("players." + offlinePlayer.getUniqueId().toString() + ".balance", null) != null;
    }

    @Override
    public boolean hasAccount(String name, String s1) {
        return hasAccount(name);
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return hasAccount(offlinePlayer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public double getBalance(String name) {
        return getBalance(Bukkit.getOfflinePlayer(name));
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        if (instance.balanceCache.containsKey(offlinePlayer)) {
            return instance.balanceCache.get(offlinePlayer);
        } else {
            return instance.data.get("players." + offlinePlayer.getUniqueId().toString() + ".balance", 0.0D);
        }
    }

    @Override
    public double getBalance(String name, String s1) {
        return getBalance(name);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean has(String name, double amount) {
        return has(Bukkit.getOfflinePlayer(name), amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double amount) {
        return getBalance(offlinePlayer) >= amount;
    }

    @Override
    public boolean has(String name, String world, double amount) {
        return has(name, amount);
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String world, double amount) {
        return has(offlinePlayer, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse withdrawPlayer(String name, double amount) {
        return withdrawPlayer(Bukkit.getOfflinePlayer(name), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        amount = Utils.round(amount);

        if (hasAccount(offlinePlayer)) {
            if (has(offlinePlayer, amount)) {
                final double total = getBalance(offlinePlayer) - amount;
                instance.data.set("players." + offlinePlayer.getUniqueId().toString() + ".balance", total);
                instance.balanceCache.put(offlinePlayer, total);
                return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Funds withdrawn from account.");
            } else {
                return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "Account lacking funds.");
            }
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account not found.");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String name, String world, double amount) {
        return withdrawPlayer(name, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String world, double amount) {
        return withdrawPlayer(offlinePlayer, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse depositPlayer(String name, double amount) {
        return depositPlayer(Bukkit.getOfflinePlayer(name), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        amount = Utils.round(amount);

        if (hasAccount(offlinePlayer)) {
            final double total = getBalance(offlinePlayer) + amount;
            instance.data.set("players." + offlinePlayer.getUniqueId().toString() + ".balance", total);
            instance.balanceCache.put(offlinePlayer, total);
            return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Funds deposited to account.");
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account not found.");
        }
    }

    @Override
    public EconomyResponse depositPlayer(String name, String world, double amount) {
        return depositPlayer(name, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String world, double amount) {
        return depositPlayer(offlinePlayer, amount);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not implemented into PhantomEconomy.");
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean createPlayerAccount(String s) {
        return createPlayerAccount(Bukkit.getOfflinePlayer(s));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        if (hasAccount(offlinePlayer)) {
            return false;
        } else {
            final double defaultBalance = instance.settings.get("default-balance", 50.0);

            instance.data.set("players." + offlinePlayer.getUniqueId().toString() + ".balance", Utils.round(defaultBalance));
            return true;
        }
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return createPlayerAccount(s);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return createPlayerAccount(offlinePlayer);
    }
}
