#!/bin/bash

echo "Comprehensive account access debugging..."

echo "1. Verifying user exists:"
curl -X GET "http://localhost:8080/users/1" | jq .

echo -e "\n2. Verifying account exists:"
curl -X GET "http://localhost:8080/accounts/1" | jq .

echo -e "\n3. Testing account listing with user ID in query parameter:"
curl -X GET "http://localhost:8080/accounts?userId=1" | jq .

echo -e "\n4. Testing account listing with user ID in request body:"
curl -X GET "http://localhost:8080/accounts" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}' | jq .

echo -e "\n5. Testing account listing with non-existent user ID:"
curl -X GET "http://localhost:8080/accounts?userId=999" | jq .

echo -e "\n6. Testing account listing without user ID:"
curl -X GET "http://localhost:8080/accounts" | jq .

echo -e "\n7. Testing account listing with invalid user ID format:"
curl -X GET "http://localhost:8080/accounts?userId=invalid" | jq .

