package com.rodak.termitaire;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;

public class GameStatistics {

    private static int attempts;
    private int moves, draws, redeals, cardFlips, keystrokes, undoes;

    private final ScoreCounter scoreCounter;
    private final Instant startTime;

    public GameStatistics() {
        startTime = Instant.now();
        attempts++;

        scoreCounter = new ScoreCounter();
    }

    public ScoreCounter getScoreCounter() {
        return scoreCounter;
    }

    public void didAMove() {
        moves++;
    }

    public void didADraw() {
        draws++;
    }

    public void didARedeal() {
        redeals++;
    }

    public void didACardFlip() {
        cardFlips++;
    }

    public void didAnUndo() {
        undoes++;
    }

    public void pressedKeys(int amount) {
        keystrokes += amount;
    }

    private Duration getGameDuration() {
        return Duration.between(startTime, Instant.now());
    }

    public String getFormattedGameTime() {
        return getFormattedGameTime(true);
    }

    public String getFormattedGameTime(boolean showMillis) {
        Duration gameDuration = getGameDuration();
        long rawMillis = gameDuration.toMillis();

        String millis = String.format("%03d", rawMillis % 1000);
        String seconds = String.format("%02d", (rawMillis / 1000) % 60);
        String minutes = String.format("%02d", (rawMillis / 60000) % 60);
        String hours = String.format("%02d", Math.min(99, (rawMillis / 3600000) % 60));

        return hours + ":" + minutes + (showMillis ? " " : ":") + seconds + (showMillis ? "." + millis : "");
    }

    private String getFormattedKPM() {
        Duration gameDuration = getGameDuration();

        float kpm = keystrokes / (gameDuration.getSeconds() / 60f);

        int rounding = (int) Math.pow(10, 3);
        return String.valueOf(Math.floor(kpm * rounding) / rounding);
    }

    public LinkedHashMap<String, String> getAllAndReset() {
        Duration gameDuration = getGameDuration();
        LinkedHashMap<String, String> stats = new LinkedHashMap<>();

        stats.put("Score", String.valueOf(scoreCounter.getScore((int) gameDuration.getSeconds())));
        stats.put("Time", getFormattedGameTime());
        stats.put("Attempts", String.valueOf(attempts));
        stats.put("Undoes", String.valueOf(undoes));

        stats.put("Moves", String.valueOf(moves));
        stats.put("Draws", String.valueOf(draws));
        stats.put("Redeals", String.valueOf(redeals));

        stats.put("Card Flips", String.valueOf(cardFlips));
        stats.put("KPM", getFormattedKPM());

        attempts = 0;

        return stats;
    }

    public int getRedealCount() {
        return redeals;
    }
}
