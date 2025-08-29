#!/bin/bash

# Start the server in the background
echo "Starting server..."
java -jar target/Bank-1.0-SNAPSHOT.jar > server.log 2>&1 &
SERVER_PID=$!

# Give the server time to start
sleep 5

# Test the server is running
echo "Testing server connectivity..."
RESPONSE=$(curl -s http://localhost)
if [ "$RESPONSE" = "PONG" ]; then
    echo "Server is running successfully"
else
    echo "Server failed to start properly"
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

# Test user registration
echo "Testing user registration..."
REGISTRATION_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost/users \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}')

# Extract response body and status code
REGISTRATION_STATUS=${REGISTRATION_RESPONSE: -3}
if [ "$REGISTRATION_STATUS" = "201" ]; then
    echo "User registration successful"
else
    echo "User registration failed with status: $REGISTRATION_STATUS"
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

# Test user login
echo "Testing user login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"testuser","password":"password123"}')

echo "Login response: $LOGIN_RESPONSE"

# Try to parse the user details from the response
if echo "$LOGIN_RESPONSE" | grep -q '"user"'; then
    USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.user.id')
    echo "User login successful, user ID: $USER_ID"
else
    echo "User login failed or user details not found in response"
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

# Test account creation
echo "Testing account creation..."
ACCOUNT_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d "{\"userId\":$USER_ID,\"initialBalance\":100.0}")

ACCOUNT_STATUS=${ACCOUNT_RESPONSE: -3}
if [ "$ACCOUNT_STATUS" = "201" ]; then
    echo "Account creation successful"
else
    echo "Account creation failed with status: $ACCOUNT_STATUS"
    echo "Response: ${ACCOUNT_RESPONSE%???}"
    kill $SERVER_PID 2>/dev/null
    exit 1
fi

# Kill the server
echo "Stopping server..."
kill $SERVER_PID 2>/dev/null

echo "All tests completed successfully!"