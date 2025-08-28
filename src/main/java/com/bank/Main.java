package com.bank;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.server.util.PairOfRepos;
import com.bank.db.inmemory.InMemoryAccountRepository;
import com.bank.db.inmemory.InMemoryUserRepository;
import com.bank.server.CustomHttpServer;
import com.bank.server.config.Configuration;
import com.bank.server.config.ConfigurationManager;
import com.bank.server.config.HttpConfigurationException;

public class Main
{
    private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    final static String CONFIG_PATH = "src/main/resources/http.json";

    static
    {
        try
        {
            ConfigurationManager.getInstance().loadConfiguration(CONFIG_PATH);
        } catch (HttpConfigurationException e)
        {
            LOGGER.error("couldn't start Server:: error: {}", String.valueOf(e));
        }
    }

    public static void main(String[] args) throws IllegalArgumentException, IOException, HttpConfigurationException
    {
        LOGGER.info("Server Starting");

        var config = ConfigurationManager.getInstance().getCurrentConfiguration();

        // --- Dependency Injection Setup ---
        var repositories = getRepositories(config);

        // Initialize Services with the chosen repositories

        // Create and start the built-in HTTP server
        var port = config.getPort();
        var threadPoolSize = 10; // You can make this configurable if needed

        var server = new CustomHttpServer(
                port, threadPoolSize, new UserService(repositories.userRepository()), new AccountService(repositories.accountRepository()));
        server.start();

        LOGGER.info("Server started at http://localhost:{}", port);
        LOGGER.info("Config: {}", config);

        // Keep the main thread alive
        try
        {
            Thread.currentThread().join();
        } catch (InterruptedException e)
        {
            LOGGER.info("Server interrupted, shutting down...");
            server.stop(0);
        }

        LOGGER.info("Server Finished");
    }

    /**
     * @param config
     * @return
     */
    private static PairOfRepos getRepositories(Configuration config)
    {
        PairOfRepos repositories;
        var storageType = config.getStorageConfig().getType();
        repositories = switch (storageType.toLowerCase())
        {
            case "in-memory" -> {
                LOGGER.info("Using In-Memory storage.");
                yield new PairOfRepos(
                        InMemoryUserRepository.getInstance(), InMemoryAccountRepository.getInstance());
            }
            case "database" -> {
                // Placeholder for database setup.
                LOGGER.info("Database storage selected. (Implementation is a placeholder)");
                throw new UnsupportedOperationException("Database storage implementation is not yet complete.");
            }
            default -> {
                LOGGER.warn("Unknown storage type '{}'. Defaulting to In-Memory.", storageType);
                yield new PairOfRepos(
                        InMemoryUserRepository.getInstance(), InMemoryAccountRepository.getInstance());
            }
        };

        return repositories;
    }
}
