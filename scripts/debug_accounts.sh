#!/bin/bash

echo "Debugging account access..."

# Test with verbose output to see headers
echo "Testing with user ID in query parameter..."
curl -v -X GET "http://localhost/accounts?userId=1" \
  -H "Content-Type: application/json"

echo -e "\n\nTesting with user ID in request body..."
curl -v -X GET "http://localhost/accounts" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'

echo -e "\n\nTesting without user ID..."
curl -v -X GET "http://localhost/accounts" \
  -H "Content-Type: application/json"