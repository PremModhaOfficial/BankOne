package com.bank.stress;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Network-based stress test for the banking system that communicates with the
 * HTTP server instead of directly accessing services.
 */
public class NetworkStressTest {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final long NUMBER_OF_USERS = Long.parseLong(System.getProperty("NUMBER_OF_USERS", "5"));
    private static final int NUMBER_OF_ACCOUNTS_PER_USER = Integer.parseInt(System.getProperty("NUMBER_OF_ACCOUNTS_PER_USER", "10"));
    private static final int NUMBER_OF_THREADS = Integer.parseInt(System.getProperty("NUMBER_OF_THREADS", "8"));
    private static final long OPERATIONS_PER_THREAD = Long.parseLong(System.getProperty("OPERATIONS_PER_THREAD", "200000"));

    // HTTP and JSON handling
    private HttpClient httpClient;
    private ObjectMapper objectMapper;

    // Test data
    private List<Long> userIds;
    private List<Long> accountIds;
    private ExecutorService executorService;

    // Metrics collection
    private AtomicLong totalOperations = new AtomicLong(0);
    private AtomicInteger successfulOperations = new AtomicInteger(0);
    private AtomicInteger failedOperations = new AtomicInteger(0);
    private ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<ErrorCategory, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AtomicInteger> operationTypeCounts = new ConcurrentHashMap<>();

    // Progress reporting
    private ScheduledExecutorService progressReporter;
    private Instant testStartTime;
    private TestScenario scenario = TestScenario.BALANCED_LOAD;
    private String outputFormat = "CONSOLE";
    private boolean showProgress = true;

    public static void main(String[] args) throws Exception {
        var stressTest = new NetworkStressTest();

        // Parse command line arguments
        for (var i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--scenario" -> {
                    if (i + 1 < args.length) {
                        try {
                            stressTest.scenario = TestScenario.valueOf(args[i + 1].toUpperCase());
                            i++; // Skip next argument
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid scenario: " + args[i + 1] + ". Using BALANCED_LOAD.");
                            i++; // Skip next argument
                        }
                    }
                }
                case "--output" -> {
                    if (i + 1 < args.length) {
                        stressTest.outputFormat = args[i + 1].toUpperCase();
                        i++; // Skip next argument
                    }
                }
                case "--progress" -> stressTest.showProgress = true;
                case "--no-progress" -> stressTest.showProgress = false;
            }
        }

        stressTest.runStressTest();
    }

    public void runStressTest() throws Exception {
        System.out.println("Starting network-based stress test...");
        System.out.println("Server URL: " + SERVER_URL);
        System.out.println("Scenario: " + scenario.name());

        // Initialize the system
        initialize();

        try {
            // Create users
            createUsers();

            // Create accounts for users
            createAccounts();

            // Start progress reporting if enabled
            if (showProgress) {
                startProgressReporting();
            }

            // Start the stress test
            testStartTime = Instant.now();
            executeConcurrentOperations();
            var endTime = Instant.now();

            // Stop progress reporting if enabled
            if (showProgress) {
                stopProgressReporting();
            }

            // Print results
            printResults(testStartTime, endTime);

            // Export results if requested
            exportResults();

        } finally {
            // Cleanup
            cleanup();
        }
    }

    private void initialize() {
        System.out.println("Initializing HTTP client...");

        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        objectMapper = new ObjectMapper();
        userIds = new ArrayList<>();
        accountIds = new ArrayList<>();

        executorService = Executors.newVirtualThreadPerTaskExecutor();
        progressReporter = Executors.newScheduledThreadPool(1);

        // Initialize error counts for all categories
        for (var category : ErrorCategory.values()) {
            errorCounts.put(category, new AtomicInteger(0));
        }

        System.out.println("HTTP client initialized");
    }

    private void createUsers() throws Exception {
        System.out.println("Creating users...");

        var futures = new ArrayList<CompletableFuture<Void>>();

        for (var i = 0; i < NUMBER_OF_USERS; i++) {
            var userIndex = i;
            var future = CompletableFuture.runAsync(() -> {
                try {
                    var userJson = String.format(
                            "{\"username\":\"stress_user_%d\",\"email\":\"stress%d@example.com\", \"password\": \"stress_pass_%d\"}",
                            userIndex, userIndex, userIndex);

                    var request = HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL + "/users"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(userJson))
                        .build();

                    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 201) {
                        var jsonNode = objectMapper.readTree(response.body());
                        var userId = jsonNode.get("id").asLong();
                        synchronized (userIds) {
                            userIds.add(userId);
                        }
                        System.out.println("Created user " + userId);
                    } else {
                        System.err.println("Failed to create user, status: " + response.statusCode() + ", response: " + response.body());
                    }
                } catch (Exception exception) {
                    System.err.println("Error creating user " + userIndex + ": " + exception.getMessage());
                }
            }, executorService);

            futures.add(future);
        }

        // Wait for all users to be created
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("Created " + userIds.size() + " users");
    }

    private void createAccounts() throws Exception {
        System.out.println("Creating accounts...");

        var futures = new ArrayList<CompletableFuture<Void>>();

        for (var userId : userIds) {
            for (var j = 0; j < NUMBER_OF_ACCOUNTS_PER_USER; j++) {
                var finalUserId = userId;
                var future = CompletableFuture.runAsync(() -> {
                    try {
                        var accountJson = String.format(
                                "{\"userId\":%d,\"balance\":1000.0,\"type\":\"SAVINGS\"}", finalUserId);

                        var request = HttpRequest.newBuilder()
                            .uri(URI.create(SERVER_URL + "/accounts"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(accountJson))
                            .build();

                        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 201) {
                            var jsonNode = objectMapper.readTree(response.body());
                            var accountId = jsonNode.get("id").asLong();
                            synchronized (accountIds) {
                                accountIds.add(accountId);
                            }
                            System.out.println("Created account " + accountId + " for user " + finalUserId);
                        } else {
                            System.err.println("Failed to create account for user " + finalUserId + ", status: " + response.statusCode() + ", response: " + response.body());
                        }
                    } catch (Exception exception) {
                        System.err.println("Error creating account for user " + finalUserId + ": " + exception.getMessage());
                    }
                }, executorService);

                futures.add(future);
            }
        }

        // Wait for all accounts to be created
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("Created " + accountIds.size() + " accounts");
    }

    private void executeConcurrentOperations() throws InterruptedException {
        System.out.println("Executing concurrent operations...");

        var futures = new ArrayList<CompletableFuture<Void>>();

        // Submit tasks to the executor service
        for (var i = 0; i < NUMBER_OF_THREADS; i++) {
            var threadId = i;
            var future = CompletableFuture.runAsync(() -> {
                performOperations(threadId);
            }, executorService);
            futures.add(future);
        }

        // Wait for all operations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void performOperations(int threadId) {
        System.out.println("Thread " + threadId + " started");

        var deposits = 0;
        var withdrawals = 0;
        var transfers = 0;

        for (var i = 0; i < OPERATIONS_PER_THREAD; i++) {
            totalOperations.incrementAndGet();

            try {
                // Select operation based on scenario ratios
                var random = Math.random();
                var success = false;
                String operationType;

                if (random < scenario.depositRatio) {
                    success = performDeposit();
                    deposits++;
                    operationType = "DEPOSIT";
                } else if (random < scenario.depositRatio + scenario.withdrawalRatio) {
                    success = performWithdrawal();
                    withdrawals++;
                    operationType = "WITHDRAWAL";
                } else {
                    success = performTransfer();
                    transfers++;
                    operationType = "TRANSFER";
                }

                // Update operation type counts
                operationTypeCounts.computeIfAbsent(operationType, k -> new AtomicInteger(0)).incrementAndGet();

                if (success) {
                    successfulOperations.incrementAndGet();
                } else {
                    failedOperations.incrementAndGet();
                }
            } catch (Exception exception) {
                System.err.println("Error in thread " + threadId + " operation " + i + ": " + exception.getMessage());
                failedOperations.incrementAndGet();
                errorCounts.get(categorizeError(exception, null)).incrementAndGet();
            }
        }

        System.out.println("Thread " + threadId + " completed - Deposits: " + deposits + ", Withdrawals: " + withdrawals + ", Transfers: " + transfers);
    }

    private boolean performDeposit() {
        var startTime = System.currentTimeMillis();
        try {
            // Select a random account
            if (accountIds.isEmpty())
                return false;
            var accountIndex = (int) (Math.random() * accountIds.size());
            var accountId = accountIds.get(accountIndex);

            // Generate a random deposit amount
            var amount = new BigDecimal(Math.random() * 100);

            var depositJson = String.format("{\"amount\":%f}", amount.doubleValue());

            var request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/accounts/" + accountId + "/deposit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(depositJson))
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var endTime = System.currentTimeMillis();

            // Record response time
            responseTimes.add(endTime - startTime);

            var success = response.statusCode() == 200;
            if (!success) {
                errorCounts.get(categorizeError(null, response)).incrementAndGet();
            }

            return success;
        } catch (Exception exception) {
            var endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
            errorCounts.get(categorizeError(exception, null)).incrementAndGet();
            System.err.println("Error performing deposit: " + exception.getMessage());
            return false;
        }
    }

    private boolean performWithdrawal() {
        var startTime = System.currentTimeMillis();
        try {
            // Select a random account
            if (accountIds.isEmpty())
                return false;
            var accountIndex = (int) (Math.random() * accountIds.size());
            var accountId = accountIds.get(accountIndex);

            // Generate a random withdrawal amount
            var amount = new BigDecimal(Math.random() * 50);

            var withdrawJson = String.format("{\"amount\":%f}", amount.doubleValue());

            var request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/accounts/" + accountId + "/withdraw"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(withdrawJson))
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var endTime = System.currentTimeMillis();

            // Record response time
            responseTimes.add(endTime - startTime);

            var success = response.statusCode() == 200;
            if (!success) {
                errorCounts.get(categorizeError(null, response)).incrementAndGet();
            }

            return success;
        } catch (Exception exception) {
            var endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
            errorCounts.get(categorizeError(exception, null)).incrementAndGet();
            System.err.println("Error performing withdrawal: " + exception.getMessage());
            return false;
        }
    }

    private boolean performTransfer() {
        var startTime = System.currentTimeMillis();
        try {
            // Select two different random accounts
            if (accountIds.size() < 2)
                return false;
            var fromAccountIndex = (int) (Math.random() * accountIds.size());
            var toAccountIndex = fromAccountIndex;
            while (toAccountIndex == fromAccountIndex) {
                toAccountIndex = (int) (Math.random() * accountIds.size());
            }

            var fromAccountId = accountIds.get(fromAccountIndex);
            var toAccountId = accountIds.get(toAccountIndex);

            // Generate a random transfer amount
            var amount = new BigDecimal(Math.random() * 25);

            var transferJson = String.format("{\"toAccountId\":%d,\"amount\":%f}", toAccountId, amount.doubleValue());

            var request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/accounts/" + fromAccountId + "/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(transferJson))
                .build();

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var endTime = System.currentTimeMillis();

            // Record response time
            responseTimes.add(endTime - startTime);

            var success = response.statusCode() == 200;
            if (!success) {
                errorCounts.get(categorizeError(null, response)).incrementAndGet();
            }

            return success;
        } catch (Exception exception) {
            var endTime = System.currentTimeMillis();
            responseTimes.add(endTime - startTime);
            errorCounts.get(categorizeError(exception, null)).incrementAndGet();
            System.err.println("Error performing transfer: " + exception.getMessage());
            return false;
        }
    }

    private void startProgressReporting() {
        progressReporter.scheduleAtFixedRate(this::reportProgress, 0, 5, TimeUnit.SECONDS);
    }

    private void stopProgressReporting() {
        if (progressReporter != null) {
            progressReporter.shutdown();
            try {
                if (!progressReporter.awaitTermination(5, TimeUnit.SECONDS)) {
                    progressReporter.shutdownNow();
                }
            } catch (InterruptedException e) {
                progressReporter.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void reportProgress() {
        var currentOps = totalOperations.get();
        var elapsedMs = Duration.between(testStartTime, Instant.now()).toMillis();
        var currentThroughput = elapsedMs > 0 ? (double) currentOps / (elapsedMs / 1000.0) : 0;
        var successRate = currentOps > 0 ? (double) successfulOperations.get() / currentOps * 100 : 0;
        var progress = (double) currentOps / (NUMBER_OF_THREADS * OPERATIONS_PER_THREAD) * 100;

        System.out.printf("\rProgress: %.1f%% | Operations: %d | Throughput: %.0f ops/sec | Success: %.1f%%",
                         Math.min(progress, 100.0), currentOps, currentThroughput, successRate);
    }

    private ErrorCategory categorizeError(Exception e, HttpResponse<?> response) {
        if (e instanceof java.net.http.HttpTimeoutException) {
            return ErrorCategory.NETWORK_TIMEOUT;
        }
        if (e instanceof java.net.ConnectException) {
            return ErrorCategory.NETWORK_CONNECTION;
        }
        if (response != null) {
            var statusCode = response.statusCode();
            if (statusCode >= 500) {
                return ErrorCategory.SERVER_ERROR;
            }
            if (statusCode >= 400 && statusCode < 500) {
                return ErrorCategory.VALIDATION_ERROR;
            }
        }
        if (e instanceof IllegalArgumentException) {
            return ErrorCategory.VALIDATION_ERROR;
        }
        return ErrorCategory.UNKNOWN;
    }

    private StatisticalSummary calculateStatistics(List<Long> values) {
        if (values.isEmpty()) {
            return new StatisticalSummary(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        Collections.sort(values);
        var size = values.size();

        // Calculate basic statistics
        var sum = values.stream().mapToLong(Long::longValue).sum();
        var mean = (double) sum / size;
        var median = (double) values.get(size / 2);

        // Calculate standard deviation
        var variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).sum() / size;
        var stdDev = Math.sqrt(variance);

        // Calculate percentiles
        var p50 = median;
        var p95 = getPercentile(values, 95);
        var p99 = getPercentile(values, 99);
        var p999 = getPercentile(values, 99.9);

        return new StatisticalSummary(mean, median, stdDev, (double) values.get(0), (double) values.get(size - 1),
                                    p50, p95, p99, p999);
    }

    private double getPercentile(List<Long> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) return 0.0;
        var index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        return (double) sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    private void printResults(Instant startTime, Instant endTime) {
        var duration = Duration.between(startTime, endTime).toMillis();
        var totalOps = totalOperations.get();
        var successOps = successfulOperations.get();
        var failedOps = failedOperations.get();
        var throughput = totalOps > 0 ? (double) totalOps / (duration / 1000.0) : 0;
        var successRate = totalOps > 0 ? (double) successOps / totalOps * 100 : 0;

        // Calculate response time statistics
        var responseTimeList = new ArrayList<>(responseTimes);
        var responseStats = calculateStatistics(responseTimeList);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏦 NETWORK STRESS TEST RESULTS");
        System.out.println("=".repeat(60));
        System.out.println("Test Configuration:");
        System.out.println("  Scenario: " + scenario.name());
        System.out.println("  Users: " + userIds.size() + " | Accounts: " + accountIds.size());
        System.out.println("  Threads: " + NUMBER_OF_THREADS + " | Operations/Thread: " + OPERATIONS_PER_THREAD);
        System.out.println();
        System.out.println("Performance Metrics:");
        System.out.println("  Duration: " + duration + " ms");
        System.out.println("  Total Operations: " + totalOps);
        System.out.println("  Successful Operations: " + successOps);
        System.out.println("  Failed Operations: " + failedOps);
        System.out.println("  Success Rate: " + String.format("%.2f%%", successRate));
        System.out.println("  Average Throughput: " + String.format("%.2f ops/sec", throughput));
        System.out.println();
        System.out.println("Response Time Statistics:");
        System.out.println("  Mean: " + String.format("%.2f ms", responseStats.mean));
        System.out.println("  Median (P50): " + String.format("%.2f ms", responseStats.median));
        System.out.println("  P95: " + String.format("%.2f ms", responseStats.p95));
        System.out.println("  P99: " + String.format("%.2f ms", responseStats.p99));
        System.out.println("  Min: " + String.format("%.2f ms", responseStats.min));
        System.out.println("  Max: " + String.format("%.2f ms", responseStats.max));
        System.out.println();
        System.out.println("Operation Breakdown:");
        for (var entry : operationTypeCounts.entrySet()) {
            var operation = entry.getKey();
            var count = entry.getValue();
            var percentage = totalOps > 0 ? (double) count.get() / totalOps * 100 : 0;
            System.out.println("  " + operation + ": " + count.get() + " (" + String.format("%.1f%%", percentage) + ")");
        }
        System.out.println();
        System.out.println("Error Analysis:");
        for (var entry : errorCounts.entrySet()) {
            var category = entry.getKey();
            var count = entry.getValue();
            if (count.get() > 0) {
                var percentage = failedOps > 0 ? (double) count.get() / failedOps * 100 : 0;
                System.out.println("  " + category.name() + ": " + count.get() + " (" + String.format("%.1f%%", percentage) + ")");
            }
        }
        System.out.println("=".repeat(60));
    }

    private void cleanup() throws InterruptedException {
        System.out.println("Cleaning up...");

        if (executorService != null) {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }

        System.out.println("Network stress test completed");
    }

    private void exportResults() {
        if ("CSV".equals(outputFormat) || "BOTH".equals(outputFormat)) {
            exportToCSV();
        }
        if ("JSON".equals(outputFormat) || "BOTH".equals(outputFormat)) {
            exportToJSON();
        }
    }

    private void exportToCSV() {
        try {
            var outputFile = new java.io.File("stress-test-results.csv");
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }

            try (var writer = new java.io.PrintWriter(outputFile)) {
                // CSV Header
                writer.println("timestamp,scenario,duration_ms,total_operations,successful_operations,failed_operations,success_rate,throughput_ops_sec,mean_response_time_ms,p50_response_time_ms,p95_response_time_ms,p99_response_time_ms");

                var duration = Duration.between(testStartTime, Instant.now()).toMillis();
                var totalOps = totalOperations.get();
                var successOps = successfulOperations.get();
                var failedOps = failedOperations.get();
                var throughput = totalOps > 0 ? (double) totalOps / (duration / 1000.0) : 0;
                var successRate = totalOps > 0 ? (double) successOps / totalOps * 100 : 0;

                var responseTimeList = new ArrayList<>(responseTimes);
                var stats = calculateStatistics(responseTimeList);

                writer.printf("%s,%s,%d,%d,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                    Instant.now().toString(),
                    scenario.name(),
                    duration,
                    totalOps,
                    successOps,
                    failedOps,
                    successRate,
                    throughput,
                    stats.mean,
                    stats.p50,
                    stats.p95,
                    stats.p99);

                System.out.println("✅ CSV results exported to: " + outputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to export CSV: " + e.getMessage());
        }
    }

    private void exportToJSON() {
        try {
            var objectMapper = new ObjectMapper();
            var rootNode = objectMapper.createObjectNode();

            // Test metadata
            var metadataNode = rootNode.putObject("testMetadata");
            metadataNode.put("scenario", scenario.name());
            metadataNode.put("startTime", testStartTime.toString());
            metadataNode.put("durationMs", Duration.between(testStartTime, Instant.now()).toMillis());
            metadataNode.put("totalOperations", totalOperations.get());
            metadataNode.put("outputFormat", outputFormat);
            metadataNode.put("progressEnabled", showProgress);

            // Summary statistics
            var summaryNode = rootNode.putObject("summary");
            var totalOps = totalOperations.get();
            var successOps = successfulOperations.get();
            var failedOps = failedOperations.get();
            summaryNode.put("totalOperations", totalOps);
            summaryNode.put("successfulOperations", successOps);
            summaryNode.put("failedOperations", failedOps);
            summaryNode.put("successRate", totalOps > 0 ? (double) successOps / totalOps * 100 : 0);
            summaryNode.put("averageThroughput", totalOps > 0 ? (double) totalOps / (Duration.between(testStartTime, Instant.now()).toMillis() / 1000.0) : 0);

            // Response time statistics
            var responseTimeList = new ArrayList<>(responseTimes);
            var stats = calculateStatistics(responseTimeList);
            var responseStatsNode = rootNode.putObject("responseTimeStats");
            responseStatsNode.put("mean", stats.mean);
            responseStatsNode.put("median", stats.median);
            responseStatsNode.put("stdDev", stats.stdDev);
            responseStatsNode.put("min", stats.min);
            responseStatsNode.put("max", stats.max);
            responseStatsNode.put("p50", stats.p50);
            responseStatsNode.put("p95", stats.p95);
            responseStatsNode.put("p99", stats.p99);
            responseStatsNode.put("p999", stats.p999);

            // Error breakdown
            var errorBreakdownNode = rootNode.putObject("errorBreakdown");
            errorCounts.forEach((category, count) -> {
                if (count.get() > 0) {
                    errorBreakdownNode.put(category.name(), count.get());
                }
            });

            // Operation breakdown
            var operationBreakdownNode = rootNode.putObject("operationBreakdown");
            operationTypeCounts.forEach((operation, count) -> {
                operationBreakdownNode.put(operation, count.get());
            });

            // Ensure parent directory exists and write to file
            var outputFile = new java.io.File("stress-test-results.json");
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, rootNode);

            System.out.println("✅ JSON results exported to: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Failed to export JSON: " + e.getMessage());
        }
    }

    // Enums and Records
    public enum TestScenario {
        BALANCED_LOAD(0.4, 0.3, 0.3),
        HEAVY_TRANSFERS(0.2, 0.2, 0.6),
        READ_HEAVY(0.8, 0.1, 0.1),
        STRESS_TRANSFERS(0.0, 0.0, 1.0),
        WITHDRAWAL_HEAVY(0.1, 0.8, 0.1);

        public final double depositRatio;
        public final double withdrawalRatio;
        public final double transferRatio;

        TestScenario(double depositRatio, double withdrawalRatio, double transferRatio) {
            this.depositRatio = depositRatio;
            this.withdrawalRatio = withdrawalRatio;
            this.transferRatio = transferRatio;
        }
    }

    public enum ErrorCategory {
        NETWORK_TIMEOUT,
        NETWORK_CONNECTION,
        SERVER_ERROR,
        VALIDATION_ERROR,
        UNKNOWN
    }

    public record StatisticalSummary(
        double mean,
        double median,
        double stdDev,
        double min,
        double max,
        double p50,
        double p95,
        double p99,
        double p999
    ) {}
}