#!/bin/bash

# Start the server in the background
echo "Starting server..."
java -cp target/Bank-1.0-SNAPSHOT-jar-with-dependencies.jar com.bank.HttpServer > server.log 2>&1 &
SERVER_PID=$!

# Give the server time to start
sleep 5

# Test the server is running
echo "Testing server connectivity..."
curl -s http://localhost:8080 > /dev/null
if [ $? -eq 0 ]; then
    echo "Server is running successfully"
else
    echo "Server failed to start"
    kill $SERVER_PID
    exit 1
fi

# Kill the server
echo "Stopping server..."
kill $SERVER_PID

echo "Test completed"