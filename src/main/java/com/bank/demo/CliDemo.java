package com.bank.demo;

import com.bank.business.entities.dto.UserCreationRequest;
import com.bank.clientInterface.BankApiClient;
import com.bank.clientInterface.util.CliObjectMapper;

import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * A simple demonstration class for the CliObjectMapper utility and
 * BankApiClient.
 * This class shows how to use CliObjectMapper to read User and Account objects
 * from the command line,
 * and how to use BankApiClient to send them to the server.
 */
public class CliDemo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CliObjectMapper mapper = new CliObjectMapper(scanner);

        System.out.println("=== Bank Client CLI Demo ===");

        // 1. Get server details
        System.out.print("Enter server URL (e.g., http://localhost:8080): ");
        String serverUrl = scanner.nextLine().trim();
        if (serverUrl.isEmpty()) {
            serverUrl = "http://localhost:8080"; // Default
        }

        BankApiClient client = new BankApiClient(serverUrl);

        // 2. Demo: Register a new user using UserCreationRequest DTO
        System.out.println("\n--- Demo: Registering a new User ---");
        try {
            UserCreationRequest userRequest = mapper.readValue(UserCreationRequest.class);
            System.out.println("Attempting to register user: " + userRequest.getUsername());

            CompletableFuture<HttpResponse<String>> futureResponse = client.registerUser(userRequest);
            HttpResponse<String> response = futureResponse.join(); // Blocking call for simplicity in demo

            System.out.println("Registration Response Status: " + response.statusCode());
            System.out.println("Registration Response Body: " + response.body());

            if (response.statusCode() == 201) { // Assuming 201 Created for successful registration
                System.out.println("User registered successfully!");
            } else {
                System.out.println("Failed to register user. Server responded with status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
        }

        // 3. Demo: Login (Placeholder)
        System.out.println("\n--- Demo: Logging in a User (Placeholder) ---");
        try {
            System.out.print("Enter username or email for login: ");
            String identifier = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            String password = scanner.nextLine().trim();

            System.out.println("Attempting to log in user: " + identifier);
            // Note: This will likely fail until you implement the login endpoint on the
            // server
            CompletableFuture<HttpResponse<String>> futureResponse = client.login(identifier, password);
            HttpResponse<String> response = futureResponse.join(); // Blocking call for simplicity in demo

            System.out.println("Login Response Status: " + response.statusCode());
            System.out.println("Login Response Body: " + response.body());

            if (response.statusCode() == 200) { // Assuming 200 OK for successful login
                System.out.println("User logged in successfully!");
                // In a real implementation, you would extract the token from the response
                // and set it in the client: client.setAuthToken(extractedToken);
            } else {
                System.out.println("Failed to log in user. Server responded with status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error during user login: " + e.getMessage());
            e.printStackTrace();
        }

        scanner.close();
        System.out.println("\n=== Demo Finished ===");
    }
}
