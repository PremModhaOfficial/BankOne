#!/bin/bash

# Quick Test Script
# Minimal test to verify everything is working

echo "=========================================="
echo "🚀 Quick Banking Stress Test"
echo "=========================================="
echo "This will run a very small test to verify:"
echo "- Bank server is running on localhost:8080"
echo "- Stress test can connect and run"
echo "- Basic functionality works"
echo "=========================================="

# Build the project and download dependencies
echo "Building project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

# Run minimal test
echo "Running quick test..."
java -cp "target/classes:target/dependency/*" \
    -DNUMBER_OF_USERS=1 \
    -DNUMBER_OF_ACCOUNTS_PER_USER=1 \
    -DNUMBER_OF_THREADS=1 \
    -DOPERATIONS_PER_THREAD=5 \
    com.bank.stress.NetworkStressTest \
    --scenario READ_HEAVY \
    --output CONSOLE \
    --no-progress

echo "=========================================="
echo "Quick test completed!"
echo "If you see successful operations above, everything is working!"
echo "=========================================="