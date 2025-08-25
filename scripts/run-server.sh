#!/bin/bash

# Script to run the Bank HTTP server

echo "Starting Bank HTTP Server..."

# Compile the project first
echo "Compiling the project..."
mvn compile -X

if [ $? -ne 0 ]; then
    echo "Compilation failed. Please check for errors."
    exit 1
fi

# Create logs directory if it doesn't exist
mkdir -p logs

# Run the server
echo "Running the server..."
echo "Logs will be written to the 'logs/' directory"
mvn exec:java -Dexec.mainClass="com.bank.HttpServer"

echo "Server stopped."
