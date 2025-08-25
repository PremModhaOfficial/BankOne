#!/bin/bash

# Start the server in the background
echo "Starting server..."
java -jar target/Bank-1.0-SNAPSHOT.jar > server.log 2>&1 &
SERVER_PID=$!

# Give the server time to start
sleep 5

# Test the server is running
echo "Testing server connectivity..."
RESPONSE=$(curl -s http://localhost:8080)
if [ "$RESPONSE" = "PONG" ]; then
    echo "Server is running successfully"
else
    echo "Server failed to start properly"
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

# Test user registration
echo "Testing user registration..."
REGISTRATION_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}')

# Extract response body and status code
REGISTRATION_STATUS=${REGISTRATION_RESPONSE: -3}
if [ "$REGISTRATION_STATUS" = "201" ]; then
    echo "User registration successful"
else
    echo "User registration failed with status: $REGISTRATION_STATUS"
fi

# Test user login
echo "Testing user login..."
LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"testuser","password":"password123"}')

LOGIN_STATUS=${LOGIN_RESPONSE: -3}
if [ "$LOGIN_STATUS" = "200" ]; then
    echo "User login successful"
else
    echo "User login failed with status: $LOGIN_STATUS"
fi

# Kill the server
echo "Stopping server..."
kill $SERVER_PID 2>/dev/null

echo "API tests completed"