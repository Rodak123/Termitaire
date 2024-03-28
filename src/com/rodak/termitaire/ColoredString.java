package com.rodak.termitaire;

public class ColoredString {

    public enum Color {
        RESET("\u001B[0m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),

        CYAN("\u001B[36m"),
        WHITE("\u001B[37m"),
        ;

        private final String ansi;

        Color(String ansi) {
            this.ansi = ansi;
        }

        @Override
        public String toString() {
            return ansi;
        }
    }

    public static final String colorRegex = "\u001B\\[\\d+m";

    public static String colorizeString(String text, Color color) {
        return color.ansi + text + Color.RESET.ansi;
    }
}
