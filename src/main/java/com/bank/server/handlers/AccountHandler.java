package com.bank.server.handlers;

import com.bank.business.entities.Account;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.server.dto.AccountResponse;
import com.bank.server.dto.CreateAccountRequest;
import com.bank.server.dto.TransactionRequest;
import com.bank.server.dto.TransactionResponse;
import com.bank.server.dto.TransferRequest;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountHandler implements HttpHandler {
    private final AccountService accountService;
    private final UserService userService;
    private final Executor executor;
    private final Logger LOGGER = LoggerFactory.getLogger(AccountHandler.class);

    public AccountHandler(AccountService accountService, UserService userService, Executor executor) {
        this.accountService = accountService;
        this.userService = userService;
        this.executor = executor;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        executor.execute(() -> {
            try {
                handleRequest(exchange);
            } catch (IOException e) {
                LOGGER.error("Error handling request: " + e.getMessage(), e);
                try {
                    sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
                } catch (IOException ioException) {
                    LOGGER.error("Failed to send error response: " + ioException.getMessage(), ioException);
                }
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        LOGGER.debug("Exchange {}", exchange);

        try {
            if ("POST".equals(method) && "/accounts".equals(path)) {
                handleCreateAccount(exchange);
            } else if ("GET".equals(method) && path.startsWith("/accounts/")) {
                handleGetAccountById(exchange);
            } else if ("GET".equals(method) && "/accounts".equals(path)) {
                handleGetAccountsByUser(exchange);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/deposit")) {
                handleDeposit(exchange);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/withdraw")) {
                handleWithdraw(exchange);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/transfer")) {
                handleTransfer(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e) {
            LOGGER.error("Error processing request: " + e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private String extractUserId(HttpExchange exchange) {
        // For this toy project, we'll extract user ID from request body
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (requestBody != null && !requestBody.isEmpty()) {
                JsonNode jsonNode = Json.parse(requestBody);
                if (jsonNode.has("userId")) {
                    return jsonNode.get("userId").asText();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error extracting user ID from request body: " + e.getMessage(), e);
        }
        return null;
    }

    private void handleCreateAccount(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            CreateAccountRequest request = Json.fromJson(jsonNode, CreateAccountRequest.class);

            // Extract user ID from request
            Long userId = request.getUserId();

            // Create account - account number will be auto-generated if not provided
            Account account;
            if (request.hasAccountNumber()) {
                account = accountService.createAccount(
                    request.getUserId(),
                    request.getAccountNumber(),
                    request.getInitialBalance(),
                    request.getType());
            } else {
                account = accountService.createAccount(
                    request.getUserId(),
                    request.getInitialBalance(),
                    request.getType());
            }

            AccountResponse response = new AccountResponse(account);
            String jsonResponse = Json.stringify(Json.toJson(response));
            sendResponse(exchange, 201, jsonResponse);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid user ID format: " + e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
        } catch (Exception e) {
            LOGGER.error("Error creating account: " + e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetAccountById(HttpExchange exchange) throws IOException {
        try {
            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);
            Optional<Account> accountOptional = accountService.getAccountById(accountId);

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                AccountResponse response = new AccountResponse(account);
                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid account ID format: " + e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID format\"}");
        } catch (Exception e) {
            LOGGER.error("Error getting account: " + e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetAccountsByUser(HttpExchange exchange) throws IOException {
        try {
            // Get user ID from query parameter or request body
            String query = exchange.getRequestURI().getQuery();
            Long userId = null;

            if (query != null && query.startsWith("userId=")) {
                String userIdStr = query.substring(7);
                userId = Long.parseLong(userIdStr);
            } else {
                // Try to extract from request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (requestBody != null && !requestBody.isEmpty()) {
                    JsonNode jsonNode = Json.parse(requestBody);
                    if (jsonNode.has("userId")) {
                        userId = jsonNode.get("userId").asLong();
                    }
                }
            }

            if (userId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: userId is required\"}");
                return;
            }

            List<Account> accounts = accountService.getAccountsByUserId(userId);
            List<AccountResponse> accountResponses = accounts.stream()
                    .map(AccountResponse::new)
                    .collect(Collectors.toList());

            String jsonResponse = Json.stringify(Json.toJson(accountResponses));
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid user ID format: " + e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
        } catch (Exception e) {
            LOGGER.error("Error getting accounts: " + e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleDeposit(HttpExchange exchange) throws IOException {
        try {
            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            TransactionRequest request = Json.fromJson(jsonNode, TransactionRequest.class);

            Optional<Account> accountOptional = accountService.getAccountById(accountId);
            if (!accountOptional.isPresent()) {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
                return;
            }

            Account account = accountOptional.get();
            account.addAmount(request.getAmount());

            // Update the account in the service
            accountService.updateAccount(account);

            TransactionResponse response = new TransactionResponse(
                    true,
                    "Deposit successful",
                    account.getBalance());

            String jsonResponse = Json.stringify(Json.toJson(response));
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid account ID or amount format: " + e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
        } catch (Exception e) {
            LOGGER.error("Error processing deposit: " + e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleWithdraw(HttpExchange exchange) throws IOException {
        try {
            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            TransactionRequest request = Json.fromJson(jsonNode, TransactionRequest.class);

            Optional<Account> accountOptional = accountService.getAccountById(accountId);
            if (!accountOptional.isPresent()) {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
                return;
            }

            Account account = accountOptional.get();
            boolean success = account.withdrawAmount(request.getAmount());

            if (success) {
                // Update the account in the service
                accountService.updateAccount(account);

                TransactionResponse response = new TransactionResponse(
                        true,
                        "Withdrawal successful",
                        account.getBalance());

                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 200, jsonResponse);
            } else {
                TransactionResponse response = new TransactionResponse(
                        false,
                        "Insufficient funds",
                        account.getBalance());

                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 400, jsonResponse);
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid account ID or amount format: " + e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
        } catch (Exception e) {
            LOGGER.error("Error processing withdrawal: " + e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleTransfer(HttpExchange exchange) throws IOException {
        try {
            // Extract account ID from path (source account)
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long fromAccountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            TransferRequest request = Json.fromJson(jsonNode, TransferRequest.class);

            // Verify that the source account exists
            Optional<Account> fromAccountOptional = accountService.getAccountById(fromAccountId);
            if (!fromAccountOptional.isPresent()) {
                sendResponse(exchange, 404, "{\"error\": \"Source account not found\"}");
                return;
            }

            // Verify that the destination account exists
            Optional<Account> toAccountOptional = accountService.getAccountById(request.getToAccountId());
            if (!toAccountOptional.isPresent()) {
                sendResponse(exchange, 400, "{\"error\": \"Destination account not found\"}");
                return;
            }

            Account fromAccount = fromAccountOptional.get();
            Account toAccount = toAccountOptional.get();

            // Perform the transfer
            boolean success = fromAccount.withdrawAmount(request.getAmount());
            if (success) {
                toAccount.addAmount(request.getAmount());

                // Update both accounts in the service
                accountService.updateAccount(fromAccount);
                accountService.updateAccount(toAccount);

                TransactionResponse response = new TransactionResponse(
                        true,
                        "Transfer successful",
                        fromAccount.getBalance());

                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 200, jsonResponse);
            } else {
                TransactionResponse response = new TransactionResponse(
                        false,
                        "Insufficient funds",
                        fromAccount.getBalance());

                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 400, jsonResponse);
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid account ID or amount format: " + e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
        } catch (Exception e) {
            LOGGER.error("Error processing transfer: " + e.getMessage(), e);
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
