package io.github.lokka30.phantomeconomy.listeners;

import io.github.lokka30.phantomeconomy.PhantomEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private PhantomEconomy instance;

    public JoinListener(PhantomEconomy instance) {
        this.instance = instance;
    }

    /**
     * This event checks when a player joins the server.
     *
     * @param e the player join event
     */
    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player player = e.getPlayer();

        createAccountIfNotExists(player);
        setBalanceCacheOnJoin(player);
        setLatestNameToUUID(player);
    }

    /**
     * If the player doesn't have a PhantomEconomy account, then it will be created for them.
     *
     * @param player the player to check (and create, if they don't have an account)
     */
    private void createAccountIfNotExists(Player player) {
        if (!instance.provider.hasAccount(player)) {
            instance.provider.createPlayerAccount(player);
        }
    }

    /**
     * If the balance cache hasn't already stored their balance, then it will insert their balance when they join.
     *
     * @param player the player to manage in the balance cache.
     */
    private void setBalanceCacheOnJoin(Player player) {

        // Make sure they aren't in there already. This step isn't required but it is there anyways.
        if (!instance.balanceCache.containsKey(player)) {

            // This is the path in the data file of the player's balance.
            final String path = "players." + player.getUniqueId().toString() + ".balance";

            // This is the default balance value, used in case their current balance is unavailable or non-existent (first join).
            final double defaultBalance = instance.getDefaultBalance();

            // Check if the data file contains the player's balance already.
            if (instance.data.contains(path)) {
                // Yep, it does. Store the balance in the file into the balance cache. If for some reason the balance in the file is null, then the default balance will be used.
                instance.balanceCache.put(player, instance.data.get(path, defaultBalance));
            } else {
                // Nope, it doesn't. Get the default balance and set their balance as that value. Store that in the data file.
                instance.balanceCache.put(player, defaultBalance);
                instance.data.set(path, defaultBalance);
            }
        }
    }

    /**
     * Will update the player's last username in the data file to their current username. Makes it easier to diagnose bugs.
     *
     * @param player the player to set their last username for
     */
    private void setLatestNameToUUID(Player player) {
        instance.data.set("players." + player.getUniqueId().toString() + ".lastUsername", player.getName());
    }
}
