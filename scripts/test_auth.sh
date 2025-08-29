#!/bin/bash

echo "Testing user creation and authentication..."

# Create a user
echo "Creating user..."
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123"
  }' | jq .

echo -e "\nLogging in..."
# Login with the user
LOGIN_RESPONSE=$(curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123"
  }')

echo $LOGIN_RESPONSE | jq .

# Extract the user ID
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.user.id')
echo -e "\nUser ID: $USER_ID"

echo -e "\nCreating account..."
# Create an account using the user ID
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $USER_ID,
    \"initialBalance\": 1000,
    \"type\": \"SAVINGS\"
  }" | jq .