package com.rodak.termitaire;

public class Timer {
    private long startTime;
    private long elapsedTime;

    public Timer() {
        startTime = System.currentTimeMillis();
    }

    public void pause() {
        elapsedTime += System.currentTimeMillis() - startTime;
    }

    public void resume() {
        startTime = System.currentTimeMillis();
    }

    public long getTimeElapsed() {
        return elapsedTime + System.currentTimeMillis() - startTime;
    }
}

