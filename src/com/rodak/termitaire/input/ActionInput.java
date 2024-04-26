package com.rodak.termitaire.input;

import com.rodak.termitaire.ui.ColoredString;
import com.rodak.termitaire.game.settings.GameSettings;
import com.rodak.termitaire.Termitaire;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ActionInput {

    private static Scanner keyboard;

    public static String promptInput(String prompt) {
        System.out.print(prompt);
        return keyboard.nextLine();
    }

    public static ColoredString.Color COMMAND_COLOR = ColoredString.Color.GREEN;
    public static ColoredString.Color INFO_COLOR = ColoredString.Color.BLUE;


    private final List<Action> actionList;

    public ActionInput() {
        if (keyboard == null) keyboard = new Scanner(System.in);
        actionList = new ArrayList<>();
    }

    public void addAllActions(List<Action> newActionList) {
        actionList.addAll(newActionList);
    }

    public void addAction(Action action) {
        actionList.add(action);
    }

    public void executeAction() {
        int maxRows = GameSettings.getInstance().getSetting("input/actionRows").getIntVal();
        int marginLeft = 4;

        System.out.println("\nAvailable actions:");

        ArrayList<String> commandStrings = new ArrayList<>();
        for (Action action : actionList) {
            StringBuilder commandsString = new StringBuilder();
            String[] commands = action.getCommands();
            for (int i = 0; i < commands.length; i++) {
                commandsString.append(ColoredString.colorizeString(commands[i], COMMAND_COLOR));
                if (i < commands.length - 1) {
                    commandsString.append(", ");
                }
            }
            commandStrings.add(" ".repeat(marginLeft) + "[" + commandsString + "] -> " + ColoredString.colorizeString(action.getInfo(), INFO_COLOR));
        }

        int rows = Math.min(commandStrings.size(), maxRows);
        int maxLengthInCol = 0, lastMaxLengthInCol = 0;
        StringBuilder[] commandLines = new StringBuilder[rows];
        for (int i = 0; i < commandLines.length; i++) {
            commandLines[i] = new StringBuilder();
        }
        for (int i = 0; i < commandStrings.size(); i++) {
            String commandString = commandStrings.get(i);
            if (i % commandLines.length == 0) {
                lastMaxLengthInCol = maxLengthInCol;
                maxLengthInCol = 0;
            }
            maxLengthInCol = Math.max(maxLengthInCol, commandString.replaceAll(ColoredString.colorRegex, "").length());

            int gap = 0;
            if (i >= rows) {
                gap += lastMaxLengthInCol - commandStrings.get(i - rows).replaceAll(ColoredString.colorRegex, "").length();
            }
            commandLines[i % commandLines.length].append(" ".repeat(gap)).append(commandString);
        }

        for (StringBuilder line : commandLines) {
            System.out.println(line);
        }

        String input = promptInput("?: ").toLowerCase();
        Termitaire.game.getStatistics().pressedKeys(input.length() + 1); // Keys pressed to type (does not account for pasting) + RET
        if (input.length() == 0) {
            input = "_";
        }

        int match = -1;
        for (Action action : actionList) {
            for (int i = 0; i < action.getCommands().length; i++) {
                String command = action.getCommands()[i];
                if (input.equals(command)) {
                    match = i;
                    break;
                }
            }
            if (match != -1) {
                if (!action.clearAfter()) {
                    Termitaire.clearScreen();
                }
                action.execute(input, match);
                if (action.clearAfter()) {
                    Termitaire.clearScreen();
                }
                break;
            }
        }
        if (match == -1) {
            Termitaire.clearScreen();
            System.out.println("No action matches '" + input + "'");
        }
        actionList.clear();

    }

    public void dispose() {
        keyboard.close();
    }

}
