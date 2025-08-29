package com.bank;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.server.config.RepositoryContainer;
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
        } catch (HttpConfigurationException configException)
        {
            LOGGER.error("couldn't start Server:: error: {}", String.valueOf(configException));
        }
    }

    public static void main(String[] args) throws IllegalArgumentException, IOException, HttpConfigurationException
    {
        LOGGER.info("Server Starting");

        var config = ConfigurationManager.getInstance().getCurrentConfiguration();

        // --- Dependency Injection Setup ---
        var repositories = getRepositories(config);

        // Initialize Services with the chosen repositories

        // Get all ports from configuration
        var ports = config.getPorts();
        if (ports == null || ports.isEmpty())
        {
            ports = List.of(8080); // Default fallback
        }

        // Create and start multiple HTTP servers
        var threadPoolSize = 8; // You can make this configurable if needed
        List<CustomHttpServer> servers = new ArrayList<>();

        for (int port : ports)
        {
            var server = new CustomHttpServer(
                    port, threadPoolSize, new UserService(repositories.userRepository()), new AccountService(repositories.accountRepository()));
            server.start();
            servers.add(server);
            LOGGER.info("Server started at http://localhost:{}", port);
        }

        LOGGER.info("All servers started. Ports: {}", ports);
        LOGGER.info("Config: {}", config);

        // Keep the main thread alive
        try
        {
            Thread.currentThread().join();
        } catch (InterruptedException interruptedException)
        {
            LOGGER.info("Server interrupted, shutting down all servers...");
            for (var server : servers)
            {
                server.stop(0);
            }
        }

        LOGGER.info("All servers finished");
    }

    /**
     * @param config
     * @return
     */
    private static RepositoryContainer getRepositories(Configuration config)
    {
        RepositoryContainer repositories;
        var storageType = config.getStorageType(); // Changed from config.getStorageConfig().getType()
        repositories = switch (storageType.toLowerCase())
        {
            case "in-memory" -> {
                LOGGER.info("Using In-Memory storage.");
                yield new RepositoryContainer(
                        InMemoryUserRepository.getInstance(), InMemoryAccountRepository.getInstance());
            }
            case "database" -> {
                // Placeholder for database setup.
                LOGGER.info("Database storage selected. (Implementation is a placeholder)");
                throw new UnsupportedOperationException("Database storage implementation is not yet complete.");
            }
            default -> {
                LOGGER.warn("Unknown storage type '{}'. Defaulting to In-Memory.", storageType);
                yield new RepositoryContainer(
                        InMemoryUserRepository.getInstance(), InMemoryAccountRepository.getInstance());
            }
        };

        return repositories;
    }
}
