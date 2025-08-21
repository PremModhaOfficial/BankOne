package com.prem;

import com.prem.business.repositories.AccountRepository;
import com.prem.business.repositories.UserRepository;
import com.prem.business.services.AccountService;
import com.prem.business.services.UserService;
import com.prem.infrastructure.persistence.inmemory.InMemoryAccountRepository;
import com.prem.infrastructure.persistence.inmemory.InMemoryUserRepository;
import com.prem.server.config.Configuration;
import com.prem.server.config.ConfigurationManager;
import com.prem.server.config.HttpConfigurationException;
import com.prem.server.core.ServerListnerThread;
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

        // --- Dependency Injection Setup ---
        UserRepository userRepository;
        AccountRepository accountRepository;

        String storageType = config.getStorageConfig().getType();
        switch (storageType.toLowerCase()) {
            case "in-memory":
                LOGGER.info("Using In-Memory storage.");
                userRepository = InMemoryUserRepository.getInstance();
                accountRepository = InMemoryAccountRepository.getInstance();
                break;
            case "database":
                // Placeholder for database setup.
                // You would initialize DatabaseUserRepository and DatabaseAccountRepository
                // here,
                // likely requiring a DataSource or EntityManager.
                LOGGER.info("Database storage selected. (Implementation is a placeholder)");
                // Example (conceptual, won't compile without actual DB setup):
                // DataSource dataSource = ... ; // Obtain DataSource
                // userRepository = new DatabaseUserRepository(dataSource);
                // accountRepository = new DatabaseAccountRepository(dataSource);
                // For now, fall back or throw an error if DB setup is incomplete.
                throw new UnsupportedOperationException("Database storage implementation is not yet complete.");
            default:
                LOGGER.warn("Unknown storage type '{}'. Defaulting to In-Memory.", storageType);
                userRepository = InMemoryUserRepository.getInstance();
                accountRepository = InMemoryAccountRepository.getInstance();
        }

        // Initialize Services with the chosen repositories
        UserService userService = new UserService(userRepository);
        AccountService accountService = new AccountService(accountRepository);
        // -----------------------------

        LOGGER.info("http://localhost:" + config.getPort());
        LOGGER.info("config : {}", config);
        ServerListnerThread slt = new ServerListnerThread(config);

        slt.start();

        try {
            slt.join();
        } catch (InterruptedException e) {
            LOGGER.error("The Listener Thread Is Interrupted while working", e);
        }

        LOGGER.info("Server Finished");
    }
}
