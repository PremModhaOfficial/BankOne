# 🤖 Stress Test Agent Documentation

## Overview

The Stress Test Agent is an intelligent, automated testing system designed to evaluate the performance, scalability, and reliability of the Banking System under various load conditions. It provides comprehensive metrics collection, real-time analysis, and detailed reporting capabilities.

## 🎯 Core Capabilities

### Intelligent Test Scenarios
- **BALANCED_LOAD**: 40% deposits, 30% withdrawals, 30% transfers - General system evaluation
- **HEAVY_TRANSFERS**: 20% deposits, 20% withdrawals, 60% transfers - Transfer performance analysis
- **READ_HEAVY**: 80% deposits, 10% withdrawals, 10% transfers - Read-heavy workload testing
- **STRESS_TRANSFERS**: 100% transfers - Maximum concurrency stress testing
- **WITHDRAWAL_HEAVY**: 10% deposits, 80% withdrawals, 10% transfers - Withdrawal validation testing

### Advanced Metrics Collection
- **Response Time Analysis**: P50, P95, P99 percentiles with full distribution
- **Throughput Monitoring**: Real-time throughput with historical sampling
- **Error Categorization**: Network, server, validation, business logic error classification
- **Operation Metrics**: Per-operation response times and success rates
- **Statistical Analysis**: Mean, median, standard deviation calculations

### Real-time Intelligence
- **Progress Reporting**: Live statistics during test execution
- **Adaptive Load**: Dynamic operation mix based on scenario requirements
- **Resource Monitoring**: JVM memory and thread utilization tracking
- **Failure Detection**: Automatic error pattern recognition and categorization

## 🚀 Quick Start Guide

### Prerequisites
- Java 21+ runtime environment
- Maven 3.6+ build system
- Running Banking System server on `http://localhost:8080`
- Compiled main Bank project (separate compilation required)

### Basic Usage
```bash
# Navigate to stress test directory
cd /path/to/bank/stress-test

# Run smoke test (recommended first)
./run-smoke-test.sh

# Run specific scenario
./run-balanced-test.sh

# Run complete test suite
./run-all-scenarios.sh
```

### Advanced Usage
```bash
# Custom scenario with specific parameters
java -cp "../target/classes:target/classes:target/dependency/*" \
  com.bank.stress.NetworkStressTest \
  --scenario HEAVY_TRANSFERS \
  --output BOTH \
  --progress

# Quick performance check
java -cp "../target/classes:target/classes:target/dependency/*" \
  com.bank.stress.NetworkStressTest \
  --scenario READ_HEAVY \
  --no-progress
```

## 📊 Agent Intelligence Features

### 1. Scenario Intelligence
The agent automatically selects optimal operation distributions based on test scenarios:

- **Load Balancing**: Distributes operations across available accounts
- **Concurrency Management**: Virtual threads for efficient resource utilization
- **Error Recovery**: Automatic retry mechanisms for transient failures
- **Resource Optimization**: Adaptive thread pool sizing

### 2. Metrics Intelligence
- **Statistical Analysis**: Automated calculation of performance percentiles
- **Trend Detection**: Throughput variation analysis over time
- **Anomaly Detection**: Identification of performance outliers
- **Correlation Analysis**: Linking errors to specific operation types

### 3. Reporting Intelligence
- **Multi-format Output**: CSV, JSON, and console reporting
- **Customizable Metrics**: Configurable reporting levels
- **Historical Tracking**: Performance trend analysis
- **Comparative Analysis**: Cross-scenario performance comparison

## 🎯 Test Scenarios & Intelligence

### BALANCED_LOAD Scenario
**Intelligence**: Simulates typical banking workload patterns
- **Operation Mix**: 40% deposits, 30% withdrawals, 30% transfers
- **Use Case**: General system performance evaluation
- **Intelligence**: Balances read/write operations for realistic testing

### HEAVY_TRANSFERS Scenario
**Intelligence**: Focuses on concurrent transfer operations
- **Operation Mix**: 20% deposits, 20% withdrawals, 60% transfers
- **Use Case**: Transfer performance and concurrency testing
- **Intelligence**: Stress-tests locking mechanisms and transaction handling

### READ_HEAVY Scenario
**Intelligence**: Optimizes for read-heavy workloads
- **Operation Mix**: 80% deposits, 10% withdrawals, 10% transfers
- **Use Case**: Balance checking and read performance
- **Intelligence**: Minimizes write contention for pure read performance

### STRESS_TRANSFERS Scenario
**Intelligence**: Maximum concurrency stress testing
- **Operation Mix**: 100% transfers
- **Use Case**: Concurrency limits and deadlock detection
- **Intelligence**: Identifies system breaking points under extreme load

### WITHDRAWAL_HEAVY Scenario
**Intelligence**: Focuses on withdrawal validation and limits
- **Operation Mix**: 10% deposits, 80% withdrawals, 10% transfers
- **Use Case**: Withdrawal performance and balance validation
- **Intelligence**: Tests insufficient funds handling and validation logic

## 📈 Performance Analysis Intelligence

### Response Time Intelligence
- **Percentile Analysis**: P50, P95, P99 for latency distribution
- **Outlier Detection**: Identifies performance anomalies
- **Trend Analysis**: Response time changes over test duration
- **Comparative Metrics**: Cross-scenario latency comparison

### Throughput Intelligence
- **Real-time Monitoring**: Live throughput calculations
- **Capacity Analysis**: Maximum sustainable throughput identification
- **Scalability Metrics**: Performance scaling with load
- **Efficiency Tracking**: Operations per resource unit

### Error Intelligence
- **Categorization Engine**: Automatic error type classification
- **Pattern Recognition**: Error frequency and distribution analysis
- **Root Cause Analysis**: Linking errors to system components
- **Recovery Metrics**: Error handling effectiveness measurement

## 🔧 Configuration Intelligence

### Adaptive Configuration
```bash
# Scenario-based automatic configuration
--scenario BALANCED_LOAD    # Auto-configures operation ratios
--scenario STRESS_TRANSFERS # Optimizes for concurrency testing
--scenario READ_HEAVY       # Minimizes write operations
```

### Output Intelligence
```bash
# Multi-format intelligent reporting
--output CONSOLE  # Real-time console display
--output CSV      # Spreadsheet analysis format
--output JSON     # Programmatic analysis format
--output BOTH     # Combined analysis capabilities
```

### Progress Intelligence
```bash
# Smart progress reporting
--progress        # Real-time statistics and progress
--no-progress     # Silent operation for automation
```

## 📊 Data Analysis Intelligence

### CSV Analysis Features
- **Spreadsheet Compatible**: Direct import into Excel/LibreOffice
- **Filtering Capabilities**: Sort and filter by operation type, response time
- **Chart Generation**: Easy visualization of performance metrics
- **Statistical Analysis**: Built-in spreadsheet statistical functions

### JSON Analysis Features
- **Programmatic Access**: Easy parsing with any JSON library
- **Custom Dashboards**: Integration with monitoring systems
- **Automated Reporting**: Generate custom performance reports
- **API Integration**: Feed data to external analysis tools

### Log Analysis Features
- **Detailed Tracing**: Complete operation execution logs
- **Error Correlation**: Link errors to specific operations
- **Performance Profiling**: Identify bottlenecks and slow operations
- **Debug Information**: Comprehensive debugging data

## 🎯 Agent Commands & Intelligence

### Help System
```bash
# Intelligent help system
./run-quick-reference.sh    # Complete usage guide
java ... --help            # Command-line options
```

### Scenario Selection
```bash
# Intelligent scenario selection
./run-balanced-test.sh     # General performance
./run-transfer-stress-test.sh  # Concurrency stress
./run-analysis-test.sh     # Comprehensive analysis
```

### Batch Operations
```bash
# Intelligent batch processing
./run-all-scenarios.sh     # Complete test suite
# Automatically handles scenario sequencing
# Provides comparative analysis
# Generates comprehensive reports
```

## 🚀 Advanced Features

### 1. Virtual Thread Intelligence
- **Resource Efficiency**: Optimal thread utilization
- **Scalability**: Handle thousands of concurrent operations
- **Memory Optimization**: Reduced memory footprint
- **Performance**: Better throughput than traditional threads

### 2. Error Recovery Intelligence
- **Automatic Retry**: Transient failure handling
- **Graceful Degradation**: Continue testing despite errors
- **Error Isolation**: Prevent single failures from affecting entire test
- **Recovery Metrics**: Track recovery effectiveness

### 3. Resource Monitoring Intelligence
- **JVM Metrics**: Memory usage and garbage collection
- **Thread Analysis**: Active thread monitoring
- **System Resources**: CPU and memory utilization
- **Performance Correlation**: Link resource usage to performance

## 📋 Agent Maintenance & Intelligence

### Self-Diagnostic Capabilities
- **Health Checks**: Automatic system health verification
- **Configuration Validation**: Ensure test parameters are valid
- **Resource Assessment**: Verify sufficient resources for testing
- **Dependency Checking**: Validate all required components

### Adaptive Learning
- **Performance Profiling**: Learn optimal configurations
- **Error Pattern Recognition**: Identify recurring issues
- **Optimization Suggestions**: Recommend configuration improvements
- **Historical Analysis**: Compare current results with past runs

## 🎉 Agent Benefits

### For Developers
- **Quick Feedback**: Rapid performance validation
- **Issue Detection**: Early identification of performance problems
- **Regression Prevention**: Automated performance regression testing
- **Optimization Guidance**: Data-driven performance improvement

### For Operations
- **Capacity Planning**: Understand system limits and scaling needs
- **Performance Monitoring**: Continuous performance validation
- **Incident Analysis**: Detailed performance data for troubleshooting
- **SLA Validation**: Ensure performance meets business requirements

### For Business
- **Confidence Building**: Validate system performance before deployment
- **Risk Mitigation**: Identify performance issues before they affect users
- **Cost Optimization**: Right-size infrastructure based on actual needs
- **User Experience**: Ensure optimal performance for end users

## 🔮 Future Intelligence Enhancements

### Planned Features
- **Machine Learning Integration**: Predictive performance analysis
- **Automated Optimization**: Self-tuning test configurations
- **Cloud Integration**: Multi-region distributed testing
- **Real-time Dashboards**: Live performance monitoring interfaces
- **AI-Powered Insights**: Automated bottleneck identification
- **Predictive Scaling**: Anticipate performance requirements

### Intelligence Roadmap
- **Smart Scenario Generation**: AI-generated optimal test scenarios
- **Adaptive Load Patterns**: Dynamic load adjustment based on system response
- **Predictive Failure Analysis**: Anticipate and prevent performance issues
- **Automated Reporting**: Intelligent report generation with insights

---

## 📞 Agent Support

### Getting Help
```bash
# Quick reference guide
./run-quick-reference.sh

# Command-line help
java -cp "../target/classes:target/classes:target/dependency/*" \
  com.bank.stress.NetworkStressTest --help
```

### Troubleshooting
- **Class Not Found**: Ensure both Bank and stress-test projects are compiled
- **Connection Refused**: Verify Bank server is running on port 8080
- **Out of Memory**: Increase JVM heap size for large-scale testing
- **Slow Performance**: Check network connectivity and server resources

### Best Practices
- **Start Small**: Begin with smoke tests before full-scale testing
- **Monitor Resources**: Watch system resources during testing
- **Analyze Results**: Always review generated reports and logs
- **Iterate**: Use results to optimize system performance

---

**🎯 The Stress Test Agent is your intelligent performance validation companion, providing deep insights into system behavior under various load conditions.**