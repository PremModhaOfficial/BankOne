#!/bin/bash

# Balanced Load Test Script
# Tests the system with a balanced mix of operations (40% deposits, 30% withdrawals, 30% transfers)

echo "=========================================="
echo "🏦 Banking System - Balanced Load Test"
echo "=========================================="
echo "Scenario: BALANCED_LOAD (40% deposits, 30% withdrawals, 30% transfers)"
echo "Output: CSV and JSON formats"
echo "=========================================="

# Build the stress test project
echo "Building stress test project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

# Run the balanced load test
echo "Starting balanced load test..."
java -cp "target/classes:target/dependency/*" com.bank.stress.NetworkStressTest \
    --scenario BALANCED_LOAD \
    --output BOTH \
    --progress

echo "=========================================="
echo "Test completed! Check the following files:"
echo "- stress-test-results.csv (Spreadsheet analysis)"
echo "- stress-test-results.json (Programmatic analysis)"
echo "- logs/application.log (Detailed execution logs)"
echo ""
echo "Files are created in the stress-test directory:"
echo "📁 /home/prem-modha/projects/Motadata/BankOne/Bank/stress-test/"
echo "=========================================="