package com.bank.server.handlers;

import com.bank.business.entities.Account;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.server.dto.AccountResponse;
import com.bank.server.dto.CreateAccountRequest;
import com.bank.server.dto.TransactionRequest;
import com.bank.server.dto.TransactionResponse;
import com.bank.server.dto.TransferRequest;
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

public class AccountHandler implements HttpHandler {
    private final AccountService accountService;
    private final UserService userService;
    private final Executor executor;

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
                System.err.println("Error handling request: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // Check for authentication token in Authorization header
            String token = extractToken(exchange);
            if (token == null) {
                sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
                return;
            }

            if ("POST".equals(method) && "/accounts".equals(path)) {
                handleCreateAccount(exchange, token);
            } else if ("GET".equals(method) && path.startsWith("/accounts/")) {
                handleGetAccountById(exchange, token);
            } else if ("GET".equals(method) && "/accounts".equals(path)) {
                handleGetAccountsByUser(exchange, token);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/deposit")) {
                handleDeposit(exchange, token);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/withdraw")) {
                handleWithdraw(exchange, token);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/transfer")) {
                handleTransfer(exchange, token);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e) {
            System.err.println("Error processing request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private String extractToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void handleCreateAccount(HttpExchange exchange, String token) throws IOException {
        try {
            // Verify token and get user ID
            String userIdStr = JWTUtil.extractUserId(token);
            if (userIdStr == null) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            CreateAccountRequest request = Json.fromJson(jsonNode, CreateAccountRequest.class);

            // Verify that the user is creating an account for themselves or is an admin
            // For simplicity, we'll assume the user can only create accounts for themselves
            if (!userId.equals(request.getUserId())) {
                sendResponse(exchange, 403, "{\"error\": \"Forbidden\"}");
                return;
            }

            Account account = accountService.createAccount(
                    request.getUserId(),
                    request.getAccountNumber(),
                    request.getInitialBalance(),
                    request.getType());

            AccountResponse response = new AccountResponse(account);
            String jsonResponse = Json.stringify(Json.toJson(response));
            sendResponse(exchange, 201, jsonResponse);
        } catch (Exception e) {
            System.err.println("Error creating account: " + e.getMessage());
            sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
        }
    }

    private void handleGetAccountById(HttpExchange exchange, String token) throws IOException {
        try {
            // Verify token and get user ID
            String userIdStr = JWTUtil.extractUserId(token);
            if (userIdStr == null) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);
            Optional<Account> accountOptional = accountService.getAccountById(accountId);

            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                // Check if user owns this account or is an admin
                if (!userId.equals(account.getUserId())) {
                    sendResponse(exchange, 403, "{\"error\": \"Forbidden\"}");
                    return;
                }

                AccountResponse response = new AccountResponse(account);
                String jsonResponse = Json.stringify(Json.toJson(response));
                sendResponse(exchange, 200, jsonResponse);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid account ID\"}");
        } catch (Exception e) {
            System.err.println("Error getting account: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private void handleGetAccountsByUser(HttpExchange exchange, String token) throws IOException {
        try {
            // Verify token and get user ID
            String userIdStr = JWTUtil.extractUserId(token);
            if (userIdStr == null) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            // Get query parameter for user ID (if provided)
            String query = exchange.getRequestURI().getQuery();
            Long requestedUserId = userId; // Default to current user

            if (query != null && query.startsWith("userId=")) {
                String requestedUserIdStr = query.substring(7);
                requestedUserId = Long.parseLong(requestedUserIdStr);
            }

            // Check if user is requesting their own accounts or is an admin
            // For simplicity, we'll assume users can only see their own accounts
            if (!userId.equals(requestedUserId)) {
                sendResponse(exchange, 403, "{\"error\": \"Forbidden\"}");
                return;
            }

            List<Account> accounts = accountService.getAccountsByUserId(requestedUserId);
            List<AccountResponse> accountResponses = accounts.stream()
                    .map(AccountResponse::new)
                    .collect(Collectors.toList());

            String jsonResponse = Json.stringify(Json.toJson(accountResponses));
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid user ID\"}");
        } catch (Exception e) {
            System.err.println("Error getting accounts: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private void handleDeposit(HttpExchange exchange, String token) throws IOException {
        try {
            // Verify token and get user ID
            String userIdStr = JWTUtil.extractUserId(token);
            if (userIdStr == null) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            TransactionRequest request = Json.fromJson(jsonNode, TransactionRequest.class);

            // Verify that the account belongs to the user
            Optional<Account> accountOptional = accountService.getAccountById(accountId);
            if (!accountOptional.isPresent() || !userId.equals(accountOptional.get().getUserId())) {
                sendResponse(exchange, 403, "{\"error\": \"Forbidden\"}");
                return;
            }

            Account account = accountOptional.get();
            account.addAmmount(request.getAmount());

            // Update the account in the service
            accountService.updateAccount(account);

            TransactionResponse response = new TransactionResponse(
                    true,
                    "Deposit successful",
                    account.getBalance());

            String jsonResponse = Json.stringify(Json.toJson(response));
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid account ID or amount\"}");
        } catch (Exception e) {
            System.err.println("Error processing deposit: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private void handleWithdraw(HttpExchange exchange, String token) throws IOException {
        try {
            // Verify token and get user ID
            String userIdStr = JWTUtil.extractUserId(token);
            if (userIdStr == null) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            TransactionRequest request = Json.fromJson(jsonNode, TransactionRequest.class);

            // Verify that the account belongs to the user
            Optional<Account> accountOptional = accountService.getAccountById(accountId);
            if (!accountOptional.isPresent() || !userId.equals(accountOptional.get().getUserId())) {
                sendResponse(exchange, 403, "{\"error\": \"Forbidden\"}");
                return;
            }

            Account account = accountOptional.get();
            boolean success = account.withdrawAmmount(request.getAmount());

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
            sendResponse(exchange, 400, "{\"error\": \"Invalid account ID or amount\"}");
        } catch (Exception e) {
            System.err.println("Error processing withdrawal: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
        }
    }

    private void handleTransfer(HttpExchange exchange, String token) throws IOException {
        try {
            // Verify token and get user ID
            String userIdStr = JWTUtil.extractUserId(token);
            if (userIdStr == null) {
                sendResponse(exchange, 401, "{\"error\": \"Invalid token\"}");
                return;
            }

            Long userId = Long.parseLong(userIdStr);

            // Extract account ID from path (source account)
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request\"}");
                return;
            }

            Long fromAccountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            TransferRequest request = Json.fromJson(jsonNode, TransferRequest.class);

            // Verify that the source account belongs to the user
            Optional<Account> fromAccountOptional = accountService.getAccountById(fromAccountId);
            if (!fromAccountOptional.isPresent() || !userId.equals(fromAccountOptional.get().getUserId())) {
                sendResponse(exchange, 403, "{\"error\": \"Forbidden\"}");
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
            boolean success = fromAccount.withdrawAmmount(request.getAmount());
            if (success) {
                toAccount.addAmmount(request.getAmount());

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
            sendResponse(exchange, 400, "{\"error\": \"Invalid account ID or amount\"}");
        } catch (Exception e) {
            System.err.println("Error processing transfer: " + e.getMessage());
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
