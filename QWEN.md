# Project Context for Qwen Code

## Project Overview

This is a Java-based HTTP server project named "Bank". The main application class is `HttpServer`, which starts a server based on a configuration file (`http.json`). The server listens for incoming HTTP requests on a specified port and serves content from a specified web root directory.

Key technologies used:
- **Language:** Java (Target/Source version 21)
- **Build Tool:** Maven
- **Dependencies:**
  - Jackson (for JSON processing)
  - SLF4J & Logback (for logging)
  - JUnit (for testing)

The project structure follows standard Maven conventions, with source code in `src/main/java` and tests in `src/test/java`. The main package is `com.bank`.

Key components identified:
- **HTTP Core:** Classes for parsing HTTP requests (`HttpRequest`, `HttpMethod`, `HttpParser`, etc.) and handling status codes (`HttpStatusCode`).
- **Server Core:** Classes for server configuration (`Configuration`, `ConfigurationManager`) and the core server listener thread (`ServerListnerThread`).
- **Business Logic:** A package (`com.bank.business`) likely for application-specific logic, currently containing an `entities` sub-package.
- **Static Resources:** The web root is configured to `/tmp` by default (in `http.json`).

## Building and Running

### Prerequisites
- Java JDK 21
- Apache Maven

### Build
To compile the project and manage dependencies, run:
```bash
mvn compile
```

To package the project into a JAR file, run:
```bash
mvn package
```
The resulting JAR will be located in the `target/` directory.

### Run
1. Ensure the project is built (`mvn compile` or `mvn package`).
2. Run the main class `com.bank.Main`. This can typically be done using Maven as well:
   ```bash
   mvn exec:java -Dexec.mainClass="com.bank.Main"
   ```
   Or by running the JAR file if packaged:
   ```bash
   java -jar target/Bank-1.0-SNAPSHOT.jar
   ```
3. The server will start and listen on the port specified in `src/main/resources/http.json` (default is 8080). It will serve files from the directory specified by the `webroot` property in the same configuration file (default is `/tmp`).

### Test
To run the unit tests (using JUnit), execute:
```bash
mvn test
```

## Development Conventions

- **Language & Style:** Java 21.
- **Project Structure:** Standard Maven directory layout.
- **Logging:** Uses SLF4J API with Logback implementation. Log messages are created using `LoggerFactory.getLogger(...)`.
- **Configuration:** Server configuration is externalized in a JSON file (`http.json`).
- **Testing:** Unit tests are written using JUnit and placed in the `src/test/java` directory.
- **Dependencies:** Managed via Maven (`pom.xml`).

## Qwen Added Memories
- Project: Bank - A Java-based HTTP server with multithreading capabilities for banking operations

Key Changes Made:
1. Removed authentication/JWT system - Simplified to username/email identification only
2. Removed timestamp fields (createdAt/updatedAt) from User and Account entities
3. Updated UserService to remove password parameters
4. Updated AccountService to remove timestamp updates
5. Modified DTOs to match simplified data model
6. Removed unused JWTUtil class and related dependencies
7. Created stress testing framework in separate stress-test directory
8. Generated comprehensive documentation in SUMMARY.md

Multithreading Implementation:
- Thread Pool Executors for concurrent request handling
- Atomic operations for account balance updates
- Lock-free algorithms using compare-and-swap (CAS) operations
- Concurrent-safe deposit/withdrawal operations

Current Status:
- Project compiles successfully (mvn clean compile)
- Can be installed to local Maven repository (mvn install -DskipTests)
- Stress test framework created but has encoding issues with Java files
- Main server runs correctly on port 8080

To Run Server:
mvn exec:java -Dexec.mainClass="com.bank.Main"

Stress Testing:
Located in stress-test directory with README.md for instructions
- Fixed stress test results display issues in the Bank application. The fixes included:
1. Modified StressTest.java to properly track successful vs failed operations by catching exceptions and incrementing the appropriate counters
2. Added missing success rate calculation to the printResults method in StressTest.java
3. Ensured proper handling of edge cases like division by zero in the results calculation
4. Successfully rebuilt the stress test package with these fixes
