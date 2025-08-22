#!/bin/bash

# Stress Test Runner Script
# This script builds and runs the stress test for the Bank application

echo "Bank Application Stress Test"
echo "============================"
echo

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "Error: This script must be run from the stress-test directory"
    exit 1
fi

# Build the main Bank application first
echo "Building main Bank application..."
cd ..
mvn clean install -DskipTests
if [ $? -ne 0 ]; then
    echo "Error: Failed to build main Bank application"
    exit 1
fi
cd stress-test

# Build the stress test
echo
echo "Building stress test..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "Error: Failed to build stress test"
    exit 1
fi

# Check if JAR was created
if [ ! -f "target/stress-test-1.0-SNAPSHOT.jar" ]; then
    echo "Error: Stress test JAR not found"
    exit 1
fi

# Run the stress test
echo
echo "Running stress test..."
echo "====================="
java -jar target/stress-test-1.0-SNAPSHOT.jar

echo
echo "Stress test completed."