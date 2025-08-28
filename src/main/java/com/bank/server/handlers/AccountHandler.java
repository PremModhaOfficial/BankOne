package com.bank.server.handlers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.entities.Account;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.server.util.Json;
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
             } catch (IOException ioException)
             {
                 LOGGER.error("Error handling request: {}", ioException.getMessage(), ioException);
                 try
                 {
                     sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + ioException.getMessage() + "\"}");
                 } catch (IOException responseException)
                 {
                     LOGGER.error("Failed to send error response: {}", responseException.getMessage(), responseException);
                 }
            }
        });
    }

    private void handleRequest(HttpExchange exchange) throws IOException
    {
        var method = exchange.getRequestMethod();
        var path = exchange.getRequestURI().getPath();

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
         } catch (Exception requestProcessingException)
         {
             LOGGER.error("Error processing request: {}", requestProcessingException.getMessage(), requestProcessingException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + requestProcessingException.getMessage() + "\"}");
         }
    }

    /**
     * @param exchange
     * @throws IOException
     */
    private void handleCreateAccount(HttpExchange exchange) throws IOException
    {
        try
        {
            var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var jsonNode = Json.parse(requestBody);

            // Extract values directly from JSON
            var userId = jsonNode.get("userId").asLong();
            BigDecimal initialBalance;
            if (jsonNode.has("initialBalance"))
            {
                initialBalance = new BigDecimal(jsonNode.get("initialBalance").asText());
            } else
            {
                initialBalance = new BigDecimal(jsonNode.get("balance").asText());
            }
            var type = Account.AccountType.valueOf(jsonNode.get("type").asText());

            // Create account - account number will be auto-generated if not provided
            Account account;
            if (jsonNode.has("accountNumber") && !jsonNode.get("accountNumber").isNull())
            {
                var accountNumber = jsonNode.get("accountNumber").asText();
                account = accountService.createAccount(userId, accountNumber, initialBalance, type);
            } else
            {
                account = accountService.createAccount(userId, initialBalance, type);
            }

            var jsonResponse = Json.stringify(Json.toJson(account));
            sendResponse(exchange, 201, jsonResponse);
         } catch (NumberFormatException numberFormatException)
         {
             LOGGER.error("Invalid user ID format: {}", numberFormatException.getMessage(), numberFormatException);
             sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
         } catch (Exception accountCreationException)
         {
             LOGGER.error("Error creating account: {}", accountCreationException.getMessage(), accountCreationException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + accountCreationException.getMessage() + "\"}");
         }
    }

    /**
     * @param exchange
     * @throws IOException
     */
    private void handleGetAccountById(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path
            var parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            var accountId = Long.parseLong(parts[2]);
            var account = accountService.getAccountById(accountId);

            if (account != null)
            {
                var jsonResponse = Json.stringify(Json.toJson(account));
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
            }
         } catch (NumberFormatException numberFormatException)
         {
             LOGGER.error("Invalid account ID format: {}", numberFormatException.getMessage(), numberFormatException);
             sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID format\"}");
         } catch (Exception accountRetrievalException)
         {
             LOGGER.error("Error getting account: {}", accountRetrievalException.getMessage(), accountRetrievalException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + accountRetrievalException.getMessage() + "\"}");
         }
    }

     private void handleGetAllAccounts(HttpExchange exchange) throws IOException
     {
         try
         {
             var accounts = accountService.getAllAccounts();
             var jsonResponse = Json.stringify(Json.toJson(accounts));
             sendResponse(exchange, 200, jsonResponse);
         } catch (NumberFormatException numberFormatException)
         {
             LOGGER.error("Invalid user ID format: {}", numberFormatException.getMessage(), numberFormatException);
             sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
         } catch (Exception accountsRetrievalException)
         {
             LOGGER.error("Error getting accounts: {}", accountsRetrievalException.getMessage(), accountsRetrievalException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + accountsRetrievalException.getMessage() + "\"}");
         }
     }

    /**
     * @param exchange
     * @throws IOException
     */
    private void handleGetAccountsByUser(HttpExchange exchange) throws IOException
    {
        try
        {
            // Get user ID from query parameter or request body
            var query = exchange.getRequestURI().getQuery();
            Long userId = null;

            if (query != null && query.startsWith("userId="))
            {
                var userIdStr = query.substring(7);
                userId = Long.parseLong(userIdStr);
            } else
            {
                // Try to extract from request body
                var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                if (!requestBody.isEmpty())
                {
                    var jsonNode = Json.parse(requestBody);
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

            var accounts = accountService.getAccountsByUserId(userId);
            var jsonResponse = Json.stringify(Json.toJson(accounts));
            sendResponse(exchange, 200, jsonResponse);
         } catch (NumberFormatException numberFormatException)
         {
             LOGGER.error("Invalid user ID format: {}", numberFormatException.getMessage(), numberFormatException);
             sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid user ID format\"}");
         } catch (Exception accountsByUserRetrievalException)
         {
             LOGGER.error("Error getting accounts: {}", accountsByUserRetrievalException.getMessage(), accountsByUserRetrievalException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + accountsByUserRetrievalException.getMessage() + "\"}");
         }
     }

    private void handleDeposit(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path
            var parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            var accountId = Long.parseLong(parts[2]);

            var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var jsonNode = Json.parse(requestBody);
            var amount = new BigDecimal(jsonNode.get("amount").asText());

            if (amount.compareTo(BigDecimal.ZERO) < 0)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Negative Deposit Not Possible\"}");
                return;
            }

            var account = accountService.getAccountById(accountId);
            if (account == null)
            {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
                return;
            }

            account.addAmount(amount);

            // Update the account in the service
            accountService.updateAccount(account);

            var response = Json.defaultObjectMapper().createObjectNode();
            response.put("success", true);
            response.put("message", "Deposit successful");
            response.put("balance", account.getBalance().toString());

            var jsonResponse = Json.stringify(response);
            sendResponse(exchange, 200, jsonResponse);
         } catch (NumberFormatException numberFormatException)
         {
             LOGGER.error("Invalid account ID or amount format: {}", numberFormatException.getMessage(), numberFormatException);
             sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
         } catch (Exception depositProcessingException)
         {
             LOGGER.error("Error processing deposit: {}", depositProcessingException.getMessage(), depositProcessingException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + depositProcessingException.getMessage() + "\"}");
         }
     }

    /**
     * @param exchange
     * @throws IOException
     */
    private void handleWithdraw(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path
            var parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            var accountId = Long.parseLong(parts[2]);

            var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            var jsonNode = Json.parse(requestBody);
            var amount = new BigDecimal(jsonNode.get("amount").asText());

            var account = accountService.getAccountById(accountId);
            if (account == null)
            {
                sendResponse(exchange, 404, "{\"error\": \"Account not found\"}");
                return;
            }
            var success = account.withdrawAmount(amount);

            var response = Json.defaultObjectMapper().createObjectNode();
            response.put("success", success);
            response.put("balance", account.getBalance().toString());

            if (success)
            {
                response.put("message", "Withdrawal successful");
                // Update the account in the service
                accountService.updateAccount(account);
                var jsonResponse = Json.stringify(response);
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                response.put("message", "Insufficient funds");
                var jsonResponse = Json.stringify(response);
                sendResponse(exchange, 400, jsonResponse);
            }
         } catch (NumberFormatException numberFormatException)
         {
             LOGGER.error("Invalid account ID or amount format: {}", numberFormatException.getMessage(), numberFormatException);
             sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
         } catch (Exception withdrawalProcessingException)
         {
             LOGGER.error("Error processing withdrawal: {}", withdrawalProcessingException.getMessage(), withdrawalProcessingException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + withdrawalProcessingException.getMessage() + "\"}");
         }
     }

    private void handleTransfer(HttpExchange exchange) throws IOException
    {
        try
        {
            // Extract account ID from path (source account)
            var parts = exchange.getRequestURI().getPath().split("/");
            if (parts.length < 3)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid path\"}");
                return;
            }

            var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            LOGGER.debug("GOT requestBody: {}", requestBody);
            var jsonNode = Json.parse(requestBody);

            var fromAccountId = Long.parseLong(parts[2]);
            var toAccountId = jsonNode.get("toAccountId").asLong();
            var amount = new BigDecimal(jsonNode.get("amount").asText());

            if (amount.compareTo(BigDecimal.ZERO) < 0)
            {
                sendResponse(exchange, 400, "{\"error\": \"Bad Request: Negative Transfer Amount is Not Allowed\"}");
                return;
            }

            var fromAccount = accountService.getAccountById(fromAccountId);
            if (fromAccount == null)
            {
                sendResponse(exchange, 404, "{\"error\": \"Source account not found\"}");
                return;
            }

            // Verify that the destination account exists
            var toAccount = accountService.getAccountById(toAccountId);
            if (toAccount == null)
            {
                sendResponse(exchange, 400, "{\"error\": \"Destination account not found\"}");
                return;
            }

            // Perform the atomic transfer
            var success = accountService.transferAmount(fromAccountId, toAccountId, amount);

            var response = Json.defaultObjectMapper().createObjectNode();
            response.put("success", success);

            if (success)
            {
                var updatedFromAccount = accountService.getAccountById(fromAccountId);
                response.put("message", "Transfer successful");
                response.put("balance", updatedFromAccount.getBalance().toString());
                var jsonResponse = Json.stringify(response);
                sendResponse(exchange, 200, jsonResponse);
            } else
            {
                response.put("message", "Insufficient funds or account not found");
                response.put("balance", fromAccount.getBalance().toString());
                var jsonResponse = Json.stringify(response);
                sendResponse(exchange, 400, jsonResponse);
            }
         } catch (NumberFormatException numberFormatException)
         {
             LOGGER.error("Invalid account ID or amount format: {}", numberFormatException.getMessage(), numberFormatException);
             sendResponse(exchange, 400, "{\"error\": \"Bad Request: Invalid account ID or amount format\"}");
         } catch (Exception transferProcessingException)
         {
             LOGGER.error("Error processing transfer: {}", transferProcessingException.getMessage(), transferProcessingException);
             sendResponse(exchange, 500, "{\"error\": \"Internal Server Error: " + transferProcessingException.getMessage() + "\"}");
         }
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
