package com.bank.server;

import com.bank.server.handlers.SimpleHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A custom HTTP server using com.sun.net.httpserver.HttpServer
 * with configurable multithreading.
 */
public class CustomHttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomHttpServer.class);

    private final HttpServer server;
    private final Executor customExecutor;
    private final int threadPoolSize;

    public CustomHttpServer(int port, int threadPoolSize) throws IOException {
        this.threadPoolSize = threadPoolSize;

        // Create the server
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Create a custom executor with a fixed thread pool
        // You can customize this to use different threading models
        customExecutor = Executors.newFixedThreadPool(threadPoolSize, new CustomThreadFactory());

        // Set the custom executor for the server
        // This controls how incoming requests are handled
        server.setExecutor(customExecutor);

        // Register your handlers
        server.createContext("/", new SimpleHandler(customExecutor));
        // You can add more contexts here for different endpoints
        // server.createContext("/users", new UserHandler(customExecutor));
        // server.createContext("/accounts", new AccountHandler(customExecutor));

        // Create a context that uses the server's default executor (for comparison)
        server.createContext("/default", new SimpleHandler(server.getExecutor()));
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
