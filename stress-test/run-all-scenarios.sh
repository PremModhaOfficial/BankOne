#!/bin/bash

# Run All Scenarios Script
# Executes all test scenarios sequentially for comprehensive analysis

echo "=========================================="
echo "🏦 Banking System - Complete Test Suite"
echo "=========================================="
echo "Running all test scenarios:"
echo "1. Smoke Test (quick verification)"
echo "2. Balanced Load Test"
echo "3. Read-Heavy Test"
echo "4. Withdrawal-Heavy Test"
echo "5. Heavy Transfers Test"
echo "6. Transfer Stress Test"
echo "=========================================="

# Build the project first
echo "Building project..."
cd ../
mvn clean compile -q
cd stress-test

# Build the stress test module
echo "Building stress test module..."
mvn compile -q

# Function to run a test scenario
run_scenario() {
    local scenario=$1
    local description=$2

    echo ""
    echo "=========================================="
    echo "Running: $description"
    echo "Scenario: $scenario"
    echo "=========================================="

    java -cp "../target/classes:stress-test/target/classes:../target/dependency/*" \
        com.bank.stress.NetworkStressTest \
        --scenario $scenario \
        --output BOTH \
        --progress

    echo "Completed: $description"
    echo "=========================================="
}

# Run smoke test first
echo "Step 1: Smoke Test"
./run-smoke-test.sh

# Run all scenarios
echo ""
echo "Step 2-6: Running all scenarios..."
run_scenario "BALANCED_LOAD" "Balanced Load Test"
run_scenario "READ_HEAVY" "Read-Heavy Test"
run_scenario "WITHDRAWAL_HEAVY" "Withdrawal-Heavy Test"
run_scenario "HEAVY_TRANSFERS" "Heavy Transfers Test"
run_scenario "STRESS_TRANSFERS" "Transfer Stress Test"

echo ""
echo "=========================================="
echo "🎉 COMPLETE TEST SUITE FINISHED!"
echo "=========================================="
echo ""
echo "📊 Analysis Files Generated:"
echo "├── stress-test-results.csv     (Latest test results)"
echo "├── stress-test-results.json    (Latest test results)"
echo "├── logs/application.log        (Complete execution logs)"
echo "└── Multiple result files from each scenario"
echo ""
echo "📈 Comparative Analysis Available:"
echo "- Compare performance across different scenarios"
echo "- Analyze throughput variations"
echo "- Review error patterns by scenario type"
echo "- Evaluate system behavior under different loads"
echo ""
echo "🔍 Next Steps:"
echo "1. Review logs for performance bottlenecks"
echo "2. Compare results across scenarios"
echo "3. Analyze error patterns and root causes"
echo "4. Generate performance reports and charts"
echo "=========================================="