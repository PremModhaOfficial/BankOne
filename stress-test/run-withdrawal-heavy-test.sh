#!/bin/bash

# Withdrawal-Heavy Test Script
# Tests the system with high withdrawal operations (80% withdrawals)
# Tests withdrawal performance and account balance management

echo "=========================================="
echo "🏦 Banking System - Withdrawal-Heavy Test"
echo "=========================================="
echo "Scenario: WITHDRAWAL_HEAVY (10% deposits, 80% withdrawals, 10% transfers)"
echo "Purpose: Test withdrawal performance and balance validation"
echo "=========================================="

# Build the project first
echo "Building project..."
cd ../
mvn clean compile -q
cd stress-test

# Build the stress test module
echo "Building stress test module..."
mvn compile -q

# Run the withdrawal-heavy test
echo "Starting withdrawal-heavy test..."
java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario WITHDRAWAL_HEAVY \
    --output BOTH \
    --progress

echo "=========================================="
echo "Withdrawal-heavy test completed!"
echo "This test evaluates:"
echo "- Withdrawal operation performance"
echo "- Insufficient funds handling"
echo "- Account balance consistency"
echo ""
echo "Check the following files:"
echo "- stress-test-results.csv"
echo "- stress-test-results.json"
echo "- logs/application.log (for validation errors)"
echo "=========================================="