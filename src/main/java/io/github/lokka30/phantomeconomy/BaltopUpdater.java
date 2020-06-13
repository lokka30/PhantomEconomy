package io.github.lokka30.phantomeconomy;

import io.github.lokka30.phantomeconomy.utils.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class BaltopUpdater {

    // Credit to Dkbay for their baltop code.

    private PhantomEconomy instance;

    public BaltopUpdater(PhantomEconomy instance) {
        this.instance = instance;
    }

    private Map<String, Double> baltopMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void update() {
        JSONParser parser = new JSONParser();
        final String path = "plugins/PhantomEconomy/";
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(path + "data.json"));
            JSONObject players = (JSONObject) jsonObject.get("players");
            if (players == null) {
                return;
            }
            ArrayList<String> uuids = new ArrayList<String>(players.keySet());
            Map<String, Double> updatedTreeMap = new HashMap<>();

            for (String uuid : uuids) {
                JSONObject playerBalance = (JSONObject) players.get(uuid);
                Object playerBalanceValue = playerBalance.get("balance");
                double balance = instance.getDefaultBalance();

                if (playerBalanceValue == null) {
                    instance.log(LogLevel.INFO, "[Baltop Task] Error: Unable to retrieve balance of UUID '" + uuid + "'@");

                    if (instance.settings.get("baltop-update-task-error-repair", true)) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                            instance.log(LogLevel.INFO, "[Baltop Task] [Repair] Attempting to repair the user's balance...");
                            instance.data.set("players." + uuid + ".balance", instance.getDefaultBalance());
                            balance = instance.getDefaultBalance();
                        } else {
                            instance.log(LogLevel.INFO, "[Baltop Task] [Repair] Couldn't repair the user's balance as they haven't joined before. Removing their account from the data file.");
                            instance.data.set("players." + uuid, null);
                            continue;
                        }
                    }
                } else {
                    balance = Double.parseDouble(playerBalanceValue.toString());
                }

                updatedTreeMap.put(uuid, balance);
            }

            final Map<String, Double> newBaltop = updatedTreeMap.entrySet()
                    .stream()
                    .sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            updateBaltop(newBaltop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBaltop(Map<String, Double> tm) {
        baltopMap = tm;
    }

    public Map<String, Double> getBaltop() {
        return baltopMap;
    }
}
