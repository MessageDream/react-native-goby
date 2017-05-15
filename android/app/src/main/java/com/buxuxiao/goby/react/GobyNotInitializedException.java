package com.buxuxiao.goby.react;

public final class GobyNotInitializedException extends RuntimeException {

    public GobyNotInitializedException(String message, Throwable cause) {
        super(message, cause);
    }

    public GobyNotInitializedException(String message) {
        super(message);
    }
}