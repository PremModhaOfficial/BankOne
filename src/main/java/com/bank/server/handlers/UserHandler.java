package com.bank.server.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.services.UserService;
import com.bank.server.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UserHandler implements HttpHandler
{
    private final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);
    private final UserService userService;
    private final Executor executor;

    public UserHandler(UserService userService, Executor executor)
    {
        this.userService = userService;
        this.executor = executor;
    }

    @Override
    public void handle(HttpExchange exchange)
    {
        executor.execute(() -> {
            try
            {
                handleRequest(exchange);
            } catch (IOException ioException)
            {
                LOGGER.error("Error User handling request: {}", ioException.getMessage(), ioException);
                try
                {
                    sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + ioException.getMessage() + "\"}");
                } catch (IOException responseException)
                {
                    LOGGER.error("Failed to user send error response: {}", responseException.getMessage(), responseException);
                }
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException
    {
        var method = exchange.getRequestMethod();
        var path = exchange.getRequestURI().getPath();

        try
        {
            if ("POST".equals(method) && "/users".equals(path))
            {
                handleCreateUser(exchange);
            } else if ("POST".equals(method) && "/login".equals(path))
            {
                handleLogin(exchange);
            } else if ("GET".equals(method) && path.startsWith("/users/"))
            {
                handleGetUserById(exchange);
            } else if ("GET".equals(method) && "/admin/users".equals(path))
            {
                handleGetAllUsers(exchange);
            } else
            {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception requestProcessingException)
        {
            LOGGER.error("Error processing request: {}", requestProcessingException.getMessage(), requestProcessingException);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + requestProcessingException.getMessage() + "\"}");
        }
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException
    {
        try
        {
            var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var jsonNode = Json.parse(requestBody);

            LOGGER.debug("JSON is {}", requestBody);

            // Validate required fields
            if (!jsonNode.has("email") || !jsonNode.has("username") || !jsonNode.has("password"))
            {
                sendResponse(exchange, 400, "{\"error\": \"Email, username, and password are required\"}");
                return;
            }

            var email = jsonNode.get("email").asText();
            var username = jsonNode.get("username").asText();
            var password = jsonNode.get("password").asText();

            // Basic input validation
            if (email.trim().isEmpty() || username.trim().isEmpty() || password.trim().isEmpty())
            {
                sendResponse(exchange, 400, "{\"error\": \"Email, username, and password cannot be empty\"}");
                return;
            }

            boolean isAdmin;
            if (jsonNode.has("admin"))
            {
                isAdmin = jsonNode.get("admin").asBoolean();
            } else
            {
                isAdmin = false;
            }

            // Create user - the service will handle duplicates appropriately
            var user = userService.createUser(username.trim(), email.trim(), password, isAdmin);
            LOGGER.info("Created user: {}", user.getUsername());

            // Remove password from response
            var userNode = Json.toJson(user).deepCopy();
            ((com.fasterxml.jackson.databind.node.ObjectNode) userNode).remove("password");

            var jsonResponse = Json.stringify(userNode);
            sendResponse(exchange, 201, jsonResponse);
        } catch (IllegalArgumentException validationException)
        {
            LOGGER.warn("Password validation failed: {}", validationException.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"" + validationException.getMessage() + "\"}");
        } catch (Exception userCreationException)
        {
            LOGGER.error("Error creating user: {}", userCreationException.getMessage(), userCreationException);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: " + userCreationException.getMessage() + "\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException
    {
        try
        {
            var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var jsonNode = Json.parse(requestBody);

            if (!jsonNode.has("username") || !jsonNode.has("password"))
            {
                sendResponse(exchange, 400, "{\"error\": \"Username and password are required\"}");
                return;
            }

            var username = jsonNode.get("username").asText();
            var password = jsonNode.get("password").asText();

            // Find user by username first, then by email
            var user = userService.getUserByUsername(username);
            if (user == null)
            {
                user = userService.getUserByEmail(username);
            }

            if (user != null && user.validatePassword(password))
            {
                // Remove password from response for security
                var userNode = Json.toJson(user).deepCopy();
                ((com.fasterxml.jackson.databind.node.ObjectNode) userNode).remove("password");

                var response = Json.defaultObjectMapper().createObjectNode();
                response.set("user", userNode);
                response.put("message", "Login successful");

                var jsonResponse = Json.stringify(response);
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                sendResponse(exchange, 401, "{\"error\": \"Invalid username or password\"}");
            }
        } catch (Exception loginException)
        {
            LOGGER.error("Error during login: {}", loginException.getMessage(), loginException);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: " + loginException.getMessage() + "\"}");
        }
    }

    private void handleGetUserById(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract user ID from path
            var parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            var userId = Long.parseLong(parts[2]);
            var userOptional = userService.getUserById(userId);

            if (userOptional != null)
            {
                var jsonResponse = Json.stringify(Json.toJson(userOptional));
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
            }
        } catch (NumberFormatException numberFormatException)
        {
            LOGGER.error("Invalid user ID format: {}", numberFormatException.getMessage(), numberFormatException);
            sendResponse(exchange, 400, "{\"error\": \"Invalid user ID format\"}");
        } catch (Exception userRetrievalException)
        {
            LOGGER.error("Error getting user: {}", userRetrievalException.getMessage(), userRetrievalException);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + userRetrievalException.getMessage() + "\"}");
        }
    }

    private void handleGetAllUsers(HttpExchange exchange) throws IOException
    {
        try
        {
            // Check for admin authorization (simplified)
            // In a real app, you'd check the JWT token
            var users = userService.getAllUsers();
            var jsonResponse = Json.stringify(Json.toJson(users));
            sendResponse(exchange, 200, jsonResponse);
        } catch (Exception usersRetrievalException)
        {
            LOGGER.error("Error getting all users: {}", usersRetrievalException.getMessage(), usersRetrievalException);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + usersRetrievalException.getMessage() + "\"}");
        }
    }

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
