package com.bank.server.config;

import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.bank.server.util.Json;

public class ConfigurationManager
{

    private static ConfigurationManager configurationManager;
    private static Configuration currentConfiguration;

    public Configuration getCurrentConfiguration() throws HttpConfigurationException
    {
        if (currentConfiguration == null)
        {
            throw new HttpConfigurationException("No Config Found");
        }
        return currentConfiguration;
    }

    private ConfigurationManager()
    {
    }

    public static ConfigurationManager getInstance()
    {
        if (configurationManager == null)
            configurationManager = new ConfigurationManager();
        return configurationManager;
    }

    public void loadConfiguration(String filePath) throws HttpConfigurationException
    {

        try (FileReader fileReader = new FileReader(filePath))
        {
            StringBuffer sb = new StringBuffer();
            int i;
            try
            {
                while ((i = fileReader.read()) != -1)
                {
                    sb.append((char) i);
                }
            } catch (IOException e)
            {
                throw new HttpConfigurationException(e);
            }
            JsonNode conf = null;
            try
            {
                conf = Json.parse(sb.toString());
            } catch (IOException e)
            {
                throw new HttpConfigurationException(e);
            }

            currentConfiguration = Json.fromJson(conf, Configuration.class);
        } catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }
}
