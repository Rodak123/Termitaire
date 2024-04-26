package com.rodak.termitaire.utils;

import java.io.Serializable;
import java.util.HashMap;

public class ScoreCounter implements Serializable {

    public final HashMap<String, Integer> scoringMap = new HashMap<>();

    private int score;

    public ScoreCounter() {
        scoringMap.put("cardMovedToAFoundation", 10);
        scoringMap.put("cardMovedFromWasteToTableau", 5);
        scoringMap.put("cardTurnedUpInATableau", 5);
        scoringMap.put("maxTimeBonus", 10000);

        scoringMap.put("tenSecondsElapsed", -2);
        scoringMap.put("cardMovedFromAFoundationToTableau", -15);
        scoringMap.put("passedThroughDeckAfterThreePasses", -20);
        scoringMap.put("passedThroughDeckAfterOnePass", -100);
        scoringMap.put("didAnUndo", -15);
    }

    private void changeScore(int scoreDelta) {
        score = Math.max(0, score + scoreDelta);
    }

    public int getScore() {
        return score;
    }

    public int getScorePenalty(int secondsElapsed) {
        return (int) Math.floor((secondsElapsed / 10f)) * getScoreDelta("tenSecondsElapsed");
    }

    public int getScoreBonus(int secondsElapsed) {
        if (Math.floor(secondsElapsed / 60f) > 20) return 0;

        return (int) Math.ceil((1 - secondsElapsed / (20f * 60f)) * getScoreDelta("maxTimeBonus"));
    }

    public int getScoreDelta(String action) {
        int scoreDelta = scoringMap.getOrDefault(action, 0);
        if (scoreDelta == 0) {
            System.out.println("Undefined action: " + action);
        }
        return scoreDelta;
    }

    public void addScoreByScoringMap(String action) {
        changeScore(getScoreDelta(action));
    }

}
