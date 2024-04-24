package com.rodak.termitaire;

public class ScoreCounter {

    private int score;

    private void changeScore(int scoreDelta) {
        score = Math.max(0, score + scoreDelta);
    }

    public int getScore() {
        return score;
    }

    public int getScore(int secondsElapsed) {
        int tenSecondPenalty = (secondsElapsed / 10) * -2;
        return score + tenSecondPenalty;
    }

    public void cardMovedToAFoundation() {
        changeScore(10);
    }

    public void cardMovedFromWasteToTableau() {
        changeScore(5);
    }

    public void cardTurnedUpInATableau() {
        changeScore(5);
    }

    public void cardMovedBetweenTableauStacks() {
        changeScore(3);
    }

    public void cardMovedFromAFoundationToTableau() {
        changeScore(-15);
    }

    public void passedThroughDeckAfterThreePasses() {
        changeScore(-20);
    }

    public void passedThroughDeckAfterOnePass() {
        changeScore(-100);
    }
}
