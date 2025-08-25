package com.bank;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.clientInterface.util.Repositories;
import com.bank.infrastructure.persistence.inmemory.InMemoryAccountRepository;
import com.bank.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.bank.server.CustomHttpServer;
import com.bank.server.config.Configuration;
import com.bank.server.config.ConfigurationManager;
import com.bank.server.config.HttpConfigurationException;

public class HttpServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);
    final static String CONFIG_PATH = "src/main/resources/http.json";

    static {
        try {
            ConfigurationManager.getInstance().loadConfiguration(CONFIG_PATH);
        } catch (HttpConfigurationException e) {
            LOGGER.error("couldn't start Server:: error: {}", e);
        }
    }

    public static void main(String[] args) throws IllegalArgumentException, IOException, HttpConfigurationException {
        LOGGER.info("Server Starting");

        Configuration config = ConfigurationManager.getInstance().getCurrentConfiguration();

        // --- Dependency Injection Setup ---
        Repositories repositories = getRepositories(config);

        // Initialize Services with the chosen repositories
        UserService userService = new UserService(repositories.userRepository());
        AccountService accountService = new AccountService(repositories.accountRepository());

        // Create and start the built-in HTTP server
        int port = config.getPort();
        int threadPoolSize = 10; // You can make this configurable if needed

        CustomHttpServer server = new CustomHttpServer(port, threadPoolSize, userService, accountService);
        server.start();

        LOGGER.info("Server started at http://localhost:{}", port);
        LOGGER.info("Config: {}", config);

        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.info("Server interrupted, shutting down...");
            server.stop(0);
        }

        LOGGER.info("Server Finished");
    }

    private static Repositories getRepositories(Configuration config) {
        Repositories repositories;
        String storageType = config.getStorageConfig().getType();
        switch (storageType.toLowerCase()) {
            case "in-memory":
                LOGGER.info("Using In-Memory storage.");
                repositories = new Repositories(
                        InMemoryUserRepository.getInstance(),
                        InMemoryAccountRepository.getInstance());
                break;
            case "database":
                // Placeholder for database setup.
                LOGGER.info("Database storage selected. (Implementation is a placeholder)");
                throw new UnsupportedOperationException("Database storage implementation is not yet complete.");
            default:
                LOGGER.warn("Unknown storage type '{}'. Defaulting to In-Memory.", storageType);
                repositories = new Repositories(
                        InMemoryUserRepository.getInstance(),
                        InMemoryAccountRepository.getInstance());
        }

        return repositories;
    }
}
