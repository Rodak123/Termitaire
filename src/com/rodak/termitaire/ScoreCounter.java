package com.rodak.termitaire;

import java.util.HashMap;

public class ScoreCounter {

    public final HashMap<String, Integer> scoringMap = new HashMap<>();

    private int score;

    public ScoreCounter() {
        scoringMap.put("cardMovedToAFoundation", 10);
        scoringMap.put("cardMovedFromWasteToTableau", 5);
        scoringMap.put("cardTurnedUpInATableau", 5);
        scoringMap.put("cardMovedBetweenTableauStacks", 3);

        scoringMap.put("tenSecondsElapsed", -2);
        scoringMap.put("cardMovedFromAFoundationToTableau", -15);
        scoringMap.put("passedThroughDeckAfterThreePasses", -20);
        scoringMap.put("passedThroughDeckAfterOnePass", -100);
        scoringMap.put("didAnUndo", -10);
    }

    private void changeScore(int scoreDelta) {
        score = Math.max(0, score + scoreDelta);
    }

    public int getScore() {
        return score;
    }

    public int getScore(int secondsElapsed) {
        int tenSecondPenalty = (secondsElapsed / 10) * getScoreDelta("tenSecondsElapsed");
        return score + tenSecondPenalty;
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
