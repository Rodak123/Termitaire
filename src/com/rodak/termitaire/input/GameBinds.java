package com.rodak.termitaire.input;


import com.rodak.termitaire.game.settings.GameSettings;

public class GameBinds {
    public static String[] Waste = new String[]{"o"};
    public static String[] Stock = new String[]{"p"};
    public static String[] Tableau = new String[]{"a", "s", "d", "f", "j", "k", "l"};
    public static String[] Foundations = new String[]{"q", "w", "e", "r"};
    public static String[] Unselect = new String[]{"_"};


    public static void loadBindsFromSettings() {
        GameSettings settings = GameSettings.getInstance();

        String[] binds;

        binds = settings.getSetting("binds/waste").getStringVal().split(" ");
        if (binds.length > 0) {
            Waste = binds;
        } else {
            System.out.println("Waste binds are not valid, must have at least one");
        }

        binds = settings.getSetting("binds/stock").getStringVal().split(" ");
        if (binds.length > 0) {
            Stock = binds;
        } else {
            System.out.println("Stock binds are not valid, must have at least one");
        }

        binds = settings.getSetting("binds/tableau").getStringVal().split(" ");
        if (binds.length == Tableau.length) {
            Tableau = binds;
        } else {
            System.out.println("Tableau binds are not valid, must have exactly " + Tableau.length);
        }

        binds = settings.getSetting("binds/foundations").getStringVal().split(" ");
        if (binds.length == Foundations.length) {
            Foundations = binds;
        } else {
            System.out.println("Foundations binds are not valid, must have exactly " + Foundations.length);
        }

        binds = settings.getSetting("binds/unselect").getStringVal().split(" ");
        if (binds.length > 0) {
            Unselect = binds;
        } else {
            System.out.println("Unselect binds are not valid, must have at least one");
        }
    }
}
