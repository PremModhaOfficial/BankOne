#!/bin/bash

echo "Testing account operations without authorization headers..."

# Create a user first
echo "Creating user..."
curl -X POST http://localhost/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "test2@example.com",
    "password": "testpassword"
  }' | jq .

# Login to get the user ID
echo -e "\nLogging in to get user ID..."
LOGIN_RESPONSE=$(curl -X POST http://localhost/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test2@example.com",
    "password": "testpassword"
  }')

USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.user.id')
echo "User ID: $USER_ID"

# Create an account using JSON only (no auth headers)
echo -e "\nCreating account with JSON only..."
curl -X POST http://localhost/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $USER_ID,
    \"initialBalance\": 2000,
    \"type\": \"CHECKING\"
  }" | jq .

# View accounts for user
echo -e "\nViewing accounts for user..."
curl -X GET "http://localhost/accounts?userId=$USER_ID" | jq .

# View specific account (assuming it gets ID 1)
echo -e "\nViewing specific account..."
curl -X GET "http://localhost/accounts/1" | jq .

# Deposit money
echo -e "\nDepositing money..."
curl -X POST http://localhost/accounts/1/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500
  }' | jq .

# Withdraw money
echo -e "\nWithdrawing money..."
curl -X POST http://localhost/accounts/1/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100
  }' | jq .

# Transfer money (create another account first)
echo -e "\nCreating second account for transfer..."
curl -X POST http://localhost/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $USER_ID,
    \"initialBalance\": 0,
    \"type\": \"SAVINGS\"
  }" | jq .

# Transfer between accounts
echo -e "\nTransferring money..."
curl -X POST http://localhost/accounts/1/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "toAccountId": 2,
    "amount": 200
  }' | jq .

# Check final account balances
echo -e "\nChecking final account balances..."
curl -X GET "http://localhost/accounts?userId=$USER_ID" | jq .
