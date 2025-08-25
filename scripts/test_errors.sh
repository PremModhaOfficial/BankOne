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
curl -X GET http://localhost:8080/accounts/invalid \
  -H "Authorization: Bearer 1:test@example.com:testpassword" | jq .

# Test non-existent account
echo -e "\nTesting non-existent account..."
curl -X GET http://localhost:8080/accounts/999 \
  -H "Authorization: Bearer 1:test@example.com:testpassword" | jq .

# Test unauthorized access
echo -e "\nTesting unauthorized access..."
curl -X GET http://localhost:8080/accounts/1 | jq .