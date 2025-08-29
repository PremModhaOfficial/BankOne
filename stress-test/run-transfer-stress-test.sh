#!/bin/bash

# Transfer Stress Test Script
# Tests the system with heavy transfer load (100% transfers)
# This is the most demanding test for concurrency and locking

echo "=========================================="
echo "🏦 Banking System - Transfer Stress Test"
echo "=========================================="
echo "Scenario: STRESS_TRANSFERS (100% transfers)"
echo "This test focuses on transfer operations to stress-test"
echo "the concurrency control and locking mechanisms"
echo "=========================================="

# Build the project first
echo "Building project..."
cd ../
mvn clean compile -q
cd stress-test

# Build the stress test module
echo "Building stress test module..."
mvn compile -q

# Run the transfer stress test with higher thread count
echo "Starting transfer stress test..."
java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario STRESS_TRANSFERS \
    --output BOTH \
    --progress

echo "=========================================="
echo "Transfer stress test completed!"
echo "This test specifically targets:"
echo "- Concurrent transfer operations"
echo "- Lock contention and deadlocks"
echo "- Database transaction performance"
echo ""
echo "Check the following files:"
echo "- stress-test-results.csv"
echo "- stress-test-results.json"
echo "- logs/application.log (for concurrency issues)"
echo "=========================================="