package com.bank.server.config;

import java.util.List;

public class Configuration
{
    private List<Integer> ports;
    private String storageType;

    // Getters and Setters
    public List<Integer> getPorts()
    {
        return ports;
    }

    public void setPorts(List<Integer> ports)
    {
        this.ports = ports;
    }


    public String getStorageType()
    {
        return storageType;
    }

    public void setStorageType(String storageType)
    {
        this.storageType = storageType;
    }

    // Backward compatibility method
    public int getPort()
    {
        return ports != null && !ports.isEmpty() ? ports.get(0) : 8080;
    }

    // Backward compatibility method for StorageConfig
    public StorageConfig getStorageConfig()
    {
        StorageConfig config = new StorageConfig();
        config.setType(storageType);
        return config;
    }

    // Keep StorageConfig for backward compatibility
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
        return "Configuration{" + "ports=" + ports + ", storageType='" + storageType + '\'' + '}';
    }
}
