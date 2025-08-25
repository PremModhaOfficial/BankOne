#!/bin/bash

echo "Testing error handling..."

# Test invalid user ID
echo "Testing invalid user ID..."
curl -X GET http://localhost:8080/users/invalid | jq .

# Test non-existent user
echo -e "\nTesting non-existent user..."
curl -X GET http://localhost:8080/users/999 | jq .

# Test invalid account ID
echo -e "\nTesting invalid account ID..."
curl -X GET http://localhost:8080/accounts/invalid | jq .

# Test non-existent account
echo -e "\nTesting non-existent account..."
curl -X GET http://localhost:8080/accounts/999 | jq .

# Test account access without user ID
echo -e "\nTesting account access without user ID..."
curl -X GET http://localhost:8080/accounts | jq .