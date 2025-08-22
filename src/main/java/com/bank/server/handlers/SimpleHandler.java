package com.bank.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

/**
 * A simple handler that demonstrates custom logic.
 * This handler will simulate some work and respond with a message.
 */
public class SimpleHandler implements HttpHandler {

    private final Executor executor;

    public SimpleHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Submit the actual work to our custom executor
        // This allows us to control the threading model
        executor.execute(() -> {
            try {
                handleRequest(exchange);
            } catch (IOException e) {
                System.err.println("Error handling request: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        // Simulate some work (e.g., database access, computation)
        try {
            Thread.sleep(100); // Simulate 100ms of work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String response = "Hello from custom handler with custom threading!";
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
