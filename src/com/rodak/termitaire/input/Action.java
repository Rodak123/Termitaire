package com.rodak.termitaire.input;

public interface Action {

    void execute(String key, int index);

    String[] getCommands();

    String getInfo();

    default boolean clearAfter() {
        return false;
    }

}
