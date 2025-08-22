package com.bank;

import com.bank.business.entities.User;
import com.bank.business.entities.dto.UserCreationRequest;
import com.bank.clientInterface.BankApiClient;
import com.bank.clientInterface.util.CliObjectMapper;
import com.bank.server.config.ConfigurationManager;
import com.bank.server.config.HttpConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

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
            if (loggedInUser == null) {
                loggedInUser = loginOrRegister(client, mapper, scanner);
            } else {
                wantToExit = launchInterface(client, mapper, scanner, loggedInUser);
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
            case 1:
                return performLogin(client, scanner);
            case 2:
                return performRegistration(client, mapper, scanner);
            case 3:
                System.out.println("Exiting...");
                System.exit(0);
                return null;
            default:
                System.out.println("Invalid option. Please try again.");
                return null;
        }
    }

    private static User performLogin(BankApiClient client, Scanner scanner) {
        System.out.print("Enter username or email: ");
        String identifier = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        System.out.println("Attempting to log in...");
        try {
            CompletableFuture<HttpResponse<String>> futureResponse = client.login(identifier, password);
            HttpResponse<String> response = futureResponse.join();

            System.out.println("Login Response Status: " + response.statusCode());
            // In a real implementation, you would extract the token from the response
            // and set it in the client: client.setAuthToken(extractedToken);
            // You would also retrieve user details from the response

            if (response.statusCode() == 200) {
                System.out.println("Login successful!");
                // Return a mock user for now
                // In a real implementation, you would parse the user details from the response
                User user = new User();
                user.setUsername(identifier);
                // Set other user properties as needed
                return user;
            } else {
                System.out.println("Login failed. Server responded with status: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            return null;
        }
    }

    private static User performRegistration(BankApiClient client, CliObjectMapper mapper, Scanner scanner) {
        System.out.println("Registering a new user:");
        try {
            UserCreationRequest userRequest = mapper.readValue(UserCreationRequest.class);
            System.out.println("Attempting to register user: " + userRequest.getUsername());

            CompletableFuture<HttpResponse<String>> futureResponse = client.registerUser(userRequest);
            HttpResponse<String> response = futureResponse.join();

            System.out.println("Registration Response Status: " + response.statusCode());
            System.out.println("Registration Response Body: " + response.body());

            if (response.statusCode() == 201) { // Assuming 201 Created for successful registration
                System.out.println("User registered successfully!");
                // Return a mock user for now
                // In a real implementation, you would parse the created user from the response
                User user = new User();
                user.setUsername(userRequest.getUsername());
                // Set other user properties as needed
                return user;
            } else {
                System.out.println("Failed to register user. Server responded with status: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error during user registration: " + e.getMessage());
            return null;
        }
    }

    private static boolean launchInterface(BankApiClient client, CliObjectMapper mapper, Scanner scanner, User user) {
        System.out.println("\n--- Main Menu ---");
        System.out.println("Welcome, " + user.getUsername() + "!");
        System.out.println("1. View Accounts");
        System.out.println("2. Create Account");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer");
        if (user.isAdmin()) {
            System.out.println("6. Admin: View All Users");
        }
        System.out.println("0. Logout");
        System.out.print("Choose an option: ");

        int choice = -1;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return false; // Don't exit
        }

        switch (choice) {
            case 1:
                viewAccounts(client, user);
                break;
            case 2:
                createAccount(client, mapper, scanner, user);
                break;
            case 3:
                deposit(client, mapper, scanner, user);
                break;
            case 4:
                withdraw(client, mapper, scanner, user);
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
            case 0:
                System.out.println("Logging out...");
                return true; // Exit to login screen
            default:
                System.out.println("Invalid option. Please try again.");
        }
        return false; // Don't exit
    }

    private static void viewAccounts(BankApiClient client, User user) {
        System.out.println("Viewing accounts for user: " + user.getUsername());
        // Implementation would involve calling client.get("/accounts?userId=" +
        // user.getId())
        // and parsing the response
        System.out.println("This feature is not yet implemented.");
    }

    private static void createAccount(BankApiClient client, CliObjectMapper mapper, Scanner scanner, User user) {
        System.out.println("Creating a new account for user: " + user.getUsername());
        // Implementation would involve creating an AccountCreationRequest,
        // using mapper.readValue(AccountCreationRequest.class),
        // and calling client.post("/accounts", accountRequestJson)
        System.out.println("This feature is not yet implemented.");
    }

    private static void deposit(BankApiClient client, CliObjectMapper mapper, Scanner scanner, User user) {
        System.out.println("Making a deposit...");
        // Implementation would involve getting account ID and amount from user,
        // and calling client.post("/accounts/{id}/deposit", depositRequestJson)
        System.out.println("This feature is not yet implemented.");
    }

    private static void withdraw(BankApiClient client, CliObjectMapper mapper, Scanner scanner, User user) {
        System.out.println("Making a withdrawal...");
        // Implementation would involve getting account ID and amount from user,
        // and calling client.post("/accounts/{id}/withdraw", withdrawRequestJson)
        System.out.println("This feature is not yet implemented.");
    }

    private static void transfer(BankApiClient client, CliObjectMapper mapper, Scanner scanner, User user) {
        System.out.println("Making a transfer...");
        // Implementation would involve getting source account ID, destination account
        // ID,
        // and amount from user, and calling client.post("/accounts/{id}/transfer",
        // transferRequestJson)
        System.out.println("This feature is not yet implemented.");
    }

    private static void viewAllUsers(BankApiClient client) {
        System.out.println("Viewing all users (Admin only)...");
        // Implementation would involve calling client.get("/admin/users")
        // and parsing the response
        System.out.println("This feature is not yet implemented.");
    }
}
