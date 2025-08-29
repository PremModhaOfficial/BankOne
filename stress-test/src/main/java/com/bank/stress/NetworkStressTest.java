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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
public class NetworkStressTest
{

    // Supporting classes and enums
    public enum ErrorCategory
    {
        NETWORK_TIMEOUT, NETWORK_CONNECTION, SERVER_ERROR, VALIDATION_ERROR, BUSINESS_LOGIC_ERROR, CLIENT_ERROR, UNKNOWN
    }

    public enum TestScenario
    {
        BALANCED_LOAD(0.4, 0.3, 0.3),      // 40% deposits, 30% withdrawals, 30% transfers
        HEAVY_TRANSFERS(0.2, 0.2, 0.6),   // 60% transfers
        READ_HEAVY(0.8, 0.1, 0.1),        // 80% deposits (read-like)
        STRESS_TRANSFERS(0.0, 0.0, 1.0),  // 100% transfers
        WITHDRAWAL_HEAVY(0.1, 0.8, 0.1);  // 80% withdrawals

        public final double depositRatio, withdrawalRatio, transferRatio;

        TestScenario(double deposits, double withdrawals, double transfers)
        {
            this.depositRatio = deposits;
            this.withdrawalRatio = withdrawals;
            this.transferRatio = transfers;
        }
    }

    public static class ThroughputSample
    {
        public final long timestamp;
        public final long operationsCompleted;
        public final double throughput;

        public ThroughputSample(long timestamp, long operationsCompleted, double throughput)
        {
            this.timestamp = timestamp;
            this.operationsCompleted = operationsCompleted;
            this.throughput = throughput;
        }
    }

    public static class StatisticalSummary
    {
        public final double mean, median, stdDev, min, max;
        public final double p50, p95, p99, p999;

        public StatisticalSummary(double mean, double median, double stdDev, double min, double max, double p50, double p95, double p99, double p999)
        {
            this.mean = mean;
            this.median = median;
            this.stdDev = stdDev;
            this.min = min;
            this.max = max;
            this.p50 = p50;
            this.p95 = p95;
            this.p99 = p99;
            this.p999 = p999;
        }
    }

    private static final String SERVER_URL = "http://localhost:8080";
    private static final long NUMBER_OF_USERS = 5; // Reduced for network testing
    private static final int NUMBER_OF_ACCOUNTS_PER_USER = 10; // Accounts per user
    private static final int NUMBER_OF_THREADS = 8; // Concurrent threads
    private static final long OPERATIONS_PER_THREAD = 200_000;

    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private List<Long> userIds;
    private List<Long> accountIds;
    private ExecutorService executorService;

    // Core metrics
    private AtomicLong totalOperations = new AtomicLong(0);
    private AtomicInteger successfulOperations = new AtomicInteger(0);
    private AtomicInteger failedOperations = new AtomicInteger(0);

    // Enhanced metrics for analysis
    private AtomicLong totalResponseTime = new AtomicLong(0);
    private List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
    private ConcurrentHashMap<Integer, AtomicInteger> statusCodeCounts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AtomicInteger> operationTypeCounts = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, AtomicLong> operationResponseTimes = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ErrorCategory, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private List<ThroughputSample> throughputSamples = Collections.synchronizedList(new ArrayList<>());

    // Progress reporting
    private ScheduledExecutorService progressReporter;
    private Instant testStartTime;
    private boolean enableProgressReporting = true;

    // Configuration
    private TestScenario scenario = TestScenario.BALANCED_LOAD;
    private String outputFormat = "CONSOLE"; // CONSOLE, CSV, JSON, BOTH

    public static void main(String[] args) throws Exception
    {
        var stressTest = new NetworkStressTest();

        // Parse command line arguments
        for (int i = 0; i < args.length; i++)
        {
            switch (args[i])
            {
                case "--scenario":
                    if (i + 1 < args.length)
                    {
                        stressTest.scenario = TestScenario.valueOf(args[++i]);
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length)
                    {
                        stressTest.outputFormat = args[++i].toUpperCase();
                    }
                    break;
                case "--no-progress":
                    stressTest.enableProgressReporting = false;
                    break;
                case "--progress":
                    stressTest.enableProgressReporting = true;
                    break;
                case "--help":
                    printUsage();
                    return;
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    printUsage();
                    return;
            }
        }

        stressTest.runStressTest();
    }

    private static void printUsage()
    {
        System.out.println("NetworkStressTest Usage:");
        System.out.println("  --scenario <BALANCED_LOAD|HEAVY_TRANSFERS|READ_HEAVY|STRESS_TRANSFERS|WITHDRAWAL_HEAVY>");
        System.out.println("  --output <CONSOLE|CSV|JSON|BOTH>");
        System.out.println("  --progress    Enable progress reporting (default)");
        System.out.println("  --no-progress Disable progress reporting");
        System.out.println("  --help        Show this help message");
    }

    public void runStressTest() throws Exception
    {
        System.out.println("Starting network-based stress test...");
        System.out.println("Server URL: " + SERVER_URL);
        System.out.println("Test Scenario: " + scenario);
        System.out.println("Output Format: " + outputFormat);
        System.out.println("Progress Reporting: " + (enableProgressReporting ? "Enabled" : "Disabled"));

        // Initialize the system
        initialize();

        try
        {
            // Create users
            createUsers();

            // Create accounts for users
            createAccounts();

            // Start the stress test
            testStartTime = Instant.now();
            var startTime = System.currentTimeMillis();

            if (enableProgressReporting)
            {
                startProgressReporting();
            }

            executeConcurrentOperations();

            var endTime = System.currentTimeMillis();

            if (enableProgressReporting)
            {
                stopProgressReporting();
            }

            // Print results
            printResults(startTime, endTime);

            // Export results if requested
            exportResults();

        } finally
        {
            // Cleanup
            cleanup();
        }
    }

    private void initialize()
    {
        System.out.println("Initializing HTTP client and metrics collection...");

        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        objectMapper = new ObjectMapper();
        userIds = new ArrayList<>();
        accountIds = new ArrayList<>();

        // Initialize metrics collections
        for (ErrorCategory category : ErrorCategory.values())
        {
            errorCounts.put(category, new AtomicInteger(0));
        }

        //        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        executorService = Executors.newVirtualThreadPerTaskExecutor();

        // Initialize progress reporter
        if (enableProgressReporting)
        {
            progressReporter = Executors.newScheduledThreadPool(1);
        }

        System.out.println("HTTP client and metrics initialized");
    }

    private void createUsers() throws Exception
    {
        System.out.println("Creating users...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (var i = 0; i < NUMBER_OF_USERS; i++)
        {
            final var userIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try
                {
                    var userJson = String.format(
                            "{\"username\":\"stress_user_%d\",\"email\":\"stress%d@example.com\", \"password\": \"stress_pass_%d\"}", userIndex, userIndex, userIndex);

                    var request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/users")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(userJson)).build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 201)
                    {
                        // Parse the user ID from the response
                        var jsonNode = objectMapper.readTree(response.body());
                        var userId = jsonNode.get("id").asLong();
                        synchronized (userIds)
                        {
                            userIds.add(userId);
                        }
                        System.out.println("Created user " + userId);
                    } else
                    {
                        System.err.println("Failed to create user, status: " + response.statusCode() + ", response: " + response.body());
                    }
                } catch (Exception exception)
                {
                    System.err.println("Error creating user " + userIndex + ": " + exception.getMessage());
                    exception.printStackTrace();
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
            for (var j = 0; j < NUMBER_OF_ACCOUNTS_PER_USER; j++)
            {
                final var finalUserId = userId;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try
                    {
                        var accountJson = String.format(
                                "{\"userId\":%d,\"balance\":1000.0,\"type\":\"SAVINGS\"}", finalUserId);

                        var request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(accountJson)).build();

                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 201)
                        {
                            // Parse the account ID from the response
                            var jsonNode = objectMapper.readTree(response.body());
                            var accountId = jsonNode.get("id").asLong();
                            synchronized (accountIds)
                            {
                                accountIds.add(accountId);
                            }
                            System.out.println("Created account " + accountId + " for user " + finalUserId);
                        } else
                        {
                            System.err.println("Failed to create account for user " + finalUserId + ", status: " + response.statusCode() + ", response: " + response.body());
                        }
                    } catch (Exception exception)
                    {
                        System.err.println(
                                "Error creating account for user " + finalUserId + ": " + exception.getMessage());
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
        for (var i = 0; i < NUMBER_OF_THREADS; i++)
        {
            final var threadId = i;
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
        var deposits = 0;
        var transfers = 0;
        var withdrawals = 0;
        System.out.println("Thread " + threadId + " started");

        for (var i = 0; i < OPERATIONS_PER_THREAD; i++)
        {
            totalOperations.incrementAndGet();

            // Sample throughput every 1000 operations
            if ((totalOperations.get() % 1000) == 0)
            {
                long currentTime = System.currentTimeMillis();
                long elapsedMs = currentTime - testStartTime.toEpochMilli();
                double currentThroughput = elapsedMs > 0 ? (double) totalOperations.get() / (elapsedMs / 1000.0) : 0;
                throughputSamples.add(new ThroughputSample(currentTime, totalOperations.get(), currentThroughput));
            }

            try
            {
                // Select operation based on scenario ratios
                var random = Math.random();
                var success = false;
                String operationType;

                if (random < scenario.depositRatio)
                {
                    success = performDeposit();
                    deposits++;
                    operationType = "DEPOSIT";
                } else if (random < scenario.depositRatio + scenario.withdrawalRatio)
                {
                    success = performWithdrawal();
                    withdrawals++;
                    operationType = "WITHDRAWAL";
                } else
                {
                    success = performTransfer();
                    transfers++;
                    operationType = "TRANSFER";
                }

                // Update operation type counts
                operationTypeCounts.computeIfAbsent(operationType, k -> new AtomicInteger(0)).incrementAndGet();

                if (success)
                {
                    successfulOperations.incrementAndGet();
                } else
                {
                    failedOperations.incrementAndGet();
                }
            } catch (Exception exception)
            {
                System.err.println("Error in thread " + threadId + " operation " + i + ": " + exception.getMessage());
                failedOperations.incrementAndGet();
                errorCounts.get(categorizeError(exception, null)).incrementAndGet();
            }
        }

        System.out.println("Thread " + threadId + " completed - Deposits: " + deposits + ", Withdrawals: " + withdrawals + ", Transfers: " + transfers);
    }

    private boolean performDeposit()
    {
        long startTime = System.nanoTime();
        HttpResponse<String> response = null;

        try
        {
            // Select a random account
            if (accountIds.isEmpty())
                return false;
            var accountIndex = (int) (Math.random() * accountIds.size());
            var accountId = accountIds.get(accountIndex);

            // Generate a random deposit amount
            var amount = new BigDecimal(Math.random() * 100);

            var depositJson = String.format("{\"amount\":%f}", amount.doubleValue());

            var request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts/" + accountId + "/deposit")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(depositJson)).build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Record metrics
            long responseTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
            responseTimes.add(responseTime);
            totalResponseTime.addAndGet(responseTime);

            // Record status code
            statusCodeCounts.computeIfAbsent(response.statusCode(), k -> new AtomicInteger(0)).incrementAndGet();

            // Record operation response time
            operationResponseTimes.computeIfAbsent("DEPOSIT", k -> new AtomicLong(0)).addAndGet(responseTime);

            return response.statusCode() == 200;
        } catch (Exception exception)
        {
            // Record error metrics
            long responseTime = (System.nanoTime() - startTime) / 1_000_000;
            responseTimes.add(responseTime);
            totalResponseTime.addAndGet(responseTime);

            errorCounts.get(categorizeError(exception, response)).incrementAndGet();

            System.err.println("Error performing deposit: " + exception.getMessage());
            return false;
        }
    }

    private boolean performWithdrawal()
    {
        long startTime = System.nanoTime();
        HttpResponse<String> response = null;

        try
        {
            // Select a random account
            if (accountIds.isEmpty())
                return false;
            var accountIndex = (int) (Math.random() * accountIds.size());
            var accountId = accountIds.get(accountIndex);

            // Generate a random withdrawal amount
            var amount = new BigDecimal(Math.random() * 50);

            var withdrawJson = String.format("{\"amount\":%f}", amount.doubleValue());

            var request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts/" + accountId + "/withdraw")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(withdrawJson)).build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Record metrics
            long responseTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
            responseTimes.add(responseTime);
            totalResponseTime.addAndGet(responseTime);

            // Record status code
            statusCodeCounts.computeIfAbsent(response.statusCode(), k -> new AtomicInteger(0)).incrementAndGet();

            // Record operation response time
            operationResponseTimes.computeIfAbsent("WITHDRAWAL", k -> new AtomicLong(0)).addAndGet(responseTime);

            return response.statusCode() == 200;
        } catch (Exception exception)
        {
            // Record error metrics
            long responseTime = (System.nanoTime() - startTime) / 1_000_000;
            responseTimes.add(responseTime);
            totalResponseTime.addAndGet(responseTime);

            errorCounts.get(categorizeError(exception, response)).incrementAndGet();

            System.err.println("Error performing withdrawal: " + exception.getMessage());
            return false;
        }
    }

    private boolean performTransfer()
    {
        long startTime = System.nanoTime();
        HttpResponse<String> response = null;

        try
        {
            // Select two different random accounts
            if (accountIds.size() < 2)
                return false;
            var fromAccountIndex = (int) (Math.random() * accountIds.size());
            int toAccountIndex;
            do
            {
                toAccountIndex = (int) (Math.random() * accountIds.size());
            } while (toAccountIndex == fromAccountIndex);

            var fromAccountId = accountIds.get(fromAccountIndex);
            var toAccountId = accountIds.get(toAccountIndex);

            // Generate a random transfer amount
            var amount = new BigDecimal(Math.random() * 25);

            var transferJson = String.format("{\"toAccountId\":%d,\"amount\":%f}", toAccountId, amount.doubleValue());

            var request = HttpRequest.newBuilder().uri(URI.create(SERVER_URL + "/accounts/" + fromAccountId + "/transfer")).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(transferJson)).build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Record metrics
            long responseTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
            responseTimes.add(responseTime);
            totalResponseTime.addAndGet(responseTime);

            // Record status code
            statusCodeCounts.computeIfAbsent(response.statusCode(), k -> new AtomicInteger(0)).incrementAndGet();

            // Record operation response time
            operationResponseTimes.computeIfAbsent("TRANSFER", k -> new AtomicLong(0)).addAndGet(responseTime);

            return response.statusCode() == 200;
        } catch (Exception exception)
        {
            // Record error metrics
            long responseTime = (System.nanoTime() - startTime) / 1_000_000;
            responseTimes.add(responseTime);
            totalResponseTime.addAndGet(responseTime);

            errorCounts.get(categorizeError(exception, response)).incrementAndGet();

            System.err.println("Error performing transfer: " + exception.getMessage());
            return false;
        }
    }

    private void startProgressReporting()
    {
        progressReporter.scheduleAtFixedRate(this::reportProgress, 0, 5, TimeUnit.SECONDS);
    }

    private void stopProgressReporting()
    {
        if (progressReporter != null)
        {
            progressReporter.shutdown();
            try
            {
                if (!progressReporter.awaitTermination(5, TimeUnit.SECONDS))
                {
                    progressReporter.shutdownNow();
                }
            } catch (InterruptedException e)
            {
                progressReporter.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void reportProgress()
    {
        long currentOps = totalOperations.get();
        long elapsedMs = java.time.Duration.between(testStartTime, Instant.now()).toMillis();
        double currentThroughput = elapsedMs > 0 ? (double) currentOps / (elapsedMs / 1000.0) : 0;
        double successRate = currentOps > 0 ? (double) successfulOperations.get() / currentOps * 100 : 0;
        double progress = (double) currentOps / (NUMBER_OF_THREADS * OPERATIONS_PER_THREAD) * 100;

        System.out.printf("\rProgress: %.1f%% | Operations: %d | Throughput: %.0f ops/sec | Success: %.1f%%", Math.min(progress, 100.0), currentOps, currentThroughput, successRate);
    }

    private ErrorCategory categorizeError(Exception e, HttpResponse<?> response)
    {
        if (e instanceof java.net.http.HttpTimeoutException)
        {
            return ErrorCategory.NETWORK_TIMEOUT;
        }
        if (e instanceof java.net.ConnectException)
        {
            return ErrorCategory.NETWORK_CONNECTION;
        }
        if (response != null)
        {
            int statusCode = response.statusCode();
            if (statusCode >= 500)
            {
                return ErrorCategory.SERVER_ERROR;
            }
            if (statusCode >= 400 && statusCode < 500)
            {
                return ErrorCategory.VALIDATION_ERROR;
            }
        }
        if (e instanceof IllegalArgumentException)
        {
            return ErrorCategory.VALIDATION_ERROR;
        }
        return ErrorCategory.UNKNOWN;
    }

    private StatisticalSummary calculateStatistics(List<Long> values)
    {
        if (values.isEmpty())
        {
            return new StatisticalSummary(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        Collections.sort(values);
        int size = values.size();

        // Calculate basic statistics
        double sum = values.stream().mapToLong(Long::longValue).sum();
        double mean = sum / size;
        double median = values.get(size / 2);

        // Calculate standard deviation
        double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).sum() / size;
        double stdDev = Math.sqrt(variance);

        // Calculate percentiles
        double p50 = median;
        double p95 = getPercentile(values, 95);
        double p99 = getPercentile(values, 99);
        double p999 = getPercentile(values, 99.9);

        return new StatisticalSummary(mean, median, stdDev, values.get(0), values.get(size - 1), p50, p95, p99, p999);
    }

    private double getPercentile(List<Long> sortedValues, double percentile)
    {
        if (sortedValues.isEmpty()) return 0.0;
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    private void exportResults()
    {
        if ("CSV".equals(outputFormat) || "BOTH".equals(outputFormat))
        {
            exportToCSV();
        }
        if ("JSON".equals(outputFormat) || "BOTH".equals(outputFormat))
        {
            exportToJSON();
        }
    }

    private void exportToCSV()
    {
        try (java.io.PrintWriter writer = new java.io.PrintWriter("stress-test-results.csv"))
        {
            // CSV Header
            writer.println("timestamp,operation_type,response_time_ms,status_code,success,error_category");

            // Note: In a real implementation, you'd collect individual operation data
            // For now, we'll export summary statistics
            writer.printf("%s,SUMMARY,%.2f,%d,%b,%s%n", Instant.now().toString(), responseTimes.stream().mapToLong(Long::longValue).average().orElse(0), 200, // Summary status
                    true, "SUMMARY");

        } catch (Exception e)
        {
            System.err.println("Failed to export CSV: " + e.getMessage());
        }
    }

    private void exportToJSON()
    {
        try
        {
            var objectMapper = new ObjectMapper();
            var rootNode = objectMapper.createObjectNode();

            // Test metadata
            var metadataNode = rootNode.putObject("testMetadata");
            metadataNode.put("scenario", scenario.name());
            metadataNode.put("startTime", testStartTime.toString());
            metadataNode.put("durationMs", java.time.Duration.between(testStartTime, Instant.now()).toMillis());
            metadataNode.put("totalOperations", totalOperations.get());

            // Summary statistics
            var summaryNode = rootNode.putObject("summary");
            long totalOps = totalOperations.get();
            long successOps = successfulOperations.get();
            long failedOps = failedOperations.get();
            summaryNode.put("totalOperations", totalOps);
            summaryNode.put("successfulOperations", successOps);
            summaryNode.put("failedOperations", failedOps);
            summaryNode.put("successRate", totalOps > 0 ? (double) successOps / totalOps * 100 : 0);
            summaryNode.put("averageThroughput", totalOps > 0 ? (double) totalOps / (java.time.Duration.between(testStartTime, Instant.now()).toMillis() / 1000.0) : 0);

            // Response time statistics
            var stats = calculateStatistics(responseTimes);
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
                if (count.get() > 0)
                {
                    errorBreakdownNode.put(category.name(), count.get());
                }
            });

            // Operation breakdown
            var operationBreakdownNode = rootNode.putObject("operationBreakdown");
            operationTypeCounts.forEach((operation, count) -> {
                operationBreakdownNode.put(operation, count.get());
            });

            // Throughput samples
            var throughputArray = rootNode.putArray("throughputSamples");
            throughputSamples.forEach(sample -> {
                var sampleNode = throughputArray.addObject();
                sampleNode.put("timestamp", sample.timestamp);
                sampleNode.put("operations", sample.operationsCompleted);
                sampleNode.put("throughput", sample.throughput);
            });

            // Write to file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new java.io.File("stress-test-results.json"), rootNode);

        } catch (Exception e)
        {
            System.err.println("Failed to export JSON: " + e.getMessage());
        }
    }

    private void printResults(long startTime, long endTime)
    {
        var duration = endTime - startTime;
        var totalOps = totalOperations.get();
        var successOps = successfulOperations.get();
        var failedOps = failedOperations.get();
        var throughput = totalOps > 0 ? (double) totalOps / (duration / 1000.0) : 0;
        var successRate = totalOps > 0 ? (double) successOps / totalOps * 100 : 0;

        // Calculate response time statistics
        var responseStats = calculateStatistics(responseTimes);

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
        operationTypeCounts.forEach((operation, count) -> {
            double percentage = totalOps > 0 ? (double) count.get() / totalOps * 100 : 0;
            System.out.println("  " + operation + ": " + count.get() + " (" + String.format("%.1f%%", percentage) + ")");
        });
        System.out.println();
        System.out.println("Error Analysis:");
        errorCounts.forEach((category, count) -> {
            if (count.get() > 0)
            {
                double percentage = failedOps > 0 ? (double) count.get() / failedOps * 100 : 0;
                System.out.println("  " + category.name() + ": " + count.get() + " (" + String.format("%.1f%%", percentage) + ")");
            }
        });
        System.out.println("=".repeat(60));
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
