#!/bin/bash

# Heavy Transfers Test Script
# Tests the system with high transfer load (60% transfers)
# Focuses on transfer performance with some deposits/withdrawals

echo "=========================================="
echo "🏦 Banking System - Heavy Transfers Test"
echo "=========================================="
echo "Scenario: HEAVY_TRANSFERS (20% deposits, 20% withdrawals, 60% transfers)"
echo "Purpose: Test transfer performance with mixed operations"
echo "=========================================="

# Build the project first
echo "Building project..."
cd ../
mvn clean compile -q
cd stress-test

# Build the stress test module
echo "Building stress test module..."
mvn compile -q

# Run the heavy transfers test
echo "Starting heavy transfers test..."
java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario HEAVY_TRANSFERS \
    --output BOTH \
    --progress

echo "=========================================="
echo "Heavy transfers test completed!"
echo "This test evaluates:"
echo "- Transfer performance under load"
echo "- Mixed operation handling"
echo "- Balance consistency across operations"
echo ""
echo "Check the following files:"
echo "- stress-test-results.csv"
echo "- stress-test-results.json"
echo "- logs/application.log (for performance insights)"
echo "=========================================="