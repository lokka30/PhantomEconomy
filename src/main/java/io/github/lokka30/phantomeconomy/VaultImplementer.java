package io.github.lokka30.phantomeconomy;

import com.palmergames.bukkit.towny.TownySettings;
import io.github.lokka30.phantomeconomy.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

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
        if (isTowny(name)) {
            return instance.data.get("towny." + name + ".balance", null) != null;
        } else {
            return hasAccount(Bukkit.getOfflinePlayer(name));
        }
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return offlinePlayer.hasPlayedBefore();
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
        if (isTowny(name)) {
            return instance.data.get("towny." + name + ".balance", 0.00D);
        } else {
            return getBalance(Bukkit.getOfflinePlayer(name));
        }
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

    @SuppressWarnings("unused")
    public double getBalance(String name, World world) {
        return getBalance(name);
    }

    @SuppressWarnings("unused")
    public void removeAccount(String name) {
        setBalance(name, instance.getDefaultBalance());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean has(String name, double amount) {
        if (isTowny(name)) {
            return getBalance(name) >= amount;
        } else {
            return has(Bukkit.getOfflinePlayer(name), amount);
        }
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

    @SuppressWarnings("unused")
    public boolean hasEnough(String name, double amount, World world) {
        return has(name, amount);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse withdrawPlayer(String name, double amount) {
        if (isTowny(name)) {
            amount = Utils.round(amount);

            if (hasAccount(name)) {
                final double total = getBalance(name) - amount;
                instance.data.set("towny." + name + ".balance", total);
                return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.SUCCESS, "Funds withdrawn from account.");
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account not found.");
            }
        } else {
            return withdrawPlayer(Bukkit.getOfflinePlayer(name), amount);
        }
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

    @SuppressWarnings("unused")
    public boolean subtract(String name, double amount, World world) {
        return withdrawPlayer(name, world.getName(), amount).transactionSuccess();
    }

    @Override
    @SuppressWarnings("deprecation")
    public EconomyResponse depositPlayer(String name, double amount) {
        if (isTowny(name)) {
            amount = Utils.round(amount);

            if (hasAccount(name)) {
                final double total = getBalance(name) + amount;
                instance.data.set("towny." + name + ".balance", total);
                return new EconomyResponse(amount, getBalance(name), EconomyResponse.ResponseType.SUCCESS, "Funds deposited to account.");
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Account not found.");
            }
        } else {
            return depositPlayer(Bukkit.getOfflinePlayer(name), amount);
        }
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

    @SuppressWarnings("unused")
    public boolean add(String name, double amount, World world) {
        return depositPlayer(name, world.getName(), amount).transactionSuccess();
    }

    public boolean setBalance(String name, Double amount) {
        boolean withdrawSuccess = withdrawPlayer(name, getBalance(name)).transactionSuccess();
        boolean depositSuccess = depositPlayer(name, amount).transactionSuccess();

        return withdrawSuccess && depositSuccess;
    }

    @SuppressWarnings("unused")
    public boolean setBalance(String name, Double amount, World world) {
        return setBalance(name, amount);
    }

    @SuppressWarnings("unused")
    public String getFormattedBalance(double balance) {
        return format(balance);
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
    public boolean createPlayerAccount(String name) {
        if (isTowny(name)) {
            if (hasAccount(name)) {
                return false;
            } else {
                instance.data.set("towny." + name + ".balance", 0);
                return true;
            }
        } else {
            return createPlayerAccount(Bukkit.getOfflinePlayer(name));
        }
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        if (hasAccount(offlinePlayer)) {
            return false;
        } else {
            instance.data.set("players." + offlinePlayer.getUniqueId().toString() + ".balance", Utils.round(instance.getDefaultBalance()));
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

    private boolean isTowny(String name) {
        if (instance.hasTownyCompatibility) {
            //Check if the name specified has a town or nation account prefix.
            return name.startsWith(TownySettings.getTownAccountPrefix()) || name.startsWith(TownySettings.getNationAccountPrefix());
        } else {
            //Towny isn't installed. Don't try to ask for Towny stuff when it isn't installed!
            return false;
        }
    }
}
