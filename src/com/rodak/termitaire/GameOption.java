package com.rodak.termitaire;

public enum GameOption {
    SingleDraw(1, "Draw 1 card at a time from the stock", true),
    TripleDraw(3, "Draw 3 cards at a time from the stock", false);

    private final int drawCount;
    private final boolean isDefault;
    private final String description;

    GameOption(int drawCount, String description, boolean isDefault) {
        this.drawCount = drawCount;
        this.description = description;
        this.isDefault = isDefault;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String toString() {
        return name() + " - " + description;
    }
}
