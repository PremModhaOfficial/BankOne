#!/bin/bash

# Balanced Load Test Script
# Tests the system with a balanced mix of operations (40% deposits, 30% withdrawals, 30% transfers)

echo "=========================================="
echo "🏦 Banking System - Balanced Load Test"
echo "=========================================="
echo "Scenario: BALANCED_LOAD (40% deposits, 30% withdrawals, 30% transfers)"
echo "Output: CSV and JSON formats"
echo "=========================================="

# Build the project first
echo "Building project..."
cd ../
mvn clean compile -q
cd stress-test

# Build the stress test module
echo "Building stress test module..."
mvn compile -q

# Run the balanced load test
echo "Starting balanced load test..."
java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario BALANCED_LOAD \
    --output BOTH \
    --progress

echo "=========================================="
echo "Test completed! Check the following files:"
echo "- stress-test-results.csv"
echo "- stress-test-results.json"
echo "- logs/application.log (for detailed logs)"
echo "=========================================="