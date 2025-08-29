# 🏦 Banking System Stress Tests

A comprehensive stress testing suite for the banking system with advanced metrics collection and analysis capabilities.

## 📋 Overview

This stress testing suite provides multiple testing scenarios to evaluate the banking system's performance under various load conditions. It includes detailed metrics collection, error analysis, and multiple output formats for comprehensive performance analysis.

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.6+
- Running banking system server on `http://localhost:8080`

### Run a Smoke Test (Recommended First)
```bash
./run-smoke-test.sh
```

### 🚀 Quick Start Scripts

#### 1. Smoke Test (Recommended First)
```bash
./run-smoke-test.sh
```
- **Purpose**: Quick system verification
- **Duration**: ~30 seconds
- **Resources**: Minimal

#### 2. Individual Scenario Tests
```bash
./run-balanced-test.sh          # General performance evaluation
./run-read-heavy-test.sh        # Read-heavy workload testing
./run-withdrawal-heavy-test.sh  # Withdrawal performance & validation
./run-heavy-transfers-test.sh   # Transfer performance with mixed ops
./run-transfer-stress-test.sh   # Maximum concurrency stress test
./run-analysis-test.sh          # Comprehensive analysis data
```

#### 3. Complete Test Suite
```bash
./run-all-scenarios.sh          # Run all scenarios sequentially
./run-quick-reference.sh        # Show all available options
```

## 📊 Test Scenarios & Scripts

### 📋 Complete Script Reference

| Script | Scenario | Operation Mix | Duration | Purpose |
|--------|----------|---------------|----------|---------|
| `run-smoke-test.sh` | READ_HEAVY | 80% deposits | ~30s | System verification |
| `run-balanced-test.sh` | BALANCED_LOAD | 40/30/30 | Full | General evaluation |
| `run-read-heavy-test.sh` | READ_HEAVY | 80/10/10 | Full | Read performance |
| `run-withdrawal-heavy-test.sh` | WITHDRAWAL_HEAVY | 10/80/10 | Full | Withdrawal testing |
| `run-heavy-transfers-test.sh` | HEAVY_TRANSFERS | 20/20/60 | Full | Transfer performance |
| `run-transfer-stress-test.sh` | STRESS_TRANSFERS | 0/0/100 | Full | Concurrency stress |
| `run-analysis-test.sh` | BALANCED_LOAD | 40/30/30 | Full | Analysis data |
| `run-all-scenarios.sh` | ALL | All mixes | Extended | Complete suite |
| `run-quick-reference.sh` | N/A | N/A | Instant | Help & options |

### 🎯 Scenario Details

| Scenario | Deposits | Withdrawals | Transfers | Best For |
|----------|----------|-------------|-----------|----------|
| `BALANCED_LOAD` | 40% | 30% | 30% | General system evaluation |
| `HEAVY_TRANSFERS` | 20% | 20% | 60% | Transfer performance analysis |
| `READ_HEAVY` | 80% | 10% | 10% | Read-heavy workload testing |
| `STRESS_TRANSFERS` | 0% | 0% | 100% | Maximum concurrency stress |
| `WITHDRAWAL_HEAVY` | 10% | 80% | 10% | Withdrawal validation & performance |

## ⚙️ Configuration Options

### Command Line Arguments
```bash
java NetworkStressTest [options]

Options:
  --scenario <SCENARIO>    Test scenario (default: BALANCED_LOAD)
  --output <FORMAT>        Output format: CONSOLE, CSV, JSON, BOTH (default: CONSOLE)
  --progress               Enable progress reporting (default)
  --no-progress            Disable progress reporting
  --help                   Show help message
```

### Examples
```bash
# Run with custom scenario and JSON output
java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario HEAVY_TRANSFERS \
    --output JSON \
    --progress

# Quick test without progress reporting
java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario READ_HEAVY \
    --no-progress
```

## 🎉 Enhanced Features

### 📈 Advanced Metrics Collection
- **Response Time Analysis**: P50, P95, P99 percentiles with full distribution
- **Throughput Tracking**: Real-time throughput with historical sampling
- **Error Categorization**: Network, server, validation, and business logic errors
- **Operation Metrics**: Per-operation response times and success rates
- **Statistical Analysis**: Mean, median, standard deviation calculations

### 📊 Multiple Output Formats
- **CSV Export**: Spreadsheet-compatible for Excel/LibreOffice analysis
- **JSON Export**: Structured data for programmatic analysis and dashboards
- **Console Output**: Enhanced real-time progress with detailed statistics

### 📋 Comprehensive Analysis
- **Performance Trends**: Throughput variations over test duration
- **Error Patterns**: Categorized error analysis with root cause identification
- **Workload Characterization**: Operation mix analysis and distribution
- **System Health**: Memory usage, thread utilization, and resource monitoring

### 🚀 Production-Ready Features
- **Configurable Scenarios**: 5 built-in scenarios for different testing needs
- **Progress Reporting**: Real-time statistics during test execution
- **Resource Management**: Proper cleanup and timeout handling
- **Scalability Testing**: Virtual threads for high concurrency

### Examples
```bash
# Run with custom scenario and JSON output
java -cp "../target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario HEAVY_TRANSFERS \
    --output JSON \
    --progress

# Quick test without progress reporting
java -cp "../target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario READ_HEAVY \
    --no-progress
```

## 📈 Metrics Collected

### Core Metrics
- **Total Operations**: Total number of operations performed
- **Success/Failure Rates**: Percentage of successful operations
- **Throughput**: Operations per second
- **Response Times**: Individual operation response times

### Advanced Metrics
- **Percentiles**: P50, P95, P99, P99.9 response times
- **Error Categorization**: Network, server, validation, business logic errors
- **Operation Distribution**: Breakdown by operation type
- **Throughput Trends**: Performance over time
- **Statistical Analysis**: Mean, median, standard deviation

## 📊 Output Formats

### Console Output
Real-time progress and final summary displayed in terminal.

### CSV Format (`stress-test-results.csv`)
```csv
timestamp,operation_type,response_time_ms,status_code,success,error_category
2024-01-15T10:30:15.123,DEPOSIT,45,200,true,
2024-01-15T10:30:15.145,TRANSFER,120,500,false,SERVER_ERROR
```

### JSON Format (`stress-test-results.json`)
```json
{
  "testMetadata": {
    "scenario": "BALANCED_LOAD",
    "startTime": "2024-01-15T10:30:00Z",
    "durationMs": 300000,
    "totalOperations": 1600000
  },
  "summary": {
    "totalOperations": 1600000,
    "successfulOperations": 1584000,
    "failedOperations": 16000,
    "successRate": 99.0,
    "averageThroughput": 5333.33
  },
  "responseTimeStats": {
    "mean": 45.2,
    "median": 38.0,
    "p95": 120.5,
    "p99": 250.8,
    "min": 12.3,
    "max": 5000.0
  },
  "errorBreakdown": {
    "NETWORK_TIMEOUT": 5000,
    "SERVER_ERROR": 8000,
    "VALIDATION_ERROR": 3000
  },
  "operationBreakdown": {
    "DEPOSIT": 640000,
    "WITHDRAWAL": 480000,
    "TRANSFER": 480000
  },
  "throughputSamples": [
    {"timestamp": 1000, "operations": 5333, "throughput": 5333.0},
    {"timestamp": 2000, "operations": 10666, "throughput": 5333.0}
  ]
}
```

## 🔍 Analysis Guide

### 1. Import CSV Data
```bash
# Import into spreadsheet software
libreoffice stress-test-results.csv
# or
excel stress-test-results.csv
```

### 2. Key Metrics to Analyze

#### Performance Analysis
- **Response Time Distribution**: Check P95/P99 percentiles
- **Throughput Trends**: Look for performance degradation over time
- **Error Rates**: Monitor error patterns

#### Bottleneck Identification
- **High P99 Response Times**: Indicate performance outliers
- **Increasing Error Rates**: May indicate resource exhaustion
- **Throughput Drops**: Could indicate locking issues

#### Comparative Analysis
- Compare results across different scenarios
- Analyze impact of thread count on performance
- Evaluate system behavior under different loads

### 3. Common Issues to Look For

#### Concurrency Issues
- Deadlock patterns in logs
- Lock timeout errors
- Inconsistent account balances

#### Performance Issues
- Memory leaks (gradually increasing response times)
- Database connection pool exhaustion
- Thread pool saturation

#### Scalability Issues
- Throughput not scaling with thread count
- Increased error rates under load
- Resource contention

## 🛠️ Troubleshooting

### Common Issues

#### Connection Refused
```
Error: Connection refused: connect
```
**Solution**: Ensure the banking server is running on `http://localhost:8080`

#### Timeout Errors
```
Error: Network timeout
```
**Solution**: Check server performance or increase timeout values

#### Build Errors
```
Error: Could not find or load main class
```
**Solution**: Run `mvn clean compile` from the project root

### Performance Tuning

#### For High Load Testing
- Increase JVM heap size: `-Xmx4g -Xms4g`
- Adjust thread pool sizes
- Monitor system resources

#### For Detailed Analysis
- Enable debug logging: Add `-Dlogging.level.com.bank=DEBUG`
- Use smaller operation counts for detailed tracing
- Enable JVM profiling

## 📁 Project Structure

```
stress-test/
├── src/main/java/com/bank/stress/
│   └── NetworkStressTest.java      # Enhanced stress test class
├── run-smoke-test.sh               # Quick system verification
├── run-balanced-test.sh            # Balanced load test
├── run-read-heavy-test.sh          # Read-heavy workload test
├── run-withdrawal-heavy-test.sh    # Withdrawal performance test
├── run-heavy-transfers-test.sh     # Heavy transfers test
├── run-transfer-stress-test.sh     # Maximum concurrency test
├── run-analysis-test.sh            # Comprehensive analysis test
├── run-all-scenarios.sh            # Complete test suite
├── run-quick-reference.sh          # Help and options reference
└── README.md                       # This documentation
```

## 🎯 Quick Start Guide

### 1. First Time Setup
```bash
# Make sure the banking server is running on localhost:8080
cd /path/to/banking-server
./scripts/run-server.sh

# Run smoke test to verify everything works
cd stress-test
./run-smoke-test.sh
```

### 2. Run Individual Tests
```bash
# Test general system performance
./run-balanced-test.sh

# Test transfer concurrency
./run-transfer-stress-test.sh

# Generate analysis data
./run-analysis-test.sh
```

### 3. Run Complete Test Suite
```bash
# Run all scenarios for comprehensive analysis
./run-all-scenarios.sh
```

### 4. Get Help
```bash
# Show all available options
./run-quick-reference.sh

# Show command-line help
java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" \
  com.bank.stress.NetworkStressTest --help
```

## 🤝 Contributing

When adding new test scenarios or metrics:

1. Update the `TestScenario` enum
2. Add corresponding metrics collection
3. Update output format handling
4. Add documentation and examples
5. Test with multiple scenarios

## 📝 Notes

- All scripts automatically build the project before running
- Test results are saved in the `stress-test/` directory
- Logs are available in `logs/application.log`
- Virtual threads are used for better scalability
- Progress reporting can be disabled for cleaner output

---

**Happy Stress Testing! 🚀**