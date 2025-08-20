package com.prem;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prem.server.config.Configuration;
import com.prem.server.config.ConfigurationManager;
import com.prem.server.core.ServerListnerThread;

public class HttpServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    final static String CONFIG_PATH = "src/main/resources/http.json";

    public static void main(String[] args) throws IllegalArgumentException, IOException {

        LOGGER.info("Server Starting");

        ConfigurationManager.getInstance().loadConfiguration(CONFIG_PATH);
        Configuration config = ConfigurationManager.getInstance().getCurrentConfiguration();

        LOGGER.info("http://localhost:" + config.getPort());
        LOGGER.info("webroot -> " + config.getWebroot());
        ServerListnerThread slt = new ServerListnerThread(config);

        slt.start();

        try {
            slt.join();
        } catch (InterruptedException e) {
            LOGGER.error("The Lisner Thread Is Interrupted while working");
        }

        LOGGER.info("Server Finished");
    }
}
