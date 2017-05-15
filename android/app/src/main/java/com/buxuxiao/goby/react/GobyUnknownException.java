package com.buxuxiao.goby.react;

class GobyUnknownException extends RuntimeException {

    public GobyUnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    public GobyUnknownException(String message) {
        super(message);
    }
}