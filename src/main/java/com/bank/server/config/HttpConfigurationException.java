package com.bank.server.config;

import java.io.IOException;

public class HttpConfigurationException extends IOException
{
    public HttpConfigurationException(String message)
    {
        super(message);
    }

    public HttpConfigurationException(Exception e)
    {
        super(e);
    }
}
