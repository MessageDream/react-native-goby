package com.buxuxiao.goby.react;

public enum GobyInstallMode {
    IMMEDIATE(0),
    ON_NEXT_RESTART(1),
    ON_NEXT_RESUME(2),
    ON_NEXT_SUSPEND(3);

    private final int value;
    GobyInstallMode(int value) {
        this.value = value;
    }
    public int getValue() {
        return this.value;
    }
}