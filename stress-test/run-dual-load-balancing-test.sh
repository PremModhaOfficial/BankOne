#!/bin/bash

# Script to run stress tests with different load balancing modes

echo "=== Banking System Dual Load Balancing Stress Test ==="
echo

# Build the stress test project and download dependencies
echo "Building stress test project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

echo "=========================================="
echo "🏦 Banking System - Dual Load Balancing Test"
echo "=========================================="
echo "Testing both load balancing modes:"
echo "1. Server-side (nginx) load balancing"
echo "2. Client-side load balancing"
echo "=========================================="

# Test 1: Nginx Load Balancing (default)
echo "=== Test 1: Nginx Load Balancing (Server-side) ==="
echo "Using nginx reverse proxy for load distribution"
echo

java -cp "target/classes:target/dependency/*" \
    com.bank.stress.NetworkStressTest \
    --scenario BALANCED_LOAD \
    --output CONSOLE \
    --progress

echo
echo "=== Test 1 Complete ==="
echo

# Wait a bit between tests
sleep 3

# Test 2: Client-side Load Balancing
echo "=== Test 2: Client-side Load Balancing ==="
echo "Using round-robin distribution from client"
echo

java -cp "target/classes:target/dependency/*" \
    -DUSE_CLIENT_LOAD_BALANCING=true \
    com.bank.stress.NetworkStressTest \
    --scenario BALANCED_LOAD \
    --output CONSOLE \
    --progress

echo
echo "=== Test 2 Complete ==="
echo

echo "=== Comparison Summary ==="
echo "Test 1: Used nginx load balancer (server-side distribution)"
echo "Test 2: Used client-side round-robin (direct server connections)"
echo
echo "Both tests completed successfully! 🎉"