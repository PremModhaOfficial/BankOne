# Bank Project Scripts

This directory contains various scripts for running and testing the Bank application.

## Server and Client Scripts

- `run-server.sh` - Starts the Bank HTTP server
- `run-client.sh` - Starts the Bank HTTP client

## Test Scripts

- `api_test.sh` - Tests the API endpoints
- `comprehensive_debug.sh` - Runs comprehensive tests with debug output
- `comprehensive_test.sh` - Runs comprehensive tests
- `debug_accounts.sh` - Debugs account-related functionality
- `test_auth.sh` - Tests authentication functionality
- `test_client.sh` - Tests the client interface
- `test_errors.sh` - Tests error handling
- `test_get_all_accounts.sh` - Tests the getAllAccounts endpoint
- `test_no_auth.sh` - Tests functionality without authentication
- `test_no_auth_fixed.sh` - Tests fixed version without authentication

## Stress Testing

The stress test is located in the `stress-test` directory and includes:
- A separate Maven project for stress testing
- Scripts to build and run the stress tests
- Configuration options for different load scenarios

See `stress-test/README.md` for detailed information on running stress tests.

## Usage

To run the server:
```bash
./scripts/run-server.sh
```

To run the client:
```bash
./scripts/run-client.sh
```

To run the stress test:
```bash
cd stress-test
./run-stress-test.sh
```

To run any test script:
```bash
./scripts/[script-name].sh
```