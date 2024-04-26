package com.rodak.termitaire.game.serialization;

import com.rodak.termitaire.Termitaire;
import com.rodak.termitaire.game.Game;
import com.rodak.termitaire.input.ActionInput;
import com.rodak.termitaire.ui.ColoredString;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.rodak.termitaire.Termitaire.getApplicationFolderPath;

public class GameSerialization {

    private static final String SAVES_FOLDER_NAME = Termitaire.NAME.toLowerCase() + "_saves";
    private static final String SAVE_FILE_EXTENSION = ".ser";

    public static void saveGame(Game game) {
        final Path savesFolderPath = getSavesFolderPath();
        if (savesFolderPath == null) {
            return;
        }

        Path saveFilePath = null;
        if (game.savePath != null) {
            Path usedSavePath = Paths.get(game.savePath);
            if (Files.exists(usedSavePath)) {
                String input = ActionInput.promptInput("Game was already saved before at: '" + ColoredString.colorizeString(game.savePath, ActionInput.INFO_COLOR) + "', \nsave it again (Y/n)? ");
                if (input.length() == 0 || input.equalsIgnoreCase("y")) {
                    saveFilePath = usedSavePath;
                }
            }
        }

        if (saveFilePath == null) {
            saveFilePath = askForSaveFilePath(savesFolderPath);
        }
        if (saveFilePath == null) {
            return;
        }

        System.out.println("Saving to '" + ColoredString.colorizeString(saveFilePath.toAbsolutePath().toString(), ActionInput.INFO_COLOR) + "'");

        try (FileOutputStream fileOut = new FileOutputStream(saveFilePath.toAbsolutePath().toString());
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            game.savePath = saveFilePath.toAbsolutePath().toString();
            out.writeObject(game);
            System.out.println("Game saved to '" + saveFilePath.getFileName() + "'");
        } catch (IOException e) {
            System.out.println("Can't save game " + e.getMessage());
        }
    }

    public static Game loadGame() {
        Termitaire.clearScreen();
        final Path savesFolderPath = getSavesFolderPath();
        if (savesFolderPath == null) {
            return null;
        }
        Path saveFilePath = null;

        List<Path> saves = getAllSaves();
        if (saves.size() > 0) {
            do {
                Termitaire.clearScreen();
                printAllSaves(saves);
                String input = ActionInput.promptInput("Select a save to load (index or name): ");
                for (int i = 0; i < saves.size(); i++) {
                    Path savePath = saves.get(i);
                    String saveName = savePath.getFileName().toString().replaceFirst("[.][^.]+$", "");
                    if (input.equals(saveName) ||
                            input.equals(String.valueOf(i))) {
                        saveFilePath = savePath;
                        break;
                    }
                }
                if (saveFilePath == null) {
                    System.out.println("No save found from: '" + input + "'");
                }
            } while (saveFilePath == null);
        }

        if (saveFilePath == null) {
            return null;
        }

        Game game = null;
        try (FileInputStream fileIn = new FileInputStream(saveFilePath.toAbsolutePath().toString());
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            game = (Game) in.readObject();
            System.out.println("Game loaded successfully!");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can't load game (may be corrupt): " + e.getMessage());
        }
        return game;
    }

    private static void printAllSaves(List<Path> saves) {
        for (int i = 0; i < saves.size(); i++) {
            Path savePath = saves.get(i);
            String saveName = savePath.getFileName().toString().replaceFirst("[.][^.]+$", "");
            System.out.println(
                    "[" + ColoredString.colorizeString(String.valueOf(i), ActionInput.COMMAND_COLOR) + "] " +
                            ColoredString.colorizeString(saveName, ActionInput.INFO_COLOR) +
                            " (" + savePath.toAbsolutePath() + ")");
        }
    }

    private static List<Path> getAllSaves() {
        List<Path> saves = new ArrayList<>();

        Path path = getSavesFolderPath();
        if (path == null) return saves;
        try (Stream<Path> paths = Files.walk(path)) {
            paths.forEach(saveFile -> {
                if (Files.isRegularFile(saveFile) && saveFile.toString().endsWith(SAVE_FILE_EXTENSION)) {
                    saves.add(saveFile);
                }
            });
//            paths.anyMatch(p -> Files.isRegularFile(p) && p.toString().endsWith(SAVE_FILE_EXTENSION));
        } catch (IOException e) {
            System.out.println("Can't list all saves: " + e.getMessage());
        }
        return saves;
    }

    public static boolean canLoadGame() {
        Path path = getSavesFolderPath();
        if (path == null) return false;
        try (Stream<Path> paths = Files.walk(path)) {
            return paths.anyMatch(p -> Files.isRegularFile(p) && p.toString().endsWith(SAVE_FILE_EXTENSION));
        } catch (IOException e) {
            System.out.println("Can't read the saves folder: " + e.getMessage());
            return false;
        }
    }

    private static Path askForSaveFilePath(Path savesFolderPath) {
        Path saveFilePath = null;
        do {
            String input = ActionInput.promptInput("Pick a name for the save (NAME" + SAVE_FILE_EXTENSION + ")('" + ColoredString.colorizeString("quit", ActionInput.COMMAND_COLOR) + "' to cancel): ");
            if (input.equalsIgnoreCase("quit")) {
                return null;
            }
            if (input.length() < 1 || input.length() > 2048) {
                System.out.println("Name must have 1 to 2048 characters");
                continue;
            }
            saveFilePath = savesFolderPath.resolve(input + SAVE_FILE_EXTENSION);
            if (Files.exists(saveFilePath)) {
                System.out.println("File '" + saveFilePath.getFileName() + "' already exists");
                saveFilePath = null;
            }
        } while (saveFilePath == null);
        return saveFilePath;
    }

    private static Path getSavesFolderPath() {
        Path savesFolderPath = getApplicationFolderPath(SAVES_FOLDER_NAME);
        if (savesFolderPath == null) return null;
        if (Files.notExists(savesFolderPath)) {
            try {
                Files.createDirectory(savesFolderPath);
            } catch (IOException e) {
                System.out.println("Can't create saves folder" + e.getMessage());
                return null;
            }
        } else if (!Files.isDirectory(savesFolderPath)) {
            System.out.println("'" + savesFolderPath.toAbsolutePath() + "' is not a directory. Can't save/load.");
            return null;
        }
        return savesFolderPath;
    }

}
