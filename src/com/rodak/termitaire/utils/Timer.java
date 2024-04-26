package com.rodak.termitaire.utils;

import java.io.Serializable;

public class Timer implements Serializable {
    private transient long startTime;
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

