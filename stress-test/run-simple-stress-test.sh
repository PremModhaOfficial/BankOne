#!/bin/bash

# Simple Stress Test Runner
# Run stress tests from the current directory
# Usage: ./run-simple-stress-test.sh [scenario] [options]

echo "=========================================="
echo "🚀 Banking System - Simple Stress Test Runner"
echo "=========================================="

# Default values
SCENARIO="BALANCED_LOAD"
OUTPUT="CONSOLE"
PROGRESS="--progress"
USERS=5
ACCOUNTS_PER_USER=10
THREADS=8
OPERATIONS_PER_THREAD=200000

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --scenario)
      SCENARIO="$2"
      shift 2
      ;;
    --output)
      OUTPUT="$2"
      shift 2
      ;;
    --no-progress)
      PROGRESS="--no-progress"
      shift
      ;;
    --progress)
      PROGRESS="--progress"
      shift
      ;;
    --users)
      USERS="$2"
      shift 2
      ;;
    --accounts)
      ACCOUNTS_PER_USER="$2"
      shift 2
      ;;
    --threads)
      THREADS="$2"
      shift 2
      ;;
    --operations)
      OPERATIONS_PER_THREAD="$2"
      shift 2
      ;;
    --help)
      echo "Usage: $0 [options]"
      echo "Options:"
      echo "  --scenario SCENARIO    Test scenario (BALANCED_LOAD, HEAVY_TRANSFERS, etc.)"
      echo "  --output FORMAT        Output format (CONSOLE, CSV, JSON, BOTH)"
      echo "  --progress/--no-progress  Enable/disable progress reporting"
      echo "  --users NUM            Number of users to create"
      echo "  --accounts NUM         Accounts per user"
      echo "  --threads NUM          Number of concurrent threads"
      echo "  --operations NUM       Operations per thread"
      echo "  --help                 Show this help"
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      echo "Use --help for usage information"
      exit 1
      ;;
  esac
done

echo "Configuration:"
echo "  Scenario: $SCENARIO"
echo "  Output: $OUTPUT"
echo "  Progress: $PROGRESS"
echo "  Users: $USERS"
echo "  Accounts/User: $ACCOUNTS_PER_USER"
echo "  Threads: $THREADS"
echo "  Operations/Thread: $OPERATIONS_PER_THREAD"
echo "=========================================="

# Build the project if needed
echo "Building stress test project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

# Run the stress test
echo "Running stress test..."
java -cp "target/classes:target/dependency/*" \
    -DNUMBER_OF_USERS=$USERS \
    -DNUMBER_OF_ACCOUNTS_PER_USER=$ACCOUNTS_PER_USER \
    -DNUMBER_OF_THREADS=$THREADS \
    -DOPERATIONS_PER_THREAD=$OPERATIONS_PER_THREAD \
    com.bank.stress.NetworkStressTest \
    --scenario $SCENARIO \
    --output $OUTPUT \
    $PROGRESS

echo "=========================================="
echo "Stress test completed!"
echo "=========================================="