#!/bin/bash

# Script to run the Bank HTTP client

echo "Starting Bank HTTP Client..."

# Compile the project first
echo "Compiling the project..."
mvn compile

if [ $? -ne 0 ]; then
    echo "Compilation failed. Please check for errors."
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p logs

# Run the client
echo "Running the client..."
echo "Logs will be written to the 'logs/' directory"
mvn exec:java -Dexec.mainClass="com.bank.HttpClient"

echo "Client stopped."
