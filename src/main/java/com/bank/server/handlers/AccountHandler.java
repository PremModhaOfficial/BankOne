package com.bank.server.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.entities.Account;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AccountHandler implements HttpHandler
{
    private final AccountService accountService;
    private final Executor executor;
    private final Logger LOGGER = LoggerFactory.getLogger(AccountHandler.class);

    public AccountHandler(AccountService accountService, UserService userService, Executor executor)
    {
        this.accountService = accountService;
        this.executor = executor;
    }

    @Override
    public void handle(HttpExchange exchange)
    {
        executor.execute(() -> {
            try
            {
                handleRequest(exchange);
            } catch (IOException e)
            {
                LOGGER.error("Error handling request: {}", e.getMessage(), e);
                try
                {
                    sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
                } catch (IOException ioException)
                {
                    LOGGER.error("Failed to send error response: {}", ioException.getMessage(), ioException);
                }
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException
    {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        LOGGER.debug("REQUEST {}@{}", method, path);

        try
        {
            if ("POST".equals(method) && "/accounts".equals(path))
            {
                handleCreateAccount(exchange);
            } else if ("GET".equals(method) && path.startsWith("/accounts/"))
            {
                handleGetAccountById(exchange);
            } else if ("GET".equals(method) && "/accounts-all".equals(path))
            {
                handleGetAllAccounts(exchange);
            } else if ("GET".equals(method) && "/accounts".equals(path))
            {
                handleGetAccountsByUser(exchange);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/deposit"))
            {
                handleDeposit(exchange);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/withdraw"))
            {
                handleWithdraw(exchange);
            } else if ("POST".equals(method) && path.startsWith("/accounts/") && path.endsWith("/transfer"))
            {
                handleTransfer(exchange);
            } else
            {
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
            }
        } catch (Exception e)
        {
            LOGGER.error("Error processing request: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleCreateAccount(HttpExchange exchange) throws IOException
    {
        try
        {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);

            // Extract values directly from JSON
            Long userId = jsonNode.get("userId").asLong();
            BigDecimal initialBalance;
            if (jsonNode.has("initialBalance"))
            {
                initialBalance = new BigDecimal(jsonNode.get("initialBalance").asText());
            } else
            {
                initialBalance = new BigDecimal(jsonNode.get("balance").asText());
            }
            Account.AccountType type = Account.AccountType.valueOf(jsonNode.get("type").asText());

            // Create account - account number will be auto-generated if not provided
            Account account;
            if (jsonNode.has("accountNumber") && !jsonNode.get("accountNumber").isNull())
            {
                String accountNumber = jsonNode.get("accountNumber").asText();
                account = accountService.createAccount(userId, accountNumber, initialBalance, type);
            } else
            {
                account = accountService.createAccount(userId, initialBalance, type);
            }

            String jsonResponse = Json.stringify(Json.toJson(account));
            sendResponse(exchange, 201, jsonResponse);
        } catch (NumberFormatException e)
        {
            LOGGER.error("Invalid user ID format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
        } catch (Exception e)
        {
            LOGGER.error("Error creating account: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetAccountById(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);
            Account account = accountService.getAccountById(accountId);

            if (account != null)
            {
                String jsonResponse = Json.stringify(Json.toJson(account));
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
            }
        } catch (NumberFormatException e)
        {
            LOGGER.error("Invalid account ID format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID format\"}");
        } catch (Exception e)
        {
            LOGGER.error("Error getting account: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetAllAccounts(HttpExchange exchange) throws IOException
    {
        try
        {
            List<Account> accounts = accountService.getAllAccounts();
            String jsonResponse = Json.stringify(Json.toJson(accounts));
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e)
        {
            LOGGER.error("Invalid user ID format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
        } catch (Exception e)
        {
            LOGGER.error("Error getting accounts: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleGetAccountsByUser(HttpExchange exchange) throws IOException
    {
        try
        {
            // Get user ID from query parameter or request body
            String query = exchange.getRequestURI().getQuery();
            Long userId = null;

            if (query != null && query.startsWith("userId="))
            {
                String userIdStr = query.substring(7);
                userId = Long.parseLong(userIdStr);
            } else
            {
                // Try to extract from request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (!requestBody.isEmpty())
                {
                    JsonNode jsonNode = Json.parse(requestBody);
                    if (jsonNode.has("userId"))
                    {
                        userId = jsonNode.get("userId").asLong();
                    }
                }
            }

            if (userId == null)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: userId is required\"}");
                return;
            }

            List<Account> accounts = accountService.getAccountsByUserId(userId);
            String jsonResponse = Json.stringify(Json.toJson(accounts));
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e)
        {
            LOGGER.error("Invalid user ID format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
        } catch (Exception e)
        {
            LOGGER.error("Error getting accounts: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleDeposit(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            BigDecimal amount = new BigDecimal(jsonNode.get("amount").asText());

            if (amount.compareTo(BigDecimal.ZERO) < 0)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Negative Deposit Not Possible\"}");
                return;
            }

            Account account = accountService.getAccountById(accountId);
            if (account == null)
            {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
                return;
            }

            account.addAmount(amount);

            // Update the account in the service
            accountService.updateAccount(account);

            ObjectNode response = Json.defaultObjectMapper().createObjectNode();
            response.put("success", true);
            response.put("message", "Deposit successful");
            response.put("balance", account.getBalance().toString());

            String jsonResponse = Json.stringify(response);
            sendResponse(exchange, 200, jsonResponse);
        } catch (NumberFormatException e)
        {
            LOGGER.error("Invalid account ID or amount format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
        } catch (Exception e)
        {
            LOGGER.error("Error processing deposit: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleWithdraw(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            Long accountId = Long.parseLong(parts[2]);

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = Json.parse(requestBody);
            BigDecimal amount = new BigDecimal(jsonNode.get("amount").asText());

            Account account = accountService.getAccountById(accountId);
            if (account == null)
            {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
                return;
            }
            boolean success = account.withdrawAmount(amount);

            ObjectNode response = Json.defaultObjectMapper().createObjectNode();
            response.put("success", success);
            response.put("balance", account.getBalance().toString());

            if (success)
            {
                response.put("message", "Withdrawal successful");
                // Update the account in the service
                accountService.updateAccount(account);
                String jsonResponse = Json.stringify(response);
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                response.put("message", "Insufficient funds");
                String jsonResponse = Json.stringify(response);
                sendResponse(exchange, 400, jsonResponse);
            }
        } catch (NumberFormatException e)
        {
            LOGGER.error("Invalid account ID or amount format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
        } catch (Exception e)
        {
            LOGGER.error("Error processing withdrawal: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void handleTransfer(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path (source account)
            String[] parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            LOGGER.debug("GOT requestBody: {}", requestBody);
            JsonNode jsonNode = Json.parse(requestBody);

            Long fromAccountId = Long.parseLong(parts[2]);
            Long toAccountId = jsonNode.get("toAccountId").asLong();
            BigDecimal amount = new BigDecimal(jsonNode.get("amount").asText());

            if (amount.compareTo(BigDecimal.ZERO) < 0)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Negative Transfer Amount is Not Allowed\"}");
                return;
            }

            Account fromAccount = accountService.getAccountById(fromAccountId);
            if (fromAccount == null)
            {
                sendResponse(exchange, 404, "{\"error\": \"Source account not found\"}");
                return;
            }

            // Verify that the destination account exists
            Account toAccount = accountService.getAccountById(toAccountId);
            if (toAccount == null)
            {
                sendResponse(exchange, 400, "{\"error\": \"Destination account not found\"}");
                return;
            }

            // Perform the atomic transfer
            boolean success = accountService.transferAmount(fromAccountId, toAccountId, amount);

            ObjectNode response = Json.defaultObjectMapper().createObjectNode();
            response.put("success", success);

            if (success)
            {
                Account updatedFromAccount = accountService.getAccountById(fromAccountId);
                response.put("message", "Transfer successful");
                response.put("balance", updatedFromAccount.getBalance().toString());
                String jsonResponse = Json.stringify(response);
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                response.put("message", "Insufficient funds or account not found");
                response.put("balance", fromAccount.getBalance().toString());
                String jsonResponse = Json.stringify(response);
                sendResponse(exchange, 400, jsonResponse);
            }
        } catch (NumberFormatException e)
        {
            LOGGER.error("Invalid account ID or amount format: {}", e.getMessage(), e);
            sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
        } catch (Exception e)
        {
            LOGGER.error("Error processing transfer: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException
    {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
