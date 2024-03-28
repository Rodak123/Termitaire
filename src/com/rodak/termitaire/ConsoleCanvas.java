package com.rodak.termitaire;

import java.util.Arrays;

public class ConsoleCanvas {

    public final int width, height;
    private final String[] lines;
    private final ColoredString.Color[][] colors;

    public ConsoleCanvas(int width, int height) {
        this.width = width;
        this.height = height;

        lines = new String[height];
        colors = new ColoredString.Color[height][width];
        clear();
    }

    public void clear() {
        Arrays.fill(lines, " ".repeat(width));
        for (ColoredString.Color[] row : colors) {
            Arrays.fill(row, null);
        }
    }

    public void setColor(int x, int y, ColoredString.Color color) {
        colors[(y % this.height)][(x % this.width)] = color;
    }

    public void setColors(int x, int y, ColoredString.Color color, int len) {
        ColoredString.Color[] colors = new ColoredString.Color[len];
        Arrays.fill(colors, color);
        setColors(x, y, colors);
    }

    public void setColors(int x, int y, ColoredString.Color[] colors) {
        setColors(x, y, new ColoredString.Color[][]{colors});
    }

    public void setColors(int x, int y, ColoredString.Color[][] colors) {
        for (int j = 0; j < colors.length; j++) {
            for (int i = 0; i < colors[j].length; i++) {
                setColor(x + i, y + j, colors[j][i]);
            }
        }
    }

    public void plot(int x, int y, String line) {
        plot(x, y, new String[]{line});
    }

    public void plot(int x, int y, String[] lines) {
        for (int j = 0; j < lines.length; j++) {
            String line = lines[j];
            int row = (y + j) % this.height;
            this.lines[row] = this.lines[row].substring(0, x) + line + this.lines[row].substring(x + line.length());
        }
    }

    public void print() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < colors.length; i++) {
            String line = lines[i];
            ColoredString.Color[] colorsRow = colors[i];
            for (int j = 0; j < colorsRow.length; j++) {
                ColoredString.Color color = colorsRow[j];
                char ch = line.charAt(j);
                if (color == null) {
                    out.append(ch);
                } else {
                    out.append(color).append(ch).append(ColoredString.Color.RESET);
                }
            }
            out.append("\n");
        }
        System.out.println();
        System.out.println(out);
    }

    public void printColors() {
        StringBuilder out = new StringBuilder();
        for (ColoredString.Color[] row : colors) {
            for (ColoredString.Color color : row) {
                char ch = '█';
                if (color == null) {
                    out.append(ch);
                } else {
                    out.append(color).append(ch).append(ColoredString.Color.RESET);
                }
            }
            out.append("\n");
        }
        System.out.println(out);
    }

}
