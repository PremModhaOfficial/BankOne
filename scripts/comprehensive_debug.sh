#!/bin/bash

echo "Comprehensive account access debugging..."

echo "1. Verifying user exists:"
curl -X GET "http://localhost:8080/users/1" | jq .

echo -e "\n2. Verifying account exists:"
curl -X GET "http://localhost:8080/accounts/1" \
  -H "Authorization: Bearer 1:test@example.com:testpassword" | jq .

echo -e "\n3. Testing account listing with correct auth:"
curl -X GET "http://localhost:8080/accounts" \
  -H "Authorization: Bearer 1:test@example.com:testpassword" | jq .

echo -e "\n4. Testing account listing with incorrect user ID in token:"
curl -X GET "http://localhost:8080/accounts" \
  -H "Authorization: Bearer 2:test@example.com:testpassword" | jq .

echo -e "\n5. Testing account listing with incorrect email in token:"
curl -X GET "http://localhost:8080/accounts" \
  -H "Authorization: Bearer 1:wrong@example.com:testpassword" | jq .

echo -e "\n6. Testing account listing with malformed token:"
curl -X GET "http://localhost:8080/accounts" \
  -H "Authorization: Bearer invalid-token" | jq .

echo -e "\n7. Testing account listing with missing auth header:"
curl -X GET "http://localhost:8080/accounts" | jq .
