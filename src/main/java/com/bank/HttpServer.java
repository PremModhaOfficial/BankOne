package com.bank;

import com.bank.server.CustomHttpServer;
import com.bank.server.config.Configuration;
import com.bank.server.config.ConfigurationManager;
import com.bank.server.config.HttpConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    final static String CONFIG_PATH = "src/main/resources/http.json";

    public static void main(String[] args) throws IllegalArgumentException, IOException, HttpConfigurationException {
        LOGGER.info("Server Starting");

        ConfigurationManager.getInstance().loadConfiguration(CONFIG_PATH);
        Configuration config = ConfigurationManager.getInstance().getCurrentConfiguration();

        int port = config.getPort();
        // Configure thread pool size, e.g., from config or a default value
        int threadPoolSize = 10; // Default value, you might want to make this configurable

        // Create and start the custom server
        CustomHttpServer server = new CustomHttpServer(port, threadPoolSize);

        LOGGER.info("http://localhost:{}", port);
        LOGGER.info("config : {}", config);

        // Add a shutdown hook to gracefully stop the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down server...");
            server.stop(0);
        }));

        server.start();

        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.info("Server interrupted, shutting down...");
            server.stop(0);
        }

        LOGGER.info("Server Finished");
    }
}
