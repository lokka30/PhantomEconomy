package io.github.lokka30.phantomeconomy.utils;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class EconomyImplementer implements Economy {

    private PhantomEconomy instance = PhantomEconomy.getInstance();

    @Override
    public boolean isEnabled() {
        return instance != null;
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
        return 2;
    }

    @Override
    public String format(double amount) {
        switch (instance.settings.get("currency-format.using", "SYMBOL")) {
            case "WORD":
                String format;
                if (instance.settings.get("currency-format.word.use-space", true)) {
                    format = "%s %s";
                } else {
                    format = "%s%s";
                }

                if (amount == 1) {
                    return String.format(format, amount, currencyNameSingular());
                } else {
                    return String.format(format, amount, currencyNamePlural());
                }
            case "SYMBOL":
                return String.format("%s%s", instance.settings.get("currency-format.symbol", "$"), amount);
            default:
                instance.log(LogLevel.SEVERE, "Invalid setting &acurrency-format.using&7, please set it to an available currency format. Using default &aSYMBOL&7.");
                return String.format("%s%s", instance.settings.get("currency-format.symbol", "$"), amount);
        }
    }

    @Override
    public String currencyNamePlural() {
        return instance.settings.get("currency-format.word.plural", "dollars");
    }

    @Override
    public String currencyNameSingular() {
        return instance.settings.get("currency-format.word.singular", "dollar");
    }

    @Override
    public boolean hasAccount(String s) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return offlinePlayer.hasPlayedBefore();
    }

    @Override
    public double getBalance(String s) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        return instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
    }

    @Override
    public double getBalance(String s, String s1) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        return instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
    }

    @Override
    public boolean has(String s, double v) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        double balance = instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
        return balance > v;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        double balance = instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
        return balance > v;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        double balance = instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
        return balance > v;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        double balance = instance.getEconomyManager().getBalance(offlinePlayer.getUniqueId());
        return balance > v;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        instance.getEconomyManager().removeBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        instance.getEconomyManager().removeBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double v) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        instance.getEconomyManager().removeBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        instance.getEconomyManager().removeBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        instance.getEconomyManager().addBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        instance.getEconomyManager().addBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double v) {
        @SuppressWarnings("deprecation") OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
        instance.getEconomyManager().addBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double v) {
        instance.getEconomyManager().addBalance(offlinePlayer.getUniqueId(), v);
        return new EconomyResponse(v, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "success");
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "bank not implemented");
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}