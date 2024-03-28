package com.rodak.termitaire;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;


public class GameBinds {
    public static String[] Waste = new String[]{"o"};
    public static String[] Stock = new String[]{"p"};
    public static String[] Tableau = new String[]{"a", "s", "d", "f", "j", "k", "l"};
    public static String[] Foundations = new String[]{"q", "w", "e", "r"};
    public static String[] Unselect = new String[]{"_"};


    public static void loadBindsFromFile() {
        // Load binds.yaml from the resources folder
        InputStream inputStream = GameBinds.class.getResourceAsStream("/binds.yaml");

        if (inputStream != null) {
            Yaml yaml = new Yaml();
            Map<String, String> data = yaml.load(inputStream);

            if (data != null) {
                Waste = getBinds("Waste", data, Waste);
                Stock = getBinds("Stock", data, Stock);
                Tableau = getBinds("Tableau", data, Tableau);
                Foundations = getBinds("Foundations", data, Foundations);
                Unselect = getBinds("Unselect", data, Unselect);
            }
        } else {
            System.out.println("Failed to load binds.yaml. Make sure it exists in the resources folder.");
        }
    }

    private static String[] getBinds(String key, Map<String, String> data, String[] defaultBinds) {
        if (data.containsKey(key))
            return data.get(key).strip().split(" ");
        return defaultBinds;
    }

}
