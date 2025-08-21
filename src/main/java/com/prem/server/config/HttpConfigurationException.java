package com.prem.server.config;

import java.io.FileNotFoundException;

public class HttpConfigurationException extends Exception {
    public HttpConfigurationException(String message) {
        super(message);
    }

    public HttpConfigurationException(Exception e) {
        super(e);
    }
}
