package com.buxuxiao.goby.react;

import java.net.MalformedURLException;

public class GobyMalformedDataException extends RuntimeException {
    public GobyMalformedDataException(String path, Throwable cause) {
        super("Unable to parse contents of " + path + ", the file may be corrupted.", cause);
    }
    public GobyMalformedDataException(String url, MalformedURLException cause) {
        super("The package has an invalid downloadUrl: " + url, cause);
    }
}