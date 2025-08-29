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

# Build the stress test project
echo "Building stress test project..."
mvn clean compile -q

# Run the transfer stress test with higher thread count
echo "Starting transfer stress test..."
java -cp "../target/classes:target/classes:target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario STRESS_TRANSFERS \
    --output BOTH \
    --progress

# Return to stress-test directory for file operations
cd stress-test

echo "=========================================="
echo "Transfer stress test completed!"
echo "This test specifically targets:"
echo "- Concurrent transfer operations"
echo "- Lock contention and deadlocks"
echo "- Database transaction performance"
echo ""
echo "Check the following files:"
echo "- stress-test-results.csv (Spreadsheet analysis)"
echo "- stress-test-results.json (Programmatic analysis)"
echo "- logs/application.log (Detailed execution logs)"
echo ""
echo "Files are created in the stress-test directory:"
echo "📁 /home/prem-modha/projects/Motadata/BankOne/Bank/stress-test/"
echo "=========================================="