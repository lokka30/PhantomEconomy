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
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(path + "data.json"));
            JSONObject players = (JSONObject) jsonObject.get("players");
            ArrayList<String> keys = new ArrayList<String>(players.keySet());
            Map<String, Double> updatedTreeMap = new HashMap<>();

            for (String key : keys) {
                JSONObject JSONBalance = (JSONObject) players.get(key);
                double balance = Double.parseDouble(JSONBalance.get("balance").toString());
                updatedTreeMap.put(key, balance);
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
