package com.rodak.termitaire.game.settings;

import com.rodak.termitaire.ui.ColoredString;
import com.rodak.termitaire.Termitaire;
import com.rodak.termitaire.input.ActionInput;
import com.rodak.termitaire.input.GameBinds;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.rodak.termitaire.Termitaire.getApplicationFolderPath;

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
        DOUBLE,
        BOOLEAN
    }

    public static class Value {

        public final ValueType type;

        private String stringVal;

        private double min, max;

        public Value(int value, int min, int max) {
            this(ValueType.INT, min, max);
            setValue(value);
        }

        public Value(boolean value) {
            this(ValueType.BOOLEAN, 0, 1);
            setValue(value);
        }

        public Value(double value, double min, double max) {
            this(ValueType.DOUBLE, min, max);
            setValue(value);
        }

        public Value(String value, int min, int max) {
            this(ValueType.STRING, min, max);
            setValue(value);
        }

        public Value(String value) {
            this(ValueType.STRING, -1, -1);
            setValue(value);
        }

        private Value(ValueType type, double min, double max) {
            this.type = type;
            this.min = min;
            this.max = max;
        }

        public static Value getValue(String type, String value, Value target) {
            if (type.equalsIgnoreCase(target.type.name())) {
                Value valueObject = new Value(target.type, target.min, target.max);
                valueObject.setValue(value);
                return valueObject;
            }
            return null;
        }

        public boolean isSameType(String value) {
            return switch (type) {
                case STRING -> value.length() > 0;
                case INT -> isPositiveInteger(value);
                case BOOLEAN -> "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
                case DOUBLE -> isDouble(value);
            };
        }

        public boolean isValid(String value) {
            switch (type) {
                case STRING -> {
                    return value.length() >= min && (value.length() <= max || max == -1);
                }
                case INT -> {
                    int intVal = Integer.parseInt(value);
                    return (intVal >= min || min == -1) && (intVal <= max || max == -1);
                }
                case DOUBLE -> {
                    double doubleVal = Double.parseDouble(value);
                    return (doubleVal >= min || min == -1) && (doubleVal <= max || max == -1);
                }
                case BOOLEAN -> {
                    return true;
                }
            }
            return false;
        }

        public String format(String value) {
            return String.valueOf(switch (type) {
                case STRING -> value;
                case INT -> Integer.valueOf(value);
                case DOUBLE -> Double.valueOf(value);
                case BOOLEAN -> Boolean.valueOf(value);
            });
        }

        private static boolean isDouble(String value) {
            if (value.isEmpty()) return false;
            String decimalPattern = "([0-9]*)(\\.([0-9]*))?";
            return Pattern.matches(decimalPattern, value);
        }

        private static boolean isPositiveInteger(String value) {
            if (value.isEmpty()) return false;
            String decimalPattern = "([0-9]+)";
            return Pattern.matches(decimalPattern, value);
        }

        public void setValue(String value) {
            if (value.contains(settingSeparator)) {
                System.out.println("Can't use: " + settingSeparator);
                return;
            }
            stringVal = value;
        }

        public void setValue(double value) {
            setValue(String.valueOf(value));
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

        public double getDoubleVal() {
            return Double.parseDouble(stringVal);
        }

        public ColoredString.Color getColor() {
            return switch (type) {
                case STRING -> ColoredString.Color.WHITE;
                case INT -> ColoredString.Color.PURPLE;
                case DOUBLE -> ColoredString.Color.CYAN;
                case BOOLEAN -> getBoolVal() ? ColoredString.Color.GREEN : ColoredString.Color.RED;
            };
        }
    }

    public static final String SETTINGS_FILE_NAME = Termitaire.NAME.toLowerCase() + "_settings.txt";

    private final HashMap<String, Value> settings;
    private final LinkedHashMap<String, String> groups;

    public GameSettings() {
        settings = new HashMap<>();
        groups = new LinkedHashMap<>();

        putSetting("binds/waste", new Value(String.join(" ", GameBinds.Waste)));
        putSetting("binds/stock", new Value(String.join(" ", GameBinds.Stock)));
        putSetting("binds/tableau", new Value(String.join(" ", GameBinds.Tableau), 14, 14));
        putSetting("binds/foundations", new Value(String.join(" ", GameBinds.Foundations), 8, 8));
        putSetting("binds/unselect", new Value(String.join(" ", GameBinds.Unselect)));

        putSetting("cards/spades", new Value("x"));
        putSetting("cards/hearts", new Value("V"));
        putSetting("cards/clubs", new Value("o"));
        putSetting("cards/diamonds", new Value("^"));

        putSetting("audio/mute", new Value(false));
        putSetting("audio/volume", new Value(1.0, 0, 1));

        putSetting("input/actionRows", new Value(3, 1, -1));
    }

    private void putSetting(String bind, Value value) {
        settings.put(bind, value);

        String group = bind.split("/")[0];
        groups.put(group, "");
    }

    public void loadSettings() {
        Path path = getSettingsPath();
        if (path == null) {
            return;
        }

        HashMap<String, Value> loadedSettings = new HashMap<>();
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                String[] parts = line.split("\\" + settingSeparator);
                if (parts.length == 3) {
                    String type = parts[0];
                    String key = parts[1];
                    String value = parts[2];
                    Value valueObject = Value.getValue(type, value, settings.get(key));
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
        return getApplicationFolderPath(SETTINGS_FILE_NAME);
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

            String settingKey = ActionInput.promptInput("What setting do you want to change (type the blue key or nothing to quit)? ").strip();
            if (settingKey.length() == 0) {
                break;
            }

            if (!settings.containsKey(settingKey)) {
                System.out.println("Setting: '" + settingKey + "' does not exist");
                continue;
            }

            Value settingValue = settings.get(settingKey);
            while (true) {
                String newValue = ActionInput.promptInput("Current value: '" + settingValue.getStringVal() + "'\nWhat new value do you want to set (empty to cancel)? ").strip();

                if (newValue.length() == 0) {
                    break;
                }

                if (!settingValue.isSameType(newValue)) {
                    System.out.println("Can't set '" + settingKey + "' to '" + newValue + "', " + settingValue.type.toString() + " is expected");
                    continue;
                }

                if (!settingValue.isValid(newValue)) {
                    System.out.println("Can't set '" + settingKey + "' to '" + newValue + "', '" + newValue + "' is not valid (min: '" + settingValue.min + "', max: '" + settingValue.max + "')");
                    continue;
                }

                settingValue.setValue(settingValue.format(newValue));
                changedSettings = true;
                break;
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
        ColoredString.Color bindKeyColor = ActionInput.INFO_COLOR;

        groups.put("other", "");
        groups.replaceAll((k, v) -> "");

        for (Map.Entry<String, Value> entry : settings.entrySet()) {
            String[] splitKey = entry.getKey().split("/");
            if (splitKey.length < 2) continue;

            Value value = entry.getValue();

            String group = splitKey[0];
            String prettyEntry = "\n " + ColoredString.colorizeString(entry.getKey(), bindKeyColor) + ": " + ColoredString.colorizeString(value.getStringVal(), value.getColor());

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
