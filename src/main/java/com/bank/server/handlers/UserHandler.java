package com.bank.server.handlers;

import com.bank.business.entities.User;
import com.bank.business.services.UserService;
import com.bank.server.dto.CreateUserRequest;
import com.bank.server.dto.LoginRequest;
import com.bank.server.dto.LoginResponse;
import com.bank.server.dto.UserResponse;
import com.bank.server.util.JWTUtil;
import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class UserHandler implements HttpHandler {
    private final UserService userService;
    private final Executor executor;

    public UserHandler(UserService userService, Executor executor) {
        this.userService = userService;
        this.executor = executor;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private void handleCreateUser(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            CreateUserRequest request = Json.fromJson(jsonNode, CreateUserRequest.class);

            // Create user (in a real app, you'd hash the password)
            User user = userService.createUser(request.getUsername(), request.getEmail(), request.getPassword(),
                    request.isAdmin());
            UserResponse response = new UserResponse(user);

            String jsonResponse = Json.stringify(Json.toJson(response));
            sendResponse(exchange, 201, jsonResponse);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            LoginRequest request = Json.fromJson(jsonNode, LoginRequest.class);

            // In a real app, you'd verify the password against the hashed version
            Optional<User> userOptional = userService.getUserByUsername(request.getIdentifier());
            if (!userOptional.isPresent()) {
                userOptional = userService.getUserByEmail(request.getIdentifier());
            }

            if (userOptional.isPresent() &&
                    userOptional.get().getHashedPassword() == request.getPassword().hashCode()) { // Simplified password
                                                                                                  // check
                User user = userOptional.get();
                String token = JWTUtil.generateToken(user.getId().toString()); // Simplified token generation
                UserResponse userResponse = new UserResponse(user);
                LoginResponse response = new LoginResponse(token, userResponse);

                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
        }
    }

    private void handleGetUserById(HttpExchange exchange) throws IOException {
        try {
            // Extract user ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
                return;
            }

            Long userId = Long.parseLong(parts[2]);
            Optional<User> userOptional = userService.getUserById(userId);

            if (userOptional.isPresent()) {
                UserResponse response = new UserResponse(userOptional.get());
                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid user ID\"}");
        } catch (Exception e) {
            System.err.println("Error getting user: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private void handleGetAllUsers(HttpExchange exchange) throws IOException {
        try {
            // Check for admin authorization (simplified)
            // In a real app, you'd check the JWT token
            List<User> users = userService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            String jsonResponse = Json.stringify(Json.toJson(userResponses));
            sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
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
