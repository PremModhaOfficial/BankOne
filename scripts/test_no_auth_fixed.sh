#!/bin/bash

echo "Testing account operations without authorization headers..."

# Create a user first
echo "Creating user..."
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser3",
    "email": "test3@example.com",
    "admin": false
  }' | jq .

# Create an account using JSON only (no auth headers)
echo -e "\nCreating account with JSON only..."
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "accountNumber": "ACC999999",
    "initialBalance": 3000,
    "type": "CHECKING"
  }' | jq .

# View accounts for user
echo -e "\nViewing accounts for user..."
curl -X GET "http://localhost:8080/accounts?userId=3" | jq .

# Check what accounts exist
echo -e "\nChecking what accounts exist..."
curl -X GET "http://localhost:8080/accounts?userId=3" | jq .

# Now we know the account ID is 3, let's access it directly
echo -e "\nViewing specific account by ID..."
curl -X GET "http://localhost:8080/accounts/3" | jq .

# Deposit money
echo -e "\nDepositing money..."
curl -X POST http://localhost:8080/accounts/3/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500
  }' | jq .

# Withdraw money
echo -e "\nWithdrawing money..."
curl -X POST http://localhost:8080/accounts/3/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100
  }' | jq .

# Create another account for transfer
echo -e "\nCreating second account for transfer..."
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 3,
    "accountNumber": "ACC888888",
    "initialBalance": 0,
    "type": "SAVINGS"
  }' | jq .

# Check what accounts exist now
echo -e "\nChecking accounts after creating second one..."
curl -X GET "http://localhost:8080/accounts?userId=3" | jq .

# Transfer between accounts (assuming the new account has ID 4)
echo -e "\nTransferring money..."
curl -X POST http://localhost:8080/accounts/3/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "toAccountId": 4,
    "amount": 200
  }' | jq .

# Check final account balances
echo -e "\nChecking final account balances..."
curl -X GET "http://localhost:8080/accounts?userId=3" | jq .