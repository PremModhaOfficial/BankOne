#!/bin/bash

# Script to run the Bank HTTP server

echo "Starting Bank HTTP Server..."

# Compile the project first
echo "Compiling the project..."
mvn compile

if [ $? -ne 0 ]; then
    echo "Compilation failed. Please check for errors."
    exit 1
fi

# Run the server
echo "Running the server..."
mvn exec:java -Dexec.mainClass="com.bank.HttpServer"

echo "Server stopped."