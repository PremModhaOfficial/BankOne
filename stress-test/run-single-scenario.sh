#!/bin/bash

# Single Scenario Runner
# Run a specific stress test scenario
# Usage: ./run-single-scenario.sh <scenario> [options]

if [ $# -eq 0 ]; then
    echo "Usage: $0 <scenario> [options]"
    echo ""
    echo "Available scenarios:"
    echo "  BALANCED_LOAD     - 40% deposits, 30% withdrawals, 30% transfers"
    echo "  HEAVY_TRANSFERS   - 20% deposits, 20% withdrawals, 60% transfers"
    echo "  READ_HEAVY        - 80% deposits, 10% withdrawals, 10% transfers"
    echo "  STRESS_TRANSFERS  - 100% transfers"
    echo "  WITHDRAWAL_HEAVY  - 10% deposits, 80% withdrawals, 10% transfers"
    echo ""
    echo "Options:"
    echo "  --output FORMAT   - Output format: CONSOLE, CSV, JSON, BOTH (default: BOTH)"
    echo "  --no-progress     - Disable progress reporting"
    echo "  --users NUM       - Number of users (default: 5)"
    echo "  --accounts NUM    - Accounts per user (default: 10)"
    echo "  --threads NUM     - Number of threads (default: 8)"
    echo "  --operations NUM  - Operations per thread (default: 200000)"
    echo ""
    echo "Examples:"
    echo "  $0 BALANCED_LOAD"
    echo "  $0 HEAVY_TRANSFERS --output CSV --users 10"
    echo "  $0 READ_HEAVY --no-progress --operations 10000"
    exit 1
fi

SCENARIO=$1
shift

# Default values
OUTPUT="BOTH"
PROGRESS="--progress"
USERS=5
ACCOUNTS_PER_USER=10
THREADS=8
OPERATIONS_PER_THREAD=200000

# Parse remaining arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --output)
      OUTPUT="$2"
      shift 2
      ;;
    --no-progress)
      PROGRESS="--no-progress"
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
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

echo "=========================================="
echo "🏦 Banking System - Single Scenario Test"
echo "=========================================="
echo "Scenario: $SCENARIO"
echo "Output: $OUTPUT"
echo "Progress: $PROGRESS"
echo "Users: $USERS"
echo "Accounts/User: $ACCOUNTS_PER_USER"
echo "Threads: $THREADS"
echo "Operations/Thread: $OPERATIONS_PER_THREAD"
echo "=========================================="

# Build the project and download dependencies
echo "Building project and downloading dependencies..."
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
echo "Test completed!"
echo "=========================================="