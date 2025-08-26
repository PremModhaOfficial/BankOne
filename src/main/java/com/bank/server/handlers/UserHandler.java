package com.bank.server.handlers;

import com.bank.business.entities.User;
import com.bank.business.services.UserService;

import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

public class UserHandler implements HttpHandler {
    private final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);
    private final UserService userService;
    private final Executor executor;

    public UserHandler(UserService userService, Executor executor) {
        this.userService = userService;
        this.executor = executor;
    }

    @Override
    public void handle(HttpExchange exchange) {
        executor.execute(() -> {
            try {
                handleRequest(exchange);
            } catch (IOException e) {
                LOGGER.error("Error User handling request: {}", e.getMessage(), e);
                try {
                    sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
                } catch (IOException ioException) {
                    LOGGER.error("Failed to user send error response: {}", ioException.getMessage(), ioException);
                }
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method) && "/users".equals(path)) {
                handleCreateUser(exchange);
            } else if ("POST".equals(method) && "/login".equals(path)) {
                handleLogin(exchange);
            } else if ("GET".equals(method) && path.startsWith("/users/")) {
                handleGetUserById(exchange);
            } else if ("GET".equals(method) && "/admin/users".equals(path)) {
                handleGetAllUsers(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e) {
            LOGGER.error("Error processing request: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);

            LOGGER.debug("JSON is {}", requestBody);
            User request = new User(jsonNode.get("username").asText(), jsonNode.get("email").asText());

            LOGGER.info("Requested Creating user: {}", request);

            // Create user - the service will handle duplicates appropriately
            User user = userService.createUser(request.getUsername(), request.getEmail(), request.isAdmin());
            String jsonResponse = Json.stringify(Json.toJson(user));
            sendResponse(exchange, 201, jsonResponse);
        } catch (Exception e) {
            LOGGER.error("Error creating user: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: " + e.getMessage() + "\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            String userId = jsonNode.get("id").asText();

            // In a real app, you'd verify the password against the hashed version
            Optional<User> userOptional = userService.getUserByUsername(userId);
            if (userOptional.isEmpty()) {
                userOptional = userService.getUserByEmail(userId);
            }

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Generate a simple session token (user ID:email:password for this simplified
                // version)
                ObjectNode userNode = Json.toJson(user).deepCopy();
                ObjectNode response = Json.defaultObjectMapper().createObjectNode();
                response.set("user", userNode);

                String jsonResponse = Json.stringify(response);
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 401, "{\"error\": \"User not found\"}");
            }
        } catch (Exception e) {
            LOGGER.error("Error during login: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetUserById(HttpExchange exchange) throws IOException {
        try {
            // Extract user ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long userId = Long.parseLong(parts[2]);
            Optional<User> userOptional = userService.getUserById(userId);

            if (userOptional.isPresent()) {
                String jsonResponse = Json.stringify(Json.toJson(userOptional.get()));
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid user ID format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Invalid user ID format\"}");
        } catch (Exception e) {
            LOGGER.error("Error getting user: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetAllUsers(HttpExchange exchange) throws IOException {
        try {
            // Check for admin authorization (simplified)
            // In a real app, you'd check the JWT token
            List<User> users = userService.getAllUsers();
            String jsonResponse = Json.stringify(Json.toJson(users));
            sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            LOGGER.error("Error getting all users: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
