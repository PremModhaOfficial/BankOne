package com.bank.stress;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Network-based stress test for the banking system that communicates with the
 * HTTP server instead of directly accessing services.
 */
public class NetworkStressTest
{

    private static final String SERVER_URL = "http://localhost:8080";
    private static final long NUMBER_OF_USERS = 500; // Reduced for network testing
    private static final int NUMBER_OF_ACCOUNTS_PER_USER = 5; // Accounts per user
    private static final int NUMBER_OF_THREADS = 16; // Concurrent threads
    private static final long OPERATIONS_PER_THREAD = 1000; // Operations per thread

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private List<Long> userIds;
    private List<Long> accountIds;
    private ExecutorService executorService;

    private AtomicLong totalOperations = new AtomicLong(0);
    private AtomicInteger successfulOperations = new AtomicInteger(0);
    private AtomicInteger failedOperations = new AtomicInteger(0);

    public static void main(String[] args) throws Exception
    {
        NetworkStressTest stressTest = new NetworkStressTest();
        stressTest.runStressTest();
    }

    public void runStressTest() throws Exception
    {
        System.out.println("Starting network-based stress test...");
        System.out.println("Server URL: " + SERVER_URL);

        // Initialize the system
        initialize();

        try
        {
            // Create users
            createUsers();

            // Create accounts for users
            createAccounts();

            // Start the stress test
            long startTime = System.currentTimeMillis();
            executeConcurrentOperations();
            long endTime = System.currentTimeMillis();

            // Print results
            printResults(startTime, endTime);
        } finally
        {
            // Cleanup
            cleanup();
        }
    }

    private void initialize()
    {
        System.out.println("Initializing HTTP client...");

        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        objectMapper = new ObjectMapper();
        userIds = new ArrayList<>();
        accountIds = new ArrayList<>();

        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        // executorService = Executors.newVirtualThreadPerTaskExecutor();

        System.out.println("HTTP client initialized");
    }

    private void createUsers() throws Exception
    {
        System.out.println("Creating users...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (long i = 0; i < NUMBER_OF_USERS; i++)
        {
            final long userIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try
                {
                    String userJson = String.format(
                            "{\"username\":\"stress_user_%d\",\"email\":\"stress%d@example.com\"}", userIndex, userIndex);

                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/users")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(userJson)).build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 201)
                    {
                        // Parse the user ID from the response
                        JsonNode jsonNode = objectMapper.readTree(response.body());
                        long userId = jsonNode.get("id").asLong();
                        synchronized (userIds)
                        {
                            userIds.add(userId);
                        }
                        System.out.println("Created user " + userId);
                    } else
                    {
                        System.err.println("Failed to create user, status: " + response.statusCode() + ", response: " + response.body());
                    }
                } catch (Exception e)
                {
                    System.err.println("Error creating user " + userIndex + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }, executorService);

            futures.add(future);
        }

        // Wait for all users to be created
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("Created " + userIds.size() + " users");
    }

    private void createAccounts() throws Exception
    {
        System.out.println("Creating accounts...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Long userId : userIds)
        {
            for (int j = 0; j < NUMBER_OF_ACCOUNTS_PER_USER; j++)
            {
                final Long finalUserId = userId;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try
                    {
                        String accountJson = String.format(
                                "{\"userId\":%d,\"balance\":1000.0,\"type\":\"SAVINGS\"}", finalUserId);

                        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(accountJson)).build();

                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 201)
                        {
                            // Parse the account ID from the response
                            JsonNode jsonNode = objectMapper.readTree(response.body());
                            long accountId = jsonNode.get("id").asLong();
                            synchronized (accountIds)
                            {
                                accountIds.add(accountId);
                            }
                            System.out.println("Created account " + accountId + " for user " + finalUserId);
                        } else
                        {
                            System.err.println("Failed to create account for user " + finalUserId + ", status: " + response.statusCode() + ", response: " + response.body());
                        }
                    } catch (Exception e)
                    {
                        System.err.println("Error creating account for user " + finalUserId + ": " + e.getMessage());
                    }
                }, executorService);

                futures.add(future);
            }
        }

        // Wait for all accounts to be created
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("Created " + accountIds.size() + " accounts");
    }

    private void executeConcurrentOperations() throws InterruptedException
    {
        System.out.println("Executing concurrent operations...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Submit tasks to the executor service
        for (int i = 0; i < NUMBER_OF_THREADS; i++)
        {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                performOperations(threadId);
            }, executorService);
            futures.add(future);
        }

        // Wait for all operations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void performOperations(int threadId)
    {
        System.out.println("Thread " + threadId + " started");

        for (int i = 0; i < OPERATIONS_PER_THREAD; i++)
        {
            totalOperations.incrementAndGet();

            try
            {
                // Randomly select an operation
                int operationType = (int) (Math.random() * 3);

                boolean success = false;
                switch (operationType)
                {
                    case 0:
                        success = performDeposit();
                        break;
                    case 1:
                        success = performWithdrawal();
                        break;
                    case 2:
                        success = performTransfer();
                        break;
                }

                if (success)
                {
                    successfulOperations.incrementAndGet();
                } else
                {
                    failedOperations.incrementAndGet();
                }
            } catch (Exception e)
            {
                System.err.println("Error in thread " + threadId + " operation " + i + ": " + e.getMessage());
                failedOperations.incrementAndGet();
            }
        }

        System.out.println("Thread " + threadId + " completed");
    }

    private boolean performDeposit()
    {
        try
        {
            // Select a random account
            if (accountIds.isEmpty())
                return false;
            int accountIndex = (int) (Math.random() * accountIds.size());
            long accountId = accountIds.get(accountIndex);

            // Generate a random deposit amount
            BigDecimal amount = new BigDecimal(Math.random() * 100);

            String depositJson = String.format("{\"amount\":%f}", amount.doubleValue());

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts/" + accountId + "/deposit")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(depositJson)).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e)
        {
            System.err.println("Error performing deposit: " + e.getMessage());
            return false;
        }
    }

    private boolean performWithdrawal()
    {
        try
        {
            // Select a random account
            if (accountIds.isEmpty())
                return false;
            int accountIndex = (int) (Math.random() * accountIds.size());
            long accountId = accountIds.get(accountIndex);

            // Generate a random withdrawal amount
            BigDecimal amount = new BigDecimal(Math.random() * 50);

            String withdrawJson = String.format("{\"amount\":%f}", amount.doubleValue());

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts/" + accountId + "/withdraw")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(withdrawJson)).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e)
        {
            System.err.println("Error performing withdrawal: " + e.getMessage());
            return false;
        }
    }

    private boolean performTransfer()
    {
        try
        {
            // Select two different random accounts
            if (accountIds.size() < 2)
                return false;
            int fromAccountIndex = (int) (Math.random() * accountIds.size());
            int toAccountIndex;
            do
            {
                toAccountIndex = (int) (Math.random() * accountIds.size());
            } while (toAccountIndex == fromAccountIndex);

            long fromAccountId = accountIds.get(fromAccountIndex);
            long toAccountId = accountIds.get(toAccountIndex);

            // Generate a random transfer amount
            BigDecimal amount = new BigDecimal(Math.random() * 25);

            String transferJson = String.format("{\"toAccountId\":%d,\"amount\":%f}", toAccountId, amount.doubleValue());

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts/" + fromAccountId + "/transfer")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(transferJson)).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e)
        {
            System.err.println("Error performing transfer: " + e.getMessage());
            return false;
        }
    }

    private void printResults(long startTime, long endTime)
    {
        long duration = endTime - startTime;
        long totalOps = totalOperations.get();
        int successOps = successfulOperations.get();
        int failedOps = failedOperations.get();
        double throughput = totalOps > 0 ? (double) totalOps / (duration / 1000.0) : 0;
        double successRate = totalOps > 0 ? (double) successOps / totalOps * 100 : 0;

        System.out.println("\n=== NETWORK STRESS TEST RESULTS ===");
        System.out.println("Duration: " + duration + " ms");
        System.out.println("Total operations: " + totalOps);
        System.out.println("Successful operations: " + successOps);
        System.out.println("Failed operations: " + failedOps);
        System.out.println("Success rate: " + String.format("%.2f", successRate) + "%");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " ops/sec");
        System.out.println("Average response time: " + String.format("%.2f", totalOps > 0 ? (double) duration / totalOps : 0) + " ms");
    }

    private void cleanup() throws InterruptedException
    {
        System.out.println("Cleaning up...");

        if (executorService != null)
        {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
            {
                executorService.shutdownNow();
            }
        }

        System.out.println("Network stress test completed");
    }
}
