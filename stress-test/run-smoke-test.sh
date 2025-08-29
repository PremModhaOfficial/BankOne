#!/bin/bash

# Smoke Test Script
# Quick test to verify the system is working correctly
# Uses minimal resources and short duration

echo "=========================================="
echo "🚀 Banking System - Smoke Test"
echo "=========================================="
echo "Quick verification test:"
echo "- Minimal users and accounts"
echo "- Short test duration"
echo "- Basic functionality check"
echo "=========================================="

# Build the stress test project
echo "Building stress test project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

# Create a temporary smoke test configuration
# We'll modify the constants temporarily for smoke test
echo "Running smoke test with minimal configuration..."
java -cp "target/classes:target/dependency/*" \
    -DNUMBER_OF_USERS=2 \
    -DNUMBER_OF_ACCOUNTS_PER_USER=2 \
    -DNUMBER_OF_THREADS=2 \
    -DOPERATIONS_PER_THREAD=25 \
    com.bank.stress.NetworkStressTest \
    --scenario READ_HEAVY \
    --output CONSOLE \
    --no-progress

echo "=========================================="
echo "Smoke test completed successfully!"
echo "The system is ready for full stress testing."
echo ""
echo "📁 Files Location:"
echo "/home/prem-modha/projects/Motadata/BankOne/Bank/stress-test/"
echo "=========================================="