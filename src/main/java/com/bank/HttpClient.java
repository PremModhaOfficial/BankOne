package com.bank;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.business.entities.Account;
import com.bank.business.entities.User;
import com.bank.business.entities.dto.UserCreationRequest;
import com.bank.clientInterface.BankApiClient;
import com.bank.clientInterface.util.CliObjectMapper;
import com.bank.clientInterface.util.ResponseFormatter;
import com.bank.server.config.ConfigurationManager;
import com.bank.server.config.HttpConfigurationException;
import com.bank.server.dto.LoginRequest;
import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;

public class HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    public static void main(String[] args) {
        // Load configuration
        String serverUrl = "http://localhost:8080";
        try {
            ConfigurationManager.getInstance().loadConfiguration("src/main/resources/http.json");
            int port = ConfigurationManager.getInstance().getCurrentConfiguration().getPort();
            serverUrl = "http://localhost:" + port;
        } catch (HttpConfigurationException e) {
            LOGGER.warn("Failed to load configuration, using default URL: {}", serverUrl, e);
        }

        // Initialize client
        BankApiClient client = new BankApiClient(serverUrl);
        Scanner scanner = new Scanner(System.in);
        CliObjectMapper mapper = new CliObjectMapper(scanner);

        LOGGER.info("Connected to server at: {}", serverUrl);

        // Main application loop
        boolean wantToExit = false;
        User loggedInUser = null;

        while (!wantToExit) {
            try {

                if (client.getLoggedInUser() == null || loggedInUser == null) {
                    loggedInUser = loginOrRegister(client, mapper, scanner);
                    client.setLoggedInUser(loggedInUser);
                } else {
                    wantToExit = launchInterface(client, mapper, scanner);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Please enter a number between 1 and 100");

            }
        }

        LOGGER.info("Thank you for using Bank Client. Goodbye!");
        scanner.close();
    }

    private static User loginOrRegister(BankApiClient client, CliObjectMapper mapper, Scanner scanner) {
        System.out.println("\n--- Bank Client ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }

        switch (choice) {
            case 1 -> {
                return performLogin(client, scanner);
            }
            case 2 -> {
                return performRegistration(client, mapper, scanner);
            }

            case 3 -> {
                System.out.println("Exiting...");
                System.exit(0);
                return null;
            }
            default -> System.out.println("Invalid option. Please try again.");
        }
        return null;
    }

    private static User performLogin(BankApiClient client, Scanner scanner) {
        System.out.print("Enter username or email: ");
        String identifier = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        System.out.println("Attempting to log in...");
        try {
            // Create login request
            LoginRequest loginRequest = new LoginRequest(identifier, password);
            JsonNode jsonBody = Json.toJson(loginRequest);

            CompletableFuture<HttpResponse<String>> futureResponse = client.post("/login", jsonBody);
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logAndDisplayResponse(LOGGER, "Login", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                // Parse the response to extract token and user details
                JsonNode responseBody = Json.parse(response.body());
                JsonNode userNode = responseBody.get("user");
                String username = userNode.get("username").asText();
                long id = userNode.get("id").asLong();
                String email = userNode.get("email").asText("");
                boolean isAdmin = userNode.get("admin").asBoolean();

                LOGGER.debug("Perform Login: email:{} username:{} id:{} Admin:{}", email, username, id, isAdmin);

                // Set the token in the client for future requests

                System.out.println("Login successful!");
                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
                User user = new User(username, email, id, isAdmin);
                // In a real implementation, you would parse all user details from the response
                client.setLoggedInUser(user);
                return user;
            } else {
                System.out.println("Login failed. Server responded with status: " + response.statusCode());
                System.out.println("Response body: " + Json.stringifyPretty(Json.toJson(response.body())));
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static User performRegistration(BankApiClient client, CliObjectMapper mapper, Scanner scanner) {
        System.out.println("Registering a new user:");
        try {
            UserCreationRequest userRequest = mapper.readValue(UserCreationRequest.class);
            System.out.println("Attempting to register user: " + userRequest.getUsername());
            JsonNode jsonBody = Json.toJson(userRequest);
            CompletableFuture<HttpResponse<String>> futureResponse = client.post("/users", jsonBody);
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logAndDisplayResponse(LOGGER, "Registration", response.statusCode(), response.body());

            if (response.statusCode() == 201) {
                System.out.println("User registered successfully!");
                // Parse the created user from the response
                JsonNode userNode = Json.parse(response.body());
                String username = userNode.get("username").asText();
                long id = userNode.get("id").asLong();
                String email = userNode.get("email").asText();
                boolean isAdmin = userNode.get("admin").asBoolean();

                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));

                User user = new User(username, email, id, isAdmin);

                client.setLoggedInUser(user);
                return user;
            } else {
                System.out.println("Failed to register user. Server responded with status: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static boolean launchInterface(BankApiClient client, CliObjectMapper mapper, Scanner scanner) {
        User user = client.getLoggedInUser();
        System.out.println("\n--- Main Menu ---");
        System.out.println("Welcome, " + user.getUsername() + "!");
        System.out.println("1. View Accounts");
        System.out.println("2. Create Account");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer");
        if (user.isAdmin()) {
            System.out.println("6. Admin: View All Users");
            System.out.println("7. Admin: View All Accounts");
        }
        System.out.println("0. Logout");
        System.out.println("8. Switch User");
        System.out.print("Choose an option: ");

        LOGGER.debug("USER {}", user);

        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return false;
        }

        switch (choice) {
            case 1:
                viewAccounts(client);
                break;
            case 2:
                createAccount(client, mapper, scanner);
                break;
            case 3:
                deposit(client, mapper, scanner);
                break;
            case 4:
                withdraw(client, mapper, scanner);
                break;
            case 5:
                transfer(client, mapper, scanner, user);
                break;
            case 6:
                if (user.isAdmin()) {
                    viewAllUsers(client);
                } else {
                    System.out.println("Invalid option.");
                }
                break;
            case 7:
                if (user.isAdmin()) {
                    viewAllAccounts(client);
                }
                break;
            case 0:
                System.out.println("Logging out...");
                return true;
            case 8:
                System.out.println("Switch Users...");
                client.setLoggedInUser(null);
                return false;
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return false;
    }

    private static void viewAllAccounts(BankApiClient client) {
        User user = client.getLoggedInUser();
        System.out.println("Viewing all accounts by admin: " + user.getUsername());
        try {
            // Include userId in query parameter for GET request
            CompletableFuture<HttpResponse<String>> futureResponse = client.get("/accounts-all");
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logResponse(LOGGER, "Accounts", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                System.out.println("All Accounts:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else {
                System.out
                        .println("Failed to retrieve accounts. Server responded with status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error retrieving accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewAccounts(BankApiClient client) {
        User user = client.getLoggedInUser();
        System.out.println("Viewing accounts for user: " + user.getUsername());
        try {
            // Include userId in query parameter for GET request
            CompletableFuture<HttpResponse<String>> futureResponse = client.get("/accounts?userId=" + user.getId());
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logResponse(LOGGER, "Accounts", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                System.out.println("Accounts:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else {
                System.out
                        .println("Failed to retrieve accounts. Server responded with status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error retrieving accounts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createAccount(BankApiClient client, CliObjectMapper mapper, Scanner scanner) {
        var user = client.getLoggedInUser();
        System.out.println("Creating a new account for user: " + user.getUsername());
        boolean created = false;
        while (!created) {
            try {
                // Get account details from user
                System.out.print("Enter initial balance: ");
                String initialBalanceString = scanner.nextLine().trim();
                BigDecimal initialBalance = new BigDecimal(initialBalanceString == "" ? "0" : initialBalanceString);

                Account.AccountType type = getAccountType(scanner);

                // Create a proper request without account number (it will be auto-generated)
                com.bank.server.dto.CreateAccountRequest request = new com.bank.server.dto.CreateAccountRequest(
                        user.getId(),
                        initialBalance,
                        type);

                JsonNode jsonBody = Json.toJson(request);
                CompletableFuture<HttpResponse<String>> futureResponse = client.post("/accounts", jsonBody);
                HttpResponse<String> response = futureResponse.join();

                ResponseFormatter.logAndDisplayResponse(LOGGER, "Create Account", response.statusCode(),
                        response.body());
                if (response.statusCode() == 201) {
                    created = true;
                    System.out.println("Account created successfully!");
                    System.out.println("Response:");
                    System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
                } else {
                    System.out.println(
                            "Failed to create account. Server responded with status: " + response.statusCode());
                    System.out.println("Response Body: " + response.body());
                }
            } catch (Exception e) {
                System.out.println("Please try again. with valid input");
                // System.err.println("Error creating account: " + e.getMessage());
                // e.printStackTrace();
            }
        }
    }

    private static Account.AccountType getAccountType(Scanner scanner) {
        System.out.println("Enter account type \n[1] SAVINGS \n[2] CHECKING");

        String nextLine = scanner.nextLine();
        if (nextLine.equals("1")) {
            return Account.AccountType.SAVINGS;
        } else if (nextLine.equals("2")) {
            return Account.AccountType.CHECKING;
        }
        String typeStr = scanner.nextLine().trim().toUpperCase();
        Account.AccountType type = Account.AccountType.valueOf(typeStr);
        return type;
    }

    private static void deposit(BankApiClient client, CliObjectMapper mapper, Scanner scanner) {
        System.out.println("Making a deposit...");
        try {
            System.out.print("Enter account ID: ");
            Long accountId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter amount to deposit: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            com.bank.server.dto.TransactionRequest transactionRequest = new com.bank.server.dto.TransactionRequest(
                    accountId, amount);
            JsonNode jsonBody = Json.toJson(transactionRequest);

            CompletableFuture<HttpResponse<String>> futureResponse = client.post("/accounts/" + accountId + "/deposit",
                    jsonBody);
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logAndDisplayResponse(LOGGER, "Deposit", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                System.out.println("Deposit successful!");
                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else {
                System.out.println("Failed to make deposit. Server responded with status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        } catch (Exception e) {
            System.err.println("Error making deposit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void withdraw(BankApiClient client, CliObjectMapper mapper, Scanner scanner) {
        System.out.println("Making a withdrawal...");
        try {
            System.out.print("Enter account ID: ");
            viewAccounts(client);
            Long accountId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter amount to withdraw: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            com.bank.server.dto.TransactionRequest transactionRequest = new com.bank.server.dto.TransactionRequest(
                    accountId, amount);
            JsonNode jsonBody = Json.toJson(transactionRequest);

            CompletableFuture<HttpResponse<String>> futureResponse = client.post("/accounts/" + accountId + "/withdraw",
                    jsonBody);
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logAndDisplayResponse(LOGGER, "Withdrawal", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                System.out.println("Withdrawal successful!");
                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else if (response.statusCode() == 400) {
                System.out.println("Withdrawal failed: Insufficient funds");
                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else {
                System.out.println("Failed to make withdrawal. Server responded with status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        } catch (Exception e) {
            System.err.println("Error making withdrawal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void transfer(BankApiClient client, CliObjectMapper mapper, Scanner scanner, User user) {
        System.out.println("Making a transfer...");
        try {
            System.out.print("Enter source account ID: ");
            Long fromAccountId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter destination account ID: ");
            Long toAccountId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter amount to transfer: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());

            com.bank.server.dto.TransferRequest transferRequest = new com.bank.server.dto.TransferRequest(fromAccountId,
                    toAccountId, amount);
            JsonNode jsonBody = Json.toJson(transferRequest);

            CompletableFuture<HttpResponse<String>> futureResponse = client
                    .post("/accounts/" + fromAccountId + "/transfer", jsonBody);
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logAndDisplayResponse(LOGGER, "Transfer", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                System.out.println("Transfer successful!");
                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else if (response.statusCode() == 400) {
                System.out.println("Transfer failed: Insufficient funds");
                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else {
                System.out.println("Failed to make transfer. Server responded with status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        } catch (Exception e) {
            System.err.println("Error making transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void viewAllUsers(BankApiClient client) {
        System.out.println("Viewing all users (Admin only)...");
        try {
            CompletableFuture<HttpResponse<String>> futureResponse = client.get("/admin/users");
            HttpResponse<String> response = futureResponse.join();

            ResponseFormatter.logAndDisplayResponse(LOGGER, "View All Users", response.statusCode(), response.body());

            if (response.statusCode() == 200) {
                System.out.println("Users retrieved successfully!");
                System.out.println("Response:");
                System.out.println(ResponseFormatter.formatJsonResponse(response.body()));
            } else {
                System.out.println("Failed to retrieve users. Server responded with status: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Error retrieving users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
