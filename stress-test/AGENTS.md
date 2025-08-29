# Agent Guidelines for Banking Stress Test Project

## Overview
The Stress Test Agent is an intelligent, automated testing system designed to evaluate the performance, scalability, and reliability of the Banking System under various load conditions. It provides comprehensive metrics collection, real-time analysis, and detailed reporting capabilities.

## Build & Test Commands

### Build Commands
- **Compile project**: `mvn clean compile`
- **Build JAR**: `mvn clean package` (includes shade plugin for fat JAR)
- **Full clean build**: `mvn clean install`

### Test Commands
- **Run single stress test**: `java -cp "target/classes:target/dependency/*" com.bank.stress.NetworkStressTest --scenario BALANCED_LOAD`
- **Run smoke test**: `./run-smoke-test.sh`
- **Run simple stress test**: `./run-simple-stress-test.sh --scenario BALANCED_LOAD --users 5 --accounts 10 --operations 1000`
- **Run specific scenario**:
  - Balanced load: `./run-balanced-test.sh`
  - Read heavy: `./run-read-heavy-test.sh`
  - Withdrawal heavy: `./run-withdrawal-heavy-test.sh`
  - Heavy transfers: `./run-heavy-transfers-test.sh`
  - Transfer stress: `./run-transfer-stress-test.sh`
- **Run all scenarios**: `./run-all-scenarios.sh`

### Single Test Execution
```bash
# Run a quick test with minimal resources
java -cp "target/classes:target/dependency/*" \
  -DNUMBER_OF_USERS=1 \
  -DNUMBER_OF_ACCOUNTS_PER_USER=1 \
  -DNUMBER_OF_THREADS=1 \
  -DOPERATIONS_PER_THREAD=10 \
  com.bank.stress.NetworkStressTest \
  --scenario READ_HEAVY \
  --no-progress
```

### Advanced Usage
```bash
# Custom scenario with specific parameters
java -cp "target/classes:target/dependency/*" \
  com.bank.stress.NetworkStressTest \
  --scenario HEAVY_TRANSFERS \
  --output BOTH \
  --progress

# Quick performance check
java -cp "target/classes:target/dependency/*" \
  com.bank.stress.NetworkStressTest \
  --scenario READ_HEAVY \
  --no-progress
```

### Available Scripts Status
| Script | Status | Notes |
|--------|--------|-------|
| `run-smoke-test.sh` | ✅ Working | Quick verification test |
| `run-simple-stress-test.sh` | ✅ Working | Flexible test runner with parameters |
| `run-balanced-test.sh` | ✅ Working | Fixed classpath issues |
| `run-all-scenarios.sh` | ⚠️ Issues | May hang during concurrent operations |
| Other scenario scripts | ⚠️ Issues | Similar hanging issues |

### Current Known Issues
- **Test Hanging**: Tests may hang during concurrent operations phase
- **Output Files**: CSV/JSON files not generated due to incomplete test execution
- **Workaround**: Use smaller test sizes or individual scenario scripts

## Intelligent Test Scenarios

### Core Scenarios
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

## Code Style Guidelines

### Language & Framework
- **Java Version**: 21 (use modern features like var, virtual threads, records)
- **Build Tool**: Maven
- **Dependencies**: Jackson (JSON), SLF4J (logging), JUnit (if tests exist)
- **Package Structure**: `com.bank.{module}.{submodule}`

### Naming Conventions
- **Packages**: lowercase with dots (e.g., `com.bank.stress`)
- **Classes/Enums**: PascalCase (e.g., `NetworkStressTest`, `TestScenario`)
- **Methods**: camelCase (e.g., `runStressTest()`, `performDeposit()`)
- **Variables**: camelCase (e.g., `httpClient`, `userIds`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `SERVER_URL`, `NUMBER_OF_USERS`)
- **Fields**: camelCase with appropriate visibility modifiers

### Code Structure & Patterns
- **Class Organization**: Group related functionality, use inner classes for supporting types
- **Method Length**: Keep methods focused and under 50 lines when possible
- **Error Handling**: Use try-catch with specific exception types, categorize errors appropriately
- **Resource Management**: Use try-with-resources for AutoCloseable objects
- **Concurrency**: Prefer virtual threads for scalability, use appropriate synchronization
- **Agent Intelligence**: Implement adaptive load patterns and real-time metrics collection

### Documentation & Comments
- **Class Documentation**: Use JavaDoc with description and purpose
- **Method Documentation**: Document complex business logic and parameters
- **Inline Comments**: Explain non-obvious logic, not obvious code
- **TODO/FIXME**: Use sparingly and with context

### Imports & Dependencies
- **Import Style**: Use specific imports, not wildcards
- **Standard Library**: Import java.* packages first, then third-party
- **Static Imports**: Use for constants and utility methods when appropriate
- **Organization**: Group imports by package hierarchy

### Code Quality
- **Exception Handling**: Catch specific exceptions, provide meaningful error messages
- **Null Safety**: Check for null values appropriately, use Optional for nullable returns
- **Performance**: Use efficient data structures, minimize object creation in loops
- **Thread Safety**: Use appropriate concurrent collections and synchronization
- **Logging**: Use SLF4J for logging with appropriate log levels

### Testing Approach
- **Test Types**: Intelligent stress tests with scenario-based operation distribution
- **Test Data**: Use realistic banking data that matches production scenarios
- **Assertions**: Verify both success and failure scenarios with comprehensive error analysis
- **Performance**: Include timing assertions and percentile analysis for performance-critical operations
- **Intelligence Features**: Adaptive load patterns, real-time monitoring, and automated error categorization

### File Organization
- **Source Files**: `src/main/java/com/bank/stress/`
- **Test Files**: `src/test/java/com/bank/stress/` (when added)
- **Resources**: `src/main/resources/`
- **Scripts**: Root directory with descriptive names

## Development Workflow

1. **Setup**: Ensure Java 21+ and Maven 3.6+ are installed
2. **Build**: Run `mvn clean compile` before making changes
3. **Test**: Use smoke test first, then specific scenario tests
4. **Verify**: Check logs and output files for issues
5. **Analyze**: Review generated CSV/JSON reports and performance metrics
6. **Document**: Update README and scripts for new features

## Agent Intelligence Features

### Real-time Intelligence
- **Progress Reporting**: Live statistics during test execution
- **Adaptive Load**: Dynamic operation mix based on scenario requirements
- **Resource Monitoring**: JVM memory and thread utilization tracking
- **Failure Detection**: Automatic error pattern recognition and categorization

### Output Intelligence
- **Multi-format Output**: CSV, JSON, and console reporting
- **Customizable Metrics**: Configurable reporting levels
- **Historical Tracking**: Performance trend analysis
- **Comparative Analysis**: Cross-scenario performance comparison

### Data Analysis Intelligence
- **CSV Analysis**: Spreadsheet compatible for Excel/LibreOffice analysis
- **JSON Analysis**: Programmatic access for custom dashboards and API integration
- **Statistical Analysis**: Automated percentile calculations and trend detection
- **Error Correlation**: Linking errors to specific operation types and system components

## Performance Considerations

- **Memory**: Monitor heap usage, especially with high concurrency and virtual threads
- **Threads**: Use virtual threads for scalability and optimal resource utilization
- **Connections**: Implement proper connection pooling and HTTP client management
- **Metrics**: Collect comprehensive performance data with percentile analysis
- **Cleanup**: Ensure proper resource cleanup and graceful shutdown in all scenarios
- **Scalability**: Design for thousands of concurrent operations using virtual threads

## Security Notes

- **Credentials**: Never hardcode sensitive information in test configurations
- **Input Validation**: Validate all user inputs and API responses thoroughly
- **Error Messages**: Don't expose sensitive system information in errors or logs
- **Logging**: Be cautious about logging sensitive banking data during testing
- **Test Data**: Use realistic but non-sensitive test data for banking operations

## Troubleshooting & Best Practices

### Common Issues
- **Class Not Found**: Ensure both Bank and stress-test projects are compiled
- **Connection Refused**: Verify Bank server is running on port 8080
- **Out of Memory**: Increase JVM heap size for large-scale testing
- **Slow Performance**: Check network connectivity and server resources
- **Test Hanging**: Tests may hang during concurrent operations (use smaller sizes)
- **No Output Files**: CSV/JSON files not generated if test doesn't complete
- **Dependency Issues**: Run `mvn dependency:copy-dependencies` to download JARs

### Best Practices
- **Start Small**: Begin with smoke tests before full-scale testing
- **Monitor Resources**: Watch system resources during testing
- **Analyze Results**: Always review generated reports and logs
- **Iterate**: Use results to optimize system performance
- **Scenario Selection**: Choose appropriate scenarios based on testing goals
- **Use Simple Scripts**: Prefer `run-simple-stress-test.sh` for reliable execution
- **Check Server Status**: Ensure bank server is responding before running tests

### Test Execution Tips
- **For Quick Testing**: Use `./run-smoke-test.sh` or small parameter sets
- **For Reliable Results**: Use `./run-simple-stress-test.sh` with controlled parameters
- **For Debugging**: Run with `--no-progress` to see detailed output
- **For Output Files**: Ensure tests complete successfully before expecting files

## Agent Benefits

### For Developers
- **Quick Feedback**: Rapid performance validation and issue detection
- **Regression Prevention**: Automated performance regression testing
- **Optimization Guidance**: Data-driven performance improvement insights

### For Operations
- **Capacity Planning**: Understand system limits and scaling requirements
- **Performance Monitoring**: Continuous performance validation
- **Incident Analysis**: Detailed performance data for troubleshooting

### For Business
- **Confidence Building**: Validate system performance before deployment
- **Risk Mitigation**: Identify performance issues before they affect users
- **User Experience**: Ensure optimal performance for end users

## Current Project Status

### ✅ Working Components
- **Project Structure**: Well-organized Maven project with Java 21
- **Dependencies**: Jackson, SLF4J properly configured
- **Build System**: Maven compilation and dependency management working
- **Basic Functionality**: User/account creation, HTTP client communication
- **Script Infrastructure**: Multiple test scripts available and executable

### ⚠️ Known Issues
- **Test Completion**: Tests may hang during concurrent operations phase
- **Output Generation**: CSV/JSON files not created due to incomplete execution
- **Scalability**: Large-scale tests may encounter performance issues
- **Error Handling**: Some transient network errors during account creation

### 🔧 Recent Fixes
- **Classpath Issues**: Fixed incorrect paths in test scripts
- **Dependency Resolution**: Added proper dependency downloading
- **Script Corrections**: Updated all scripts to work from current directory
- **Build Process**: Ensured proper compilation and JAR generation

### 📊 Test Results Summary
- **User/Account Creation**: ✅ Successfully creates test data
- **HTTP Communication**: ✅ Connects to bank server on localhost:8080
- **Concurrent Operations**: ⚠️ May hang with larger test sizes
- **Output Files**: ❌ Not generated due to incomplete test execution
- **Error Recovery**: ⚠️ Some transient connection errors observed

## Future Intelligence Enhancements

### Planned Features
- **Machine Learning Integration**: Predictive performance analysis
- **Automated Optimization**: Self-tuning test configurations
- **Cloud Integration**: Multi-region distributed testing
- **Real-time Dashboards**: Live performance monitoring interfaces
- **AI-Powered Insights**: Automated bottleneck identification

### Immediate Improvements
- **Test Stability**: Fix hanging issues in concurrent operations
- **Output Reliability**: Ensure CSV/JSON files are always generated
- **Error Recovery**: Improve handling of transient network errors
- **Performance Monitoring**: Add better resource utilization tracking

---

*This file should be updated when new coding standards or tools are adopted.*