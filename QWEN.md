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
2. Run the main class `com.bank.HttpServer`. This can typically be done using Maven as well:
   ```bash
   mvn exec:java -Dexec.mainClass="com.bank.HttpServer"
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
