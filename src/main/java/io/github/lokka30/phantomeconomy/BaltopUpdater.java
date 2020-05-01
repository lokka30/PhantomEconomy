package io.github.lokka30.phantomeconomy;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BaltopUpdater {

    private Map<String, Double> baltopMap = new HashMap<>();

    /*
    Credit: Big thanks to Dkbay for providing the baltop code.
     */

    @SuppressWarnings("unchecked")
    public void update() {
        JSONParser parser = new JSONParser();
        final String path = "plugins/PhantomEconomy/";
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(path + "old/data.json"));
            JSONObject players = (JSONObject) jsonObject.get("players");
            if (players == null) {
                return;
            }
            ArrayList<String> uuids = new ArrayList<String>(players.keySet());
            Map<String, Double> updatedTreeMap = new HashMap<>();

            for (String uuid : uuids) {
                JSONObject playerBalance = (JSONObject) players.get(uuid);
                double balance = Double.parseDouble(playerBalance.get("balance").toString());
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
