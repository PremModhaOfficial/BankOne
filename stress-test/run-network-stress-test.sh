#!/bin/bash

# Network Stress Test Runner Script
# This script runs the network-based stress test for the Bank application

echo "Bank Application Network Stress Test"
echo "==================================="
echo

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "Error: This script must be run from the stress-test directory"
    exit 1
fi

# Check if JAR exists
if [ ! -f "target/stress-test-1.0-SNAPSHOT.jar" ]; then
    echo "Error: Network stress test JAR not found. Please build it first:"
    echo "  mvn clean package"
    exit 1
fi

# Make sure the server is running
echo "Please make sure the Bank server is running on http://localhost:8080"
echo "You can start it by running: cd .. && ./scripts/run-server.sh"
echo
read -p "Press Enter to continue or Ctrl+C to cancel..."

# Run the network stress test
echo
echo "Running network stress test..."
echo "============================"
java -jar target/stress-test-1.0-SNAPSHOT.jar

echo
echo "Network stress test completed."