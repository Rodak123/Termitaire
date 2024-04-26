package com.rodak.termitaire;

import com.rodak.termitaire.game.Game;
import com.rodak.termitaire.game.SoundManager;
import com.rodak.termitaire.game.serialization.GameSerialization;
import com.rodak.termitaire.game.settings.GameSettings;
import com.rodak.termitaire.game.components.Card;
import com.rodak.termitaire.input.Action;
import com.rodak.termitaire.input.ActionInput;
import com.rodak.termitaire.input.GameBinds;
import com.rodak.termitaire.ui.ColoredString;
import com.rodak.termitaire.ui.ConsoleCanvas;
import com.rodak.termitaire.ui.GamePlotter;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Termitaire {

    private static boolean running;

    private static final int MIN_JAVA_MAJOR = 17;

    public static final String NAME = "Termitaire";
    public static final String DESCRIPTION = "Text-based solitaire.";
    public static final String AUTHOR = "Radek Titěra";
    public static final String AUTHOR_URL = "https://rodakdev.itch.io/";
    public static final String VERSION = "1.0";

    private static final HashMap<String, String> paths = new HashMap<>();

    public static Game game;
    public static SoundManager soundManager;

    public static void main(String[] args) {
        checkJavaVersion();

        paths.put("help", "/data/help.txt");
        paths.put("soundsFolder", "/sfx");

        System.out.println("Loading...");

        soundManager = new SoundManager(paths.get("soundsFolder"), ".wav");

        clearScreen();
        printTitle();
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

        soundManager.play(SoundManager.Sound.Startup);

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
        soundManager.dispose();
    }

    private static void addPausedActions(ActionInput actionInput) {
        if (game.canBeSaved()) {
            actionInput.addAction(new Action() {
                @Override
                public void execute(String key, int index) {
                    GameSerialization.saveGame(game);
                }

                @Override
                public String[] getCommands() {
                    return new String[]{"save"};
                }

                @Override
                public String getInfo() {
                    return "Saves currently paused game to the saves directory";
                }
            });
        }
        if (GameSerialization.canLoadGame()) {
            actionInput.addAction(new Action() {
                @Override
                public void execute(String key, int index) {
                    if (game.canBeSaved()) {
                        String input = ActionInput.promptInput("Are you sure you want to load a game, doing so will overwrite the currently paused game(Y/n)? ");
                        if (!(input.length() == 0 || input.equalsIgnoreCase("y"))) {
                            return;
                        }
                    }
                    Game loadedGame = GameSerialization.loadGame();
                    if (loadedGame == null) {
                        System.out.println("Game save not loaded");
                        return;
                    }
                    game = loadedGame;
                    System.out.println("Game save loaded");
                }

                @Override
                public String[] getCommands() {
                    return new String[]{"load"};
                }

                @Override
                public String getInfo() {
                    return "Load a saved game from the saves directory";
                }
            });
        }

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

                try (InputStream in = getClass().getResourceAsStream("/data/help.txt");
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    String helpText = reader.lines().collect(Collectors.joining(System.lineSeparator()));
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
                    e.printStackTrace();
                    System.out.println("Did not find '" + ColoredString.colorizeString("help.txt", ColoredString.Color.RED) + "'");
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


    public static Path getApplicationFolderPath(String resolvePath) {
        try {
            Path path = Path.of(Termitaire.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return path.getParent().resolve(resolvePath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println("Can't access the application folder");
            return null;
        }
    }

}