# Bank Stress Testing Suite

This directory contains stress testing tools for the Bank application to profile performance under high load conditions.

## Overview

The stress test creates a high volume of concurrent operations to measure:
- Throughput (operations per second)
- Response times
- System stability under load
- Identification of performance bottlenecks

## Components

1. **StressTest.java** - Main stress testing application that:
   - Creates multiple users and accounts
   - Executes concurrent deposit, withdrawal, and transfer operations
   - Measures performance metrics
   - Reports results

2. **pom.xml** - Maven build configuration for the stress test

## Configuration

The stress test can be configured by modifying these constants in `StressTest.java`:

```java
private static final int NUMBER_OF_USERS = 100;           // Number of test users
private static final int NUMBER_OF_ACCOUNTS_PER_USER = 5; // Accounts per user
private static final int NUMBER_OF_THREADS = 50;          // Concurrent threads
private static final int OPERATIONS_PER_THREAD = 100;     // Operations per thread
```

## Running the Stress Test

1. First, ensure the main Bank application is built and installed:
   ```bash
   cd /path/to/Bank
   mvn clean install -DskipTests
   ```

2. Build the stress test:
   ```bash
   cd /path/to/Bank/stress-test
   mvn clean package
   ```

3. Run the stress test:
   ```bash
   java -cp "target/stress-test-1.0-SNAPSHOT.jar:/path/to/.m2/repository/com/prem/Bank/1.0-SNAPSHOT/Bank-1.0-SNAPSHOT.jar" com.bank.stress.StressTest
   ```

## Profiling Options

To profile the application performance, you can run with different JVM profiling options:

### CPU Profiling
```bash
java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=stress-test-cpu.jfr \
     -cp "target/stress-test-1.0-SNAPSHOT.jar:/path/to/.m2/repository/com/prem/Bank/1.0-SNAPSHOT/Bank-1.0-SNAPSHOT.jar" \
     com.bank.stress.StressTest
```

### Memory Profiling
```bash
java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=stress-test-memory.jfr,settings=profile \
     -cp "target/stress-test-1.0-SNAPSHOT.jar:/path/to/.m2/repository/com/prem/Bank/1.0-SNAPSHOT/Bank-1.0-SNAPSHOT.jar" \
     com.bank.stress.StressTest
```

## Expected Output

The stress test will produce output similar to:
```
Starting stress test...
Initializing system...
Created 100 users
Creating accounts...
Created 500 accounts
Executing concurrent operations...
Thread 0 started
Thread 1 started
...
Thread 49 completed

=== STRESS TEST RESULTS ===
Duration: 12500 ms
Total operations: 5000
Successful operations: 4985
Failed operations: 15
Throughput: 400.00 ops/sec
Average response time: 2.50 ms
```

## Interpreting Results

- **Throughput**: Higher is better. Indicates how many operations the system can handle per second.
- **Response Time**: Lower is better. Average time taken per operation.
- **Success Rate**: Percentage of successful operations. Should be as close to 100% as possible.
- **Failure Analysis**: Any failed operations should be investigated for race conditions or other issues.