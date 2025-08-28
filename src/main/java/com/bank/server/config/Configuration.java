package com.bank.server.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration
{
    private int port;
    private String webroot;

    @JsonProperty("storage")
    private StorageConfig storageConfig;

    // Getters and Setters
    public int getPort()
    {
        return port;
    }

    public StorageConfig getStorageConfig()
    {
        return storageConfig;
    }

    public static class StorageConfig
    {
        private String type;

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        @Override
        public String toString()
        {
            return "StorageConfig{" + "type='" + type + '\'' + '}';
        }
    }

    @Override
    public String toString()
    {
        return "Configuration{" + "port=" + port + ", webroot='" + webroot + '\'' + ", storageConfig=" + storageConfig + '}';
    }
}
