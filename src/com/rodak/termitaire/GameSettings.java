package com.rodak.termitaire;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameSettings {

    private static GameSettings gameSettings;
    private static final String settingSeparator = "|";

    public static GameSettings getInstance() {
        if (gameSettings == null) {
            gameSettings = new GameSettings();
        }
        return gameSettings;
    }

    public enum ValueType {
        STRING,
        INT,
        BOOLEAN
    }

    public static class Value {

        public final ValueType type;

        private String stringVal;

        public Value(int value) {
            this(ValueType.INT);
            setValue(value);
        }

        public Value(boolean value) {
            this(ValueType.BOOLEAN);
            setValue(value);
        }

        public Value(String value) {
            this(ValueType.STRING);
            setValue(value);
        }

        private Value(ValueType type) {
            this.type = type;
        }

        public static Value getValue(String type, String value) {
            for (ValueType valueType : ValueType.values()) {
                if (type.equalsIgnoreCase(valueType.name())) {
                    Value valueObject = new Value(valueType);
                    valueObject.setValue(value);
                    return valueObject;
                }
            }
            return null;
        }

        public boolean isSameType(String value) {
            return switch (type) {
                case STRING -> value.length() > 0;
                case INT -> isInteger(value, 10);
                case BOOLEAN -> "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
            };
        }

        private static boolean isInteger(String s, int radix) {
            if (s.isEmpty()) return false;
            for (int i = 0; i < s.length(); i++) {
                if (i == 0 && s.charAt(i) == '-') {
                    if (s.length() == 1) return false;
                    else continue;
                }
                if (Character.digit(s.charAt(i), radix) < 0) return false;
            }
            return true;
        }

        public void setValue(String value) {
            if (value.contains(settingSeparator)) {
                System.out.println("Can't use: " + settingSeparator);
                return;
            }
            stringVal = value;
        }

        public void setValue(int value) {
            setValue(String.valueOf(value));
        }

        public void setValue(boolean value) {
            setValue(String.valueOf(value));
        }

        public String getStringVal() {
            return stringVal;
        }

        public int getIntVal() {
            try {
                return Integer.parseInt(stringVal);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        public boolean getBoolVal() {
            return Boolean.parseBoolean(stringVal);
        }
    }

    public static final String SETTINGS_FILE_NAME = Termitaire.NAME.toLowerCase() + "_settings.txt";

    private final HashMap<String, Value> settings;

    public GameSettings() {
        settings = new HashMap<>();

        settings.put("binds/waste", new Value(String.join(" ", GameBinds.Waste)));
        settings.put("binds/stock", new Value(String.join(" ", GameBinds.Stock)));
        settings.put("binds/tableau", new Value(String.join(" ", GameBinds.Tableau)));
        settings.put("binds/foundations", new Value(String.join(" ", GameBinds.Foundations)));
        settings.put("binds/unselect", new Value(String.join(" ", GameBinds.Unselect)));

        settings.put("cards/spades", new Value("x"));
        settings.put("cards/hearts", new Value("V"));
        settings.put("cards/clubs", new Value("o"));
        settings.put("cards/diamonds", new Value("^"));
    }

    public void loadSettings() {
        Path path = getSettingsPath();

        HashMap<String, Value> loadedSettings = new HashMap<>();
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                String[] parts = line.split("\\" + settingSeparator);
                if (parts.length == 3) {
                    String type = parts[0];
                    String key = parts[1];
                    String value = parts[2];
                    Value valueObject = Value.getValue(type, value);
                    if (valueObject == null) {
                        System.out.println("Setting '" + key + "' can't be read and is ignored");
                    } else {
                        loadedSettings.put(key, valueObject);
                    }
                }
            });
        } catch (IOException e) {
            storeSettings();
        }
        settings.putAll(loadedSettings);
    }

    private Path getSettingsPath() {
        try {
            Path path = Path.of(Termitaire.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            if (path == null) {
                System.out.println("Can't access folder this application is in, therefore can't store settings");
                return null;
            }
            return path.resolve(SETTINGS_FILE_NAME);

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void storeSettings() {
        Path path = getSettingsPath();
        if (path == null) {
            return;
        }
        try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
            outputStream.write(this.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't access '" + ColoredString.colorizeString(path.getFileName().toString(), ColoredString.Color.RED) + "'");
        }
    }

    public void changeSettings() {
        boolean changedSettings = false;
        while (true) {
            Termitaire.clearScreen();
            listAllSettings();

            String settingKey = ActionInput.promptInput("What setting do you want to change (type the blue key or nothing to quit)? ").strip().toLowerCase();
            if (settingKey.length() == 0) {
                break;
            }

            if (!settings.containsKey(settingKey)) {
                System.out.println("Key: " + settingKey + " does not exist");
                continue;
            }

            Value settingValue = settings.get(settingKey);
            while (true) {
                String newValue = ActionInput.promptInput("What new value do you want to set? ").strip().toLowerCase();

                if (settingValue.isSameType(newValue)) {
                    settingValue.setValue(newValue);
                    changedSettings = true;
                    break;
                }
                System.out.println("Can't set '" + settingKey + "' to '" + newValue + "', " + settingValue.type.toString() + " is expected");
            }
        }

        if (!changedSettings) {
            System.out.println("Settings not modified.");
            return;
        }

        String input = ActionInput.promptInput("Save settings(Y/n)? ").strip().toLowerCase();
        if (input.length() == 0 || input.equals("y")) {
            System.out.println("Settings saved.");
            Termitaire.onSettingsUpdated(true);
        } else {
            System.out.println("Settings not modified.");
        }
    }

    private void listAllSettings() {
        ColoredString.Color groupColor = ColoredString.Color.YELLOW;
        ColoredString.Color bindKeyColor = ColoredString.Color.BLUE;

        LinkedHashMap<String, String> groups = new LinkedHashMap<>();

        groups.put("binds", "");
        groups.put("cards", "");
        groups.put("other", "");

        for (Map.Entry<String, Value> entry : settings.entrySet()) {
            String[] splitKey = entry.getKey().split("/");
            if (splitKey.length < 2) continue;
            String group = splitKey[0];
            String prettyEntry = "\n " + ColoredString.colorizeString(entry.getKey(), bindKeyColor) + ": " + entry.getValue().getStringVal();

            String groupValue = groups.getOrDefault(group, null);
            if (groupValue != null) {
                groups.put(group, groupValue + prettyEntry);
            } else {
                groups.put("other", groups.get("other") + prettyEntry);
            }
        }

        System.out.println();
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            if (entry.getValue().length() == 0) continue;
            String groupName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
            System.out.println(ColoredString.colorizeString(groupName, groupColor) + entry.getValue() + "\n");
        }
    }

    public Value getSetting(String key) {
        return settings.getOrDefault(key, null);
    }

    @Override
    public String toString() {
        StringBuilder allSettings = new StringBuilder();
        for (Map.Entry<String, Value> entry : settings.entrySet()) {
            Value value = entry.getValue();
            allSettings
                    .append(value.type).append(settingSeparator)
                    .append(entry.getKey()).append(settingSeparator)
                    .append(value.getStringVal()).append(System.lineSeparator());
        }
        return allSettings.toString();
    }
}
