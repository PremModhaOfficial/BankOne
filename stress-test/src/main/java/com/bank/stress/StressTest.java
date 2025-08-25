package com.bank.stress;

import com.bank.business.entities.Account;
import com.bank.business.entities.User;
import com.bank.business.services.AccountService;
import com.bank.business.services.UserService;
import com.bank.infrastructure.persistence.inmemory.InMemoryAccountRepository;
import com.bank.infrastructure.persistence.inmemory.InMemoryUserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stress test for the banking system to profile performance under high load.
 * This test creates multiple threads performing concurrent operations to
 * measure
 * throughput and identify performance bottlenecks.
 */
public class StressTest {

    private static final int NUMBER_OF_USERS = 10000;
    private static final int NUMBER_OF_ACCOUNTS_PER_USER = 59;
    private static final int NUMBER_OF_THREADS = 2323;
    private static final int OPERATIONS_PER_THREAD = 100;

    private UserService userService;
    private AccountService accountService;
    private List<User> users;
    private List<Account> accounts;
    private ExecutorService executorService;

    private AtomicLong totalOperations = new AtomicLong(0);
    private AtomicInteger successfulOperations = new AtomicInteger(0);
    private AtomicInteger failedOperations = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        StressTest stressTest = new StressTest();
        stressTest.runStressTest();
    }

    public void runStressTest() throws Exception {
        System.out.println("Starting stress test...");

        // Initialize the system
        initialize();

        // Create accounts for users
        createAccounts();

        // Start the stress test
        long startTime = System.currentTimeMillis();
        executeConcurrentOperations();
        long endTime = System.currentTimeMillis();

        // Print results
        printResults(startTime, endTime);

        // Cleanup
        cleanup();
    }

    private void initialize() {
        System.out.println("Initializing system...");

        // Set up repositories and services
        InMemoryUserRepository userRepository = InMemoryUserRepository.getInstance();
        InMemoryAccountRepository accountRepository = InMemoryAccountRepository.getInstance();

        userService = new UserService(userRepository);
        accountService = new AccountService(accountRepository);

        users = new ArrayList<>();
        accounts = new ArrayList<>();

        // Create users
        for (int i = 0; i < NUMBER_OF_USERS; i++) {
            User user = userService.createUser("user" + i, "user" + i + "@example.com");
            users.add(user);
        }

        System.out.println("Created " + users.size() + " users");

        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    private void createAccounts() {
        System.out.println("Creating accounts...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (User user : users) {
            for (int j = 0; j < NUMBER_OF_ACCOUNTS_PER_USER; j++) {
                final User finalUser = user;
                final int accountIndex = j;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    Account account = accountService.createAccount(
                            finalUser.getId(),
                            "ACC" + finalUser.getId() + "_" + accountIndex,
                            new BigDecimal("1000.0"),
                            Account.AccountType.SAVINGS);
                    synchronized (accounts) {
                        accounts.add(account);
                    }
                }, executorService);
                futures.add(future);
            }
        }

        // Wait for all accounts to be created
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        System.out.println("Created " + accounts.size() + " accounts");
    }

    private void executeConcurrentOperations() throws InterruptedException {
        System.out.println("Executing concurrent operations...");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Submit tasks to the executor service
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                performOperations(threadId);
            }, executorService);
            futures.add(future);
        }

        // Wait for all operations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void performOperations(int threadId) {
        System.out.println("Thread " + threadId + " started");

        for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
            totalOperations.incrementAndGet();

            try {
                // Randomly select an operation
                int operationType = (int) (Math.random() * 3);

                boolean success = false;
                switch (operationType) {
                    case 0:
                        try {
                            performDeposit();
                            success = true;
                        } catch (Exception e) {
                            // Operation failed
                        }
                        break;
                    case 1:
                        try {
                            performWithdrawal();
                            success = true;
                        } catch (Exception e) {
                            // Operation failed
                        }
                        break;
                    case 2:
                        try {
                            performTransfer();
                            success = true;
                        } catch (Exception e) {
                            // Operation failed
                        }
                        break;
                }

                if (success) {
                    successfulOperations.incrementAndGet();
                } else {
                    failedOperations.incrementAndGet();
                }
            } catch (Exception e) {
                System.err.println("Error in thread " + threadId + " operation " + i + ": " + e.getMessage());
                failedOperations.incrementAndGet();
            }
        }

        System.out.println("Thread " + threadId + " completed");
    }

    private void performDeposit() {
        // Select a random account
        int accountIndex = (int) (Math.random() * accounts.size());
        Account account = accounts.get(accountIndex);

        // Generate a random deposit amount
        BigDecimal amount = new BigDecimal(Math.random() * 100);

        // Perform the deposit
        account.addAmount(amount);
        accountService.updateAccount(account);
    }

    private void performWithdrawal() {
        // Select a random account
        int accountIndex = (int) (Math.random() * accounts.size());
        Account account = accounts.get(accountIndex);

        // Generate a random withdrawal amount
        BigDecimal amount = new BigDecimal(Math.random() * 50);

        // Perform the withdrawal
        account.withdrawAmount(amount);
        accountService.updateAccount(account);
    }

    private void performTransfer() {
        // Select two different random accounts
        int fromAccountIndex = (int) (Math.random() * accounts.size());
        int toAccountIndex;
        do {
            toAccountIndex = (int) (Math.random() * accounts.size());
        } while (toAccountIndex == fromAccountIndex);

        Account fromAccount = accounts.get(fromAccountIndex);
        Account toAccount = accounts.get(toAccountIndex);

        // Generate a random transfer amount
        BigDecimal amount = new BigDecimal(Math.random() * 25);

        // Perform the transfer
        boolean success = fromAccount.withdrawAmount(amount);
        if (success) {
            toAccount.addAmount(amount);
            accountService.updateAccount(fromAccount);
            accountService.updateAccount(toAccount);
        }
    }

    private void printResults(long startTime, long endTime) {
        long duration = endTime - startTime;
        long totalOps = totalOperations.get();
        int successOps = successfulOperations.get();
        int failedOps = failedOperations.get();
        double throughput = totalOps > 0 ? (double) totalOps / (duration / 1000.0) : 0;
        double successRate = totalOps > 0 ? (double) successOps / totalOps * 100 : 0;

        System.out.println("=== STRESS TEST RESULTS ===");
        System.out.println("Duration: " + duration + " ms");
        System.out.println("Total operations: " + totalOps);
        System.out.println("Successful operations: " + successOps);
        System.out.println("Failed operations: " + failedOps);
        System.out.println("Success rate: " + String.format("%.2f", successRate) + "%");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " ops/sec");
        System.out.println("Average response time: " + String.format("%.2f", totalOps > 0 ? (double) duration / totalOps : 0) + " ms");
    }

    private void cleanup() throws InterruptedException {
        System.out.println("Cleaning up...");

        if (executorService != null) {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }

        System.out.println("Stress test completed");
    }
}
