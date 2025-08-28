package com.bank.client;

import com.bank.business.entities.User;
import com.bank.server.util.Json;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
     * Logs in a user with username/email and email.
     * This is a placeholder implementation. You'll need to implement the actual
     * login logic on the server side and return a token.
     *
     * @param identifier The username or email.
     * @param email      The plain text email.
     * @return CompletableFuture<HttpResponse<String>> representing the asynchronous
     *         response.
     *         The response body should contain the authentication token.
     */
    public CompletableFuture<HttpResponse<String>> login(String identifier, String email)
    {
        String url = baseUrl + "/login";
        try
        {
            // Create a simple form-encoded body for login
            Map<String, String> formData = new HashMap<>();
            formData.put("identifier", identifier); // Could be username or email
            formData.put("email", email);

            String formDataString = encodeFormData(formData);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(formDataString)).build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e)
        {
            CompletableFuture<HttpResponse<String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
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
        StringBuilder encodedData = new StringBuilder();
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
        String url = baseUrl + endpoint;
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).GET();

        HttpRequest request = requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
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
        String url = baseUrl + endpoint;
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json");

        try
        {
            String jsonBody = Json.stringify(body);
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } catch (Exception e)
        {
            CompletableFuture<HttpResponse<String>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }

        HttpRequest request = requestBuilder.build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
