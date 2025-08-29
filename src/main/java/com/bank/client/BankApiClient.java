package com.bank.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.bank.business.entities.User;
import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A simple HTTP client to communicate with the Bank server API.
 * This client handles basic operations like user registration and login.
 * It also manages authentication tokens for subsequent requests.
 */
public class BankApiClient
{

    private final String baseUrl;
    private final HttpClient httpClient;
    private User LoggedInUser;

    public User getLoggedInUser()
    {
        return LoggedInUser;
    }

    public void setLoggedInUser(User loggedInUser)
    {
        LoggedInUser = loggedInUser;
    }

    public BankApiClient(String baseUrl)
    {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();

    }

    /**
     * Logs in a user with username and password.
     * Sends a JSON request to the server for authentication.
     *
     * @param username The username.
     * @param password The plain text password.
     * @return CompletableFuture<HttpResponse<String>> representing the asynchronous
     *         response.
     *         The response body contains user details on successful login.
     */
    public CompletableFuture<HttpResponse<String>> login(String username, String password)
    {
        var url = baseUrl + "/login";
        try
        {
            // Create JSON request body for login
            var loginRequest = Json.defaultObjectMapper().createObjectNode();
            loginRequest.put("username", username);
            loginRequest.put("password", password);
            var jsonBody = Json.stringify(loginRequest);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception)
        {
            var failedFuture = new CompletableFuture<HttpResponse<String>>();
            failedFuture.completeExceptionally(exception);
            return failedFuture;
        }
    }

    /**
     * Encodes a map of form data into a URL-encoded string.
     *
     * @param data The map of form data.
     * @return The URL-encoded string.
     */
    private String encodeFormData(Map<String, String> data)
    {
        var encodedData = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet())
        {
            if (!encodedData.isEmpty())
            {
                encodedData.append("&");
            }
            encodedData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            encodedData.append("=");
            encodedData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return encodedData.toString();
    }

    /**
     * Sends a GET request to the specified endpoint.
     * If an auth token is available, it's included in the Authorization header.
     *
     * @param endpoint The API endpoint (e.g., "/users/123").
     * @return CompletableFuture<HttpResponse<String>> representing the asynchronous
     *         response.
     */
    public CompletableFuture<HttpResponse<String>> get(String endpoint)
    /**/ {
        var url = baseUrl + endpoint;
        var requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();

        var request = requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Registers a new user with the provided details.
     *
     * @param username The desired username.
     * @param email    The user's email address.
     * @param password The plain text password.
     * @return CompletableFuture<HttpResponse<String>> representing the asynchronous
     *         response.
     */
    public CompletableFuture<HttpResponse<String>> register(String username, String email, String password)
    {
        var url = baseUrl + "/users";
        try
        {
            // Create JSON request body for registration
            var registerRequest = Json.defaultObjectMapper().createObjectNode();
            registerRequest.put("username", username);
            registerRequest.put("email", email);
            registerRequest.put("password", password);
            var jsonBody = Json.stringify(registerRequest);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception exception)
        {
            var failedFuture = new CompletableFuture<HttpResponse<String>>();
            failedFuture.completeExceptionally(exception);
            return failedFuture;
        }
    }

    /**
     * Sends a POST request to the specified endpoint with a JSON body.
     * If an auth token is available, it's included in the Authorization header.
     *
     * @param endpoint The API endpoint (e.g., "/accounts").
     * @param body     The request body as a JsonNode.
     * @return CompletableFuture<HttpResponse<String>> representing the asynchronous
     *         response.
     */
    public CompletableFuture<HttpResponse<String>> post(String endpoint, JsonNode body)
    {
        var url = baseUrl + endpoint;
        var requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json");

        try
        {
            var jsonBody = Json.stringify(body);
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } catch (Exception exception)
        {
            var failedFuture = new CompletableFuture<HttpResponse<String>>();
            failedFuture.completeExceptionally(exception);
            return failedFuture;
        }

        var request = requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
