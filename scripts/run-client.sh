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

# Run the client
echo "Running the client..."
mvn exec:java -Dexec.mainClass="com.bank.HttpClient"

echo "Client stopped."