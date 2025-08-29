#!/bin/bash

# Read-Heavy Test Script
# Tests the system with high read operations (80% deposits)
# Simulates read-heavy workloads like balance checking

echo "=========================================="
echo "🏦 Banking System - Read-Heavy Test"
echo "=========================================="
echo "Scenario: READ_HEAVY (80% deposits, 10% withdrawals, 10% transfers)"
echo "Purpose: Test read-heavy workloads and system responsiveness"
echo "=========================================="

# Build the stress test project
echo "Building stress test project..."
mvn clean compile -q

# Run the read-heavy test
echo "Starting read-heavy test..."
java -cp "../target/classes:target/classes:target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario READ_HEAVY \
    --output BOTH \
    --progress

# Return to stress-test directory for file operations
cd stress-test

echo "=========================================="
echo "Read-heavy test completed!"
echo "This test evaluates:"
echo "- System responsiveness under read load"
echo "- Deposit operation performance"
echo "- Overall system stability"
echo ""
echo "Check the following files:"
echo "- stress-test-results.csv"
echo "- stress-test-results.json"
echo "- logs/application.log (for performance metrics)"
echo "=========================================="