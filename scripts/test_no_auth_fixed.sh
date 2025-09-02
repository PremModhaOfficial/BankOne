#!/bin/bash

echo "Testing account operations without authorization headers..."

# Create a user first
echo "Creating user..."
curl -X POST http://localhost/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "email": "test3@example.com",
    "password": "testpassword"
  }' | jq .

# Login to get the user ID
echo -e "\nLogging in to get user ID..."
LOGIN_RESPONSE=$(curl -X POST http://localhost/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "test3@example.com",
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
    \"initialBalance\": 3000,
    \"type\": \"CHECKING\"
  }" | jq .

# View accounts for user
echo -e "\nViewing accounts for user..."
curl -X GET "http://localhost/accounts?userId=$USER_ID" | jq .

# Check what accounts exist
echo -e "\nChecking what accounts exist..."
curl -X GET "http://localhost/accounts?userId=$USER_ID" | jq .

# Now we know the account ID is 1, let's access it directly
echo -e "\nViewing specific account by ID..."
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

# Create another account for transfer
echo -e "\nCreating second account for transfer..."
curl -X POST http://localhost/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": $USER_ID,
    \"initialBalance\": 0,
    \"type\": \"SAVINGS\"
  }" | jq .

# Check what accounts exist now
echo -e "\nChecking accounts after creating second one..."
curl -X GET "http://localhost/accounts?userId=$USER_ID" | jq .

# Transfer between accounts (assuming the new account has ID 2)
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
