package com.rodak.termitaire;

import java.nio.file.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Termitaire {

    private static boolean running;

    private static final int MIN_JAVA_MAJOR = 17;

    public static final String NAME = "Termitaire";
    public static final String DESCRIPTION = "Text-based solitaire.";
    public static final String AUTHOR = "Radek Titěra";
    public static final String AUTHOR_URL = "https://www.therodak.online/";
    public static final String VERSION = "1.0";

    private static final HashMap<String, Path> paths = new HashMap<>();

    public static Game game;

    public static void main(String[] args) {
        checkJavaVersion();

        paths.put("help", Paths.get("resources", "/data/help.txt"));

        clearScreen();
        printTitle();
        System.out.println();
        System.out.println(String.join("\n", new String[]{
                "Todo:",
                "- Sound",
                "- Saving game + Loading saves",
        }));
        System.out.println();

        runGame();
    }

    private static void printTitle() {
        int dashes = titleDashCount();
        System.out.println(String.join("\n", new String[]{
                "-".repeat(dashes),
                centerText(NAME, dashes),
                centerText("by " + AUTHOR, dashes),
                "-".repeat(dashes)
        }));
    }

    private static void printInfo() {
        System.out.println(String.join("\n", new String[]{
                "Version: " + VERSION,
                "Description: " + DESCRIPTION,
                "Author URL: " + AUTHOR_URL
        }));
        System.out.println();
    }

    public static void onSettingsUpdated(boolean write) {
        if (write) GameSettings.getInstance().storeSettings();
        GameBinds.loadBindsFromSettings();
    }

    private static void runGame() {
        running = true;

        ActionInput actionInput = new ActionInput();

        GameSettings.getInstance().loadSettings();
        onSettingsUpdated(false);

        game = new Game();
        ConsoleCanvas canvas = GamePlotter.createFittingCanvas(game);

        while (running) {
            if (game.isPlaying()) {
                canvas.clear();
                GamePlotter.plotGameToCanvas(game, canvas);
                canvas.print();
            }

            actionInput.addAllActions(game.getActions());
            actionInput.addAction(new Action() {
                @Override
                public void execute(String key, int index) {
                    running = false;
                }

                @Override
                public String[] getCommands() {
                    return new String[]{"quit"};
                }

                @Override
                public String getInfo() {
                    return "Quit the game";
                }
            });

            if (!game.isPlaying()) {
                addPausedActions(actionInput);
            }
            actionInput.executeAction();
        }

        System.out.println("Goodbye");
        actionInput.dispose();
    }

    private static void addPausedActions(ActionInput actionInput) {
        actionInput.addAction(new Action() {
            @Override
            public void execute(String key, int index) {
                printTitle();
                printInfo();
            }

            @Override
            public String[] getCommands() {
                return new String[]{"info"};
            }

            @Override
            public String getInfo() {
                return "Get info about this game";
            }
        });

        actionInput.addAction(new Action() {
            @Override
            public void execute(String key, int index) {
                GameSettings.getInstance().changeSettings();
            }

            @Override
            public String[] getCommands() {
                return new String[]{"settings"};
            }

            @Override
            public String getInfo() {
                return "Change binds and looks";
            }
        });

        actionInput.addAction(new Action() {
            @Override
            public void execute(String key, int index) {
                ColoredString.Color titleColor = ColoredString.Color.YELLOW;
                ColoredString.Color placeColor = ColoredString.Color.CYAN;

                String[] ranks = new String[Card.Rank.values().length];
                for (int i = 0; i < ranks.length; i++) {
                    ranks[i] = Card.Rank.values()[i].getShortName();
                }

                Path helpPath = paths.get("help");
                try (Stream<String> lines = Files.lines(helpPath)) {
                    String helpText = lines.collect(Collectors.joining(System.lineSeparator()));

                    HashMap<String, String> vars = new HashMap<>();

                    for (ColoredString.Color color : ColoredString.Color.values()) {
                        vars.put(color.name().toUpperCase(), color.toString());
                    }
                    vars.put("CTITLE", titleColor.toString());
                    vars.put("CPLACE", placeColor.toString());
                    vars.put("SUITS",
                            ColoredString.colorizeString(Card.Suit.SPADES.getShortName(), Card.BLACK) + " (for spades), " +
                                    ColoredString.colorizeString(Card.Suit.HEARTS.getShortName(), Card.RED) + " (for hearts), " +
                                    ColoredString.colorizeString(Card.Suit.CLUBS.getShortName(), Card.BLACK) + " (for clubs), " +
                                    ColoredString.colorizeString(Card.Suit.DIAMONDS.getShortName(), Card.RED) + " (for diamonds)"
                    );
                    vars.put("RANKS", String.join(", ", ranks));
                    vars.put("CARD-EXAMPLE", Card.Suit.HEARTS.getShortName() + " " + Card.Rank.FIVE.getShortName());

                    for (Map.Entry<String, Integer> entry : game.getStatistics().getScoreCounter().scoringMap.entrySet()) {
                        int value = entry.getValue();
                        ColoredString.Color color = value < 0 ? ColoredString.Color.RED : ColoredString.Color.GREEN;
                        String formattedValue = (value >= 0 ? " " : "") + color + value + ColoredString.Color.RESET;
                        vars.put(entry.getKey(), formattedValue);
                    }

                    String specialSeparator = "_";
                    for (Map.Entry<String, String> entry : vars.entrySet()) {
                        helpText = helpText.replaceAll(specialSeparator + entry.getKey().toUpperCase() + specialSeparator, entry.getValue());
                    }

                    System.out.println(helpText);
                } catch (IOException e) {
                    System.out.println("Did not find '" + ColoredString.colorizeString(helpPath.getFileName().toString(), ColoredString.Color.RED) + "'");
                }
            }

            @Override
            public String[] getCommands() {
                return new String[]{"help"};
            }

            @Override
            public String getInfo() {
                return "How to play the game";
            }
        });
    }

    private static void checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");

        int majorVersion = Integer.parseInt(javaVersion.split("\\.")[0]);

        if (majorVersion < MIN_JAVA_MAJOR) {
            System.err.println("Error: This application requires at least Java 17 to run.");
            System.exit(1);
        }
    }

    public static String centerText(String text, int width) {
        return centerText(text, width, " ");
    }

    public static String centerText(String text, int width, String filler) {
        int spaceLeft = width - text.length();
        if (spaceLeft <= 0) {
            return text;
        }
        int right = Math.floorDiv(spaceLeft, 2);
        int left = spaceLeft - right;
        return filler.repeat(left) + text + filler.repeat(right);
    }

    public static String rightAlignText(String text, int width) {
        int spacesLeft = width - text.length();
        if (spacesLeft <= 0) {
            return text;
        }
        return " ".repeat(spacesLeft) + text;
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static int titleDashCount() {
        return 32;
    }

}
