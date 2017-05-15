package com.buxuxiao.goby.react;

public enum GobyUpdateState {
    RUNNING(0),
    PENDING(1),
    LATEST(2);

    private final int value;
    GobyUpdateState(int value) {
        this.value = value;
    }
    public int getValue() {
        return this.value;
    }
}