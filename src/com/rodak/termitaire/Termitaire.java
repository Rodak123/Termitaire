package com.rodak.termitaire;

public class Termitaire {

    private static boolean running;

    private static final int MIN_JAVA_MAJOR = 17;

    private static final String NAME = "Termitaire";
    private static final String DESCRIPTION = "Text-based solitaire.";
    private static final String AUTHOR = "Radek Titěra";
    private static final String AUTHOR_URL = "https://www.therodak.online/";
    private static final String VERSION = "0.1";

    public static void main(String[] args) {
        checkJavaVersion();

        GameBinds.loadBindsFromFile();

        clearScreen();
        printTitle();
        System.out.println();
        System.out.println(String.join("\n", new String[]{
                "This is not a finished project.",
                "Todo:",
                "- Detecting when player wins",
                "- Tracking time",
                "- Tracking score",
                "- Change binds from app"
        }));
        System.out.println();

        runGame();
    }

    private static void printTitle() {
        int dashes = 24;
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

    private static void runGame() {
        running = true;

        ActionInput actionInput = new ActionInput();

        Game game = new Game();
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
                ColoredString.Color titleColor = ColoredString.Color.YELLOW;
                ColoredString.Color placeColor = ColoredString.Color.CYAN;

                String[] ranks = new String[Card.Rank.values().length];
                for (int i = 0; i < ranks.length; i++) {
                    ranks[i] = Card.Rank.values()[i].getShortName();
                }

                System.out.println(String.join("\n", new String[]{
                        ColoredString.colorizeString("Cards", titleColor),
                        " Each card is represented by its RANK and SUIT.",
                        "  RANK can be " + ColoredString.colorizeString(String.join(", ", ranks), ColoredString.Color.GREEN),
                        "  SUIT can be " +
                                ColoredString.colorizeString(Card.Suit.SPADES.getShortName(), Card.BLACK) + " (for spades), " +
                                ColoredString.colorizeString(Card.Suit.HEARTS.getShortName(), Card.RED) + " (for hearts), " +
                                ColoredString.colorizeString(Card.Suit.CLUBS.getShortName(), Card.BLACK) + " (for clubs), " +
                                ColoredString.colorizeString(Card.Suit.DIAMONDS.getShortName(), Card.RED) + " (for diamonds)",
                        "",
                        ColoredString.colorizeString("Places", titleColor),
                        " " + ColoredString.colorizeString("Foundations", placeColor) + " - Four stacks in the top left corner",
                        " " + ColoredString.colorizeString("Hand", placeColor) + " - Column of cards in the top right corner",
                        " " + ColoredString.colorizeString("Stock", placeColor) + " - Pile of cards next to the hand",
                        " " + ColoredString.colorizeString("Waste", placeColor) + " - Pile of cards next to the stock",
                        " " + ColoredString.colorizeString("Tableau", placeColor) + " - Seven columns in the second row of cards",
                        "",
                        ColoredString.colorizeString("Goal", titleColor),
                        " In solitaire, your goal is to put all cards into their foundations.",
                        "",
                        ColoredString.colorizeString("Controls", titleColor),
                        " To execute an action, type it's command and press " + ColoredString.colorizeString("ENTER/RETURN", ColoredString.Color.PURPLE),
                        " Selecting card/s from a tableau column (Which card to select? ):",
                        "  1) Leave it empty, if you want to select the very top card",
                        "  2) Type the index (Numbers on both sides: [" + ColoredString.colorizeString("NUMBER", ColoredString.Color.GREEN) + "])",
                        "  3) Type the card's SUIT and RANK. For example to select FIVE of HEARTS: '" +
                                ColoredString.colorizeString(Card.Suit.HEARTS.getShortName() + " " + Card.Rank.FIVE.getShortName(), Card.RED) +
                                "' (Not case sensitive)"
                }));
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
        int spaceLeft = width - text.length();
        if (spaceLeft <= 0) {
            return text;
        }
        int right = Math.floorDiv(spaceLeft, 2);
        int left = spaceLeft - right;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

}
