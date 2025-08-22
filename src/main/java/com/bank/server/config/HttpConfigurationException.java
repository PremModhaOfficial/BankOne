package com.bank.server.config;

public class HttpConfigurationException extends Exception {
    public HttpConfigurationException(String message) {
        super(message);
    }

    public HttpConfigurationException(Exception e) {
        super(e);
    }
}
