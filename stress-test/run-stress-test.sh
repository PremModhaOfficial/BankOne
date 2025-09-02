#!/bin/bash

# Network Stress Test Runner Script
# This script builds and runs the network-based stress test for the Bank application

echo "=========================================="
echo "🏦 Banking System - Network Stress Test"
echo "=========================================="
echo "Default configuration:"
echo "- Scenario: BALANCED_LOAD"
echo "- Output: CONSOLE format"
echo "- Progress reporting enabled"
echo "=========================================="

# Build the stress test project and download dependencies
echo "Building stress test project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

# Run the network stress test
echo "Running network stress test..."
java -cp "target/classes:target/dependency/*" \
    com.bank.stress.NetworkStressTest \
    --scenario BALANCED_LOAD \
    --output CONSOLE \
    --progress

echo "=========================================="
echo "Network stress test completed!"
echo "=========================================="
