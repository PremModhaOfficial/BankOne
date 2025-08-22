#!/bin/bash

echo "Testing account operations without authorization headers..."

# Create a user first
echo "Creating user..."
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "test2@example.com",
    "admin": false
  }' | jq .

# Create an account using JSON only (no auth headers)
echo -e "\nCreating account with JSON only..."
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "accountNumber": "ACC789012",
    "initialBalance": 2000,
    "type": "CHECKING"
  }' | jq .

# View accounts for user
echo -e "\nViewing accounts for user..."
curl -X GET "http://localhost:8080/accounts?userId=2" | jq .

# View specific account
echo -e "\nViewing specific account..."
curl -X GET "http://localhost:8080/accounts/2" | jq .

# Deposit money
echo -e "\nDepositing money..."
curl -X POST http://localhost:8080/accounts/2/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500
  }' | jq .

# Withdraw money
echo -e "\nWithdrawing money..."
curl -X POST http://localhost:8080/accounts/2/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100
  }' | jq .

# Transfer money (create another account first)
echo -e "\nCreating second account for transfer..."
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "accountNumber": "ACC345678",
    "initialBalance": 0,
    "type": "SAVINGS"
  }' | jq .

# Transfer between accounts
echo -e "\nTransferring money..."
curl -X POST http://localhost:8080/accounts/2/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "toAccountId": 3,
    "amount": 200
  }' | jq .

# Check final account balances
echo -e "\nChecking final account balances..."
curl -X GET "http://localhost:8080/accounts?userId=2" | jq .
