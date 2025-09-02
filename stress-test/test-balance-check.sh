#!/bin/bash

# Test script to verify balance checking fixes insufficient funds errors

echo "=== Testing Balance Check Fixes ==="
echo

# Build the stress test project and download dependencies
echo "Building stress test project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

echo "=========================================="
echo "🏦 Banking System - Balance Check Test"
echo "=========================================="
echo "Testing balance checking with reduced operation amounts"
echo "This should show fewer 'insufficient funds' errors"
echo "=========================================="

# Run a quick test with smaller amounts
java -cp "target/classes:target/dependency/*" \
  -DNUMBER_OF_USERS=2 \
  -DNUMBER_OF_ACCOUNTS_PER_USER=2 \
  -DNUMBER_OF_THREADS=2 \
  -DOPERATIONS_PER_THREAD=50 \
  com.bank.stress.NetworkStressTest \
  --scenario BALANCED_LOAD \
  --output CONSOLE \
  --progress

echo
echo "=== Test Complete ==="
echo "Check the results above for:"
echo "- Reduced insufficient funds errors"
echo "- Operations skipped due to low balance"
echo "- Better overall success rate"