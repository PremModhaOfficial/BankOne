package com.bank.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.server.handlers.AccountHandler;
import com.bank.server.handlers.PingHandler;
import com.bank.server.handlers.UserHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A custom HTTP server using com.sun.net.httpserver.HttpServer
 * with configurable multithreading.
 */
public class CustomHttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomHttpServer.class);

    private final HttpServer server;
    private final Executor customExecutor;
    private final int threadPoolSize;

    public CustomHttpServer(int port, int threadPoolSize, UserService userService, AccountService accountService)
            throws IOException {
        this.threadPoolSize = threadPoolSize;
        // Create the server
        server = HttpServer.create(new InetSocketAddress(port), 1000);

        // customExecutor = Executors.newFixedThreadPool(threadPoolSize, new
        // CustomThreadFactory());
        customExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Set the custom executor for the server
        // This controls how incoming requests are handled
        server.setExecutor(customExecutor);

        // Register your handlers
        server.createContext("/", new PingHandler(customExecutor));
        server.createContext("/users", new UserHandler(userService, customExecutor));
        server.createContext("/login", new UserHandler(userService, customExecutor));
        server.createContext("/admin/users", new UserHandler(userService, customExecutor));

        // Create contexts for account operations
        server.createContext("/accounts", new AccountHandler(accountService, userService, customExecutor));
        server.createContext("/accounts-all", new AccountHandler(accountService, userService, customExecutor));
        // For
        // /accounts/{id}
        server.createContext("/accounts/", new AccountHandler(accountService, userService, customExecutor));

        // Create a context that uses the server's default executor (for comparison)
        server.createContext("/default", new PingHandler(server.getExecutor()));
    }

    public void start() {
        server.start();
        LOGGER.info("Server started on port {}", server.getAddress().getPort());
        LOGGER.info("Configured thread pool size: {}", threadPoolSize);
    }

    public void stop(int delay) {
        server.stop(delay);
        LOGGER.info("Server stopped");
    }

    /**
     * A custom thread factory to name threads for better debugging.
     */
    static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "CustomHttpServer-" + threadNumber.getAndIncrement());
            t.setDaemon(false); // User threads, not daemon threads
            return t;
        }
    }
}
