# Bank Stress Testing Suite

This directory contains stress testing tools for the Bank application to profile performance under high load conditions.

## Overview

The stress test creates a high volume of concurrent operations to measure:
- Throughput (operations per second)
- Response times
- System stability under load
- Identification of performance bottlenecks

## Components

1. **StressTest.java** - Original stress testing application that directly accesses services (in-memory)
2. **NetworkStressTest.java** - Network-based stress testing application that communicates with the HTTP server
3. **pom.xml** - Maven build configuration for the stress test

## Configuration

The stress test can be configured by modifying these constants in `NetworkStressTest.java`:

```java
private static final String SERVER_URL = "http://localhost:8080";
private static final int NUMBER_OF_USERS = 100;           // Number of test users
private static final int NUMBER_OF_ACCOUNTS_PER_USER = 5; // Accounts per user
private static final int NUMBER_OF_THREADS = 20;          // Concurrent threads
private static final int OPERATIONS_PER_THREAD = 50;      // Operations per thread
```

## Running the Network Stress Test

1. First, ensure the main Bank application is built and installed:
   ```bash
   cd /path/to/Bank
   mvn clean install -DskipTests
   ```

2. Start the Bank server:
   ```bash
   cd /path/to/Bank
   ./scripts/run-server.sh
   ```
   (Run this in a separate terminal)

3. Build the stress test:
   ```bash
   cd /path/to/Bank/stress-test
   mvn clean package
   ```

4. Run the network stress test:
   ```bash
   java -jar target/stress-test-1.0-SNAPSHOT.jar
   ```

## Profiling Options

To profile the application performance, you can run with different JVM profiling options:

### CPU Profiling
```bash
java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=stress-test-cpu.jfr \
     -jar target/stress-test-1.0-SNAPSHOT.jar
```

### Memory Profiling
```bash
java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=stress-test-memory.jfr,settings=profile \
     -jar target/stress-test-1.0-SNAPSHOT.jar
```

## Expected Output

The network stress test will produce output similar to:
```
Starting network-based stress test...
Server URL: http://localhost:8080
Initializing HTTP client...
HTTP client initialized
Creating users...
Created 100 users
Creating accounts...
Created 500 accounts
Executing concurrent operations...
Thread 0 started
Thread 1 started
...
Thread 19 completed

=== NETWORK STRESS TEST RESULTS ===
Duration: 12500 ms
Total operations: 1000
Successful operations: 985
Failed operations: 15
Success rate: 98.50%
Throughput: 80.00 ops/sec
Average response time: 2.50 ms
```

The in-memory stress test will produce similar output but without network latency:
```
Starting stress test...
Initializing system...
Created 10000 users
Creating accounts...
Created 590000 accounts
Executing concurrent operations...
Thread 0 started
Thread 1 started
...
Thread 2322 completed

=== STRESS TEST RESULTS ===
Duration: 8500 ms
Total operations: 232300
Successful operations: 232300
Failed operations: 0
Success rate: 100.00%
Throughput: 27329.41 ops/sec
Average response time: 0.04 ms
```

## Interpreting Results

- **Throughput**: Higher is better. Indicates how many operations the system can handle per second.
- **Response Time**: Lower is better. Average time taken per operation.
- **Success Rate**: Percentage of successful operations. Should be as close to 100% as possible.
- **Failure Analysis**: Any failed operations should be investigated for race conditions or other issues.

## Running the Original (In-Memory) Stress Test

If you want to run the original stress test that directly accesses services without going through the network:

1. Update the pom.xml to use `com.bank.stress.StressTest` as the main class
2. Run:
   ```bash
   java -cp "target/stress-test-1.0-SNAPSHOT.jar:/path/to/.m2/repository/com/prem/Bank/1.0-SNAPSHOT/Bank-1.0-SNAPSHOT.jar" com.bank.stress.StressTest
   ```

This version will be faster but won't test the HTTP layer or network performance.