package com.bank.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple handler that demonstrates custom logic.
 * This handler will simulate some work and respond with a message.
 */
public class PingHandler implements HttpHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PingHandler.class);

    private final Executor executor;

    public PingHandler(Executor executor)
    {
        this.executor = executor;
    }

    @Override
    public void handle(HttpExchange exchange)
    {
        // Submit the actual work to our custom executor
        // This allows us to control the threading model
        executor.execute(() -> {
            try
            {
                handleRequest(exchange);
            } catch (IOException e)
            {
                LOGGER.error("Error handling Ping request: {}", e.getMessage(), e);
                try
                {
                    sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
                } catch (IOException ioException)
                {
                    LOGGER.error("Failed to send Ping error response: {}", ioException.getMessage(), ioException);
                }
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException
    {
        // Simulate some work (e.g., database access, computation)

        var response = "PONG";
        sendResponse(exchange, 200, response);
    }

    /**
     * @param exchange
     * @param statusCode
     * @param response
     * @throws IOException
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException
    {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);

        try (var os = exchange.getResponseBody())
        {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
