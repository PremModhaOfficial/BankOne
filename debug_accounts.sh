#!/bin/bash

echo "Debugging account access..."

# Test with verbose output to see headers
echo "Testing with verbose curl output..."
curl -v -X GET "http://localhost:8080/accounts" \
  -H "Authorization: Bearer 1:test@example.com:testpassword" \
  -H "Content-Type: application/json"

echo -e "\n\nTesting without authorization header..."
curl -v -X GET "http://localhost:8080/accounts" \
  -H "Content-Type: application/json"