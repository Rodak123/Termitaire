package com.rodak.termitaire.game;

import com.rodak.termitaire.Termitaire;
import com.rodak.termitaire.utils.ScoreCounter;
import com.rodak.termitaire.utils.Timer;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class GameStatistics implements Serializable {

    private static int attempts;
    private int moves, draws, redeals, cardFlips, keystrokes;

    private final ScoreCounter scoreCounter;
    private final Timer timer;

    public GameStatistics() {
        timer = new Timer();
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

    public void pressedKeys(int amount) {
        keystrokes += amount;
    }


    public String getFormattedGameTime(boolean showMillis) {
        long rawMillis = timer.getTimeElapsed();

        String millis = String.format("%03d", rawMillis % 1000);
        String seconds = String.format("%02d", (rawMillis / 1000) % 60);
        String minutes = String.format("%02d", (rawMillis / 60000) % 60);
        String hours = String.format("%02d", Math.min(99, (rawMillis / 3600000) % 60));

        return hours + ":" + minutes + ":" + seconds + (showMillis ? "." + millis : "");
    }

    private String getFormattedKpM() {
        float kpm = keystrokes / (timer.getTimeElapsed() / (60f * 1000f));

        int rounding = (int) Math.pow(10, 3);
        return String.valueOf(Math.floor(kpm * rounding) / rounding);
    }

    public LinkedHashMap<String, String> getAllAndReset() {
        LinkedHashMap<String, String> stats = new LinkedHashMap<>();

        int score = scoreCounter.getScore();
        int secondsElapsed = (int) (timer.getTimeElapsed() / 1000f);
        int penaltyScore = scoreCounter.getScorePenalty(secondsElapsed);
        int bonusScore = scoreCounter.getScoreBonus(secondsElapsed);
        stats.put("Score", String.valueOf(score));
        stats.put("Penalty Score", String.valueOf(penaltyScore));
        stats.put("Bonus Score", String.valueOf(bonusScore));
        stats.put("Total Score", String.valueOf(score + bonusScore + penaltyScore));

        stats.put("Time", getFormattedGameTime(true));
        stats.put("Attempts", String.valueOf(attempts));

        stats.put("Moves", String.valueOf(moves));
        stats.put("Draws", String.valueOf(draws));
        stats.put("Redeals", String.valueOf(redeals));

        stats.put("Card Flips", String.valueOf(cardFlips));
        stats.put("KpM", getFormattedKpM());
        stats.put("Seed", Termitaire.game.seed);

        attempts = 0;

        return stats;
    }

    public int getRedealCount() {
        return redeals;
    }

    public Timer getTimer() {
        return timer;
    }
}
