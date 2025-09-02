#!/bin/bash

# Analysis-Friendly Test Script
# Runs a comprehensive test optimized for data analysis
# Includes detailed metrics and multiple output formats

echo "=========================================="
echo "📊 Banking System - Analysis Test"
echo "=========================================="
echo "This test is optimized for performance analysis:"
echo "- Detailed metrics collection"
echo "- Multiple output formats"
echo "- Statistical analysis"
echo "- Error categorization"
echo "=========================================="

# Build the stress test project and download dependencies
echo "Building stress test project and downloading dependencies..."
mvn clean compile dependency:copy-dependencies -q

# Run the analysis test
echo "Starting analysis-optimized test..."
java -cp "target/classes:target/dependency/*" \
    com.bank.stress.NetworkStressTest \
    --scenario BALANCED_LOAD \
    --output BOTH \
    --progress

echo "=========================================="
echo "Analysis test completed!"
echo ""
echo "📈 Analysis Files Generated:"
echo "├── stress-test-results.csv     (Spreadsheet analysis)"
echo "├── stress-test-results.json    (Programmatic analysis)"
echo "└── logs/application.log        (Detailed execution logs)"
echo ""
echo "📊 Key Metrics to Analyze:"
echo "├── Response time percentiles (P50, P95, P99)"
echo "├── Throughput over time"
echo "├── Error categorization"
echo "├── Operation type distribution"
echo "└── Success/failure rates"
echo ""
echo "🔍 Next Steps:"
echo "1. Import CSV into Excel/LibreOffice for charts"
echo "2. Use JSON for custom analysis scripts"
echo "3. Review logs for performance bottlenecks"
echo ""
echo "📁 Files Location:"
echo "/home/prem-modha/projects/Motadata/BankOne/Bank/stress-test/"
echo "=========================================="