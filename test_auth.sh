#!/bin/bash

echo "Testing user creation and authentication..."

# Create a user
echo "Creating user..."
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "admin": false
  }' | jq .

echo -e "\nLogging in..."
# Login with the user
LOGIN_RESPONSE=$(curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test@example.com",
    "password": "testpassword"
  }')

echo $LOGIN_RESPONSE | jq .

# Extract the token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo -e "\nToken: $TOKEN"

echo -e "\nCreating account..."
# Create an account using the token
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": 1,
    "accountNumber": "ACC123456",
    "initialBalance": 1000,
    "type": "SAVINGS"
  }' | jq .