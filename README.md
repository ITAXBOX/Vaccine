# 🛡️ Vaccine - Advanced SQL Injection Scanner

**Cybersecurity Piscine - Final Project**

Vaccine is a powerful, enterprise-grade SQL injection vulnerability scanner built with a focus on **clean architecture**, **design patterns**, and **SOLID principles**. This project demonstrates professional-level software engineering applied to cybersecurity tooling.

---

## 🎯 Project Overview

Vaccine is not just another SQL injection scanner—it's a **carefully architected security tool** that showcases:

- ✅ **Multiple Detection Strategies**: Error-based, Union-based, Boolean-based, and Time-based injection detection
- ✅ **DBMS Fingerprinting**: Intelligent database detection and dialect-specific payload generation
- ✅ **Automated Enumeration**: Extract database structure, tables, and data once a vulnerability is found
- ✅ **Enterprise Design Patterns**: Strategy, Factory, Facade, and more
- ✅ **SOLID Principles**: Clean, maintainable, and extensible codebase
- ✅ **Comprehensive Testing**: Multi-protocol support (GET/POST) with custom headers and bodies

---

## 🚀 Quick Start

### Build

```bash
make
```

### Basic Usage

```bash
./vaccine "http://testphp.vulnweb.com/artists.php?artist=1"
```

This single command will:
1. Parse the target URL and extract parameters
2. Fingerprint the database management system
3. Test all parameters with **4 powerful injection strategies**
4. Enumerate database information if vulnerabilities are found
5. Generate a detailed report in `vaccine.txt`

---

## 💪 Power & Capabilities

### Multi-Strategy Attack Surface

Vaccine employs **four distinct injection strategies**, each designed to detect different types of SQL injection vulnerabilities:

#### 1️⃣ **Error-Based Detection**
- Triggers database errors and analyzes error messages
- Identifies DBMS type through error fingerprinting
- Fast and reliable for verbose error configurations

#### 2️⃣ **Union-Based Detection**
- Leverages SQL UNION operators to extract data
- Automatically determines the number of columns
- Enables direct data exfiltration

#### 3️⃣ **Boolean-Based Detection**
- Analyzes response differences based on true/false conditions
- Works in blind injection scenarios
- Highly effective against filtered applications

#### 4️⃣ **Time-Based Detection**
- Uses time delays to infer SQL execution
- The most stealthy approach
- Works even when no visible output is returned

### DBMS Fingerprinting Engine

Vaccine doesn't just test for vulnerabilities—it **intelligently identifies** the target database:

- MySQL
- PostgreSQL
- Microsoft SQL Server
- Oracle
- SQLite

Once detected, Vaccine uses **database-specific dialects** to craft optimal payloads, maximizing success rates.

### Automated Data Enumeration

When a vulnerability is confirmed, Vaccine automatically:
- Extracts the current database name
- Lists all available databases
- Enumerates tables in the target database
- Retrieves column information
- Dumps sensitive data

---

## 🏗️ Architecture & Design Excellence

### Design Patterns Implemented

#### 🔹 **Strategy Pattern**
The core of Vaccine's power lies in the **Strategy Pattern**. Each injection technique is encapsulated as a strategy:

```
InjectionStrategy (interface)
├── ErrorBasedStrategy
├── UnionBasedStrategy
├── BooleanBasedStrategy
└── TimeBasedStrategy
```

**Benefits:**
- Easy to add new detection techniques
- Strategies can be enabled/disabled independently
- Each strategy is tested and maintained in isolation
- Follows Open/Closed Principle (open for extension, closed for modification)

#### 🔹 **Factory Pattern**
The `DbmsDialectFactory` creates database-specific dialects:

```
DbmsDialectFactory
├── MySqlDialect
├── PostgreSqlDialect
├── MsSqlDialect
├── OracleDialect
└── SqliteDialect
```

**Benefits:**
- Centralizes object creation logic
- Supports runtime dialect selection based on fingerprinting
- Easily extensible for new database types

#### 🔹 **Facade Pattern**
The `VaccineFacade` provides a simplified interface to the complex scanning system:

```java
VaccineFacade facade = new VaccineFacade();
ScanResult result = facade.scan(config);
```

**Benefits:**
- Hides complex subsystem interactions
- Provides a clean API for clients
- Manages dependency assembly and orchestration

#### 🔹 **Provider Pattern**
The `InjectionStrategyProvider` manages strategy instances:

**Benefits:**
- Centralized strategy management
- Consistent strategy lifecycle
- Easy to modify the strategy set

---

### SOLID Principles in Action

#### **S - Single Responsibility Principle**
Each class has **one clear purpose**:
- `ScanEngine`: Orchestrates the scanning process
- `DbmsFingerprintingService`: Detects database types
- `EnumerationService`: Extracts database information
- `HttpClient`: Handles HTTP communications
- `StorageManager`: Manages result persistence

#### **O - Open/Closed Principle**
The system is **open for extension, closed for modification**:
- Adding a new injection strategy doesn't require changing existing code
- New DBMS dialects can be added without modifying the factory
- The enumeration system can be extended with new techniques

#### **L - Liskov Substitution Principle**
All strategy implementations are **perfectly interchangeable**:
- Any `InjectionStrategy` can be used wherever the interface is expected
- All dialects implement `DbmsDialect` consistently
- HTTP client implementations are substitutable

#### **I - Interface Segregation Principle**
Interfaces are **focused and specific**:
- `InjectionStrategy`: Only requires `getName()` and `isVulnerable()`
- `HttpClient`: Minimal surface area for HTTP operations
- `DbmsDialect`: Only DBMS-specific operations

#### **D - Dependency Inversion Principle**
High-level modules depend on **abstractions, not concretions**:
- `ScanEngine` depends on `InjectionStrategy` interface, not concrete strategies
- `ScanContext` uses `HttpClient` interface, not `SimpleHttpClient`
- Strategies depend on `DbmsDialect` interface, not specific dialects

---

## 🧩 Project Structure

```
src/com/vaccine/
├── app/              # Application entry point
├── cli/              # Command-line parsing
├── config/           # Configuration management
├── core/             # Core scanning engine and context
├── db/               # DBMS fingerprinting and dialects
│   └── dialect/      # Database-specific implementations
├── enumeration/      # Data extraction services
├── facade/           # Simplified API facade
├── http/             # HTTP client abstraction
├── injection/        # Strategy implementations
├── model/            # Domain models (Target, Vulnerability, etc.)
└── util/             # Utilities and storage
```

---

## 🎓 Technical Highlights

### 1. **Clean Separation of Concerns**
Each package has a clear responsibility, making the codebase highly maintainable.

### 2. **Dependency Injection**
Dependencies are injected through constructors, enabling:
- Easy testing with mock objects
- Flexible configuration
- Reduced coupling

### 3. **Immutability & Encapsulation**
Domain models like `Target`, `Parameter`, and `Vulnerability` are designed to be immutable where appropriate.

### 4. **Context Object Pattern**
The `ScanContext` carries shared state across the scanning process, avoiding global variables and parameter proliferation.

### 5. **Optional Pattern**
Uses `Optional<Vulnerability>` to handle presence/absence of vulnerabilities safely, avoiding null pointer exceptions.

---

## 🔬 Testing Example

```bash
./vaccine "http://testphp.vulnweb.com/artists.php?artist=1"
```

**Expected Output:**
```
[*] Parsed URL: http://testphp.vulnweb.com/artists.php
[*] Found 1 parameter(s) to test
    - artist = 1
[*] Sending baseline request...
[*] Baseline response: 200 (XXXX bytes)
[*] Fingerprinting DBMS...
[*] Detected DBMS: MySQL
[*] Testing parameters for SQL injection...
[*] Testing parameter: artist
    [*] Trying Error-Based strategy...
    [+] VULNERABLE! Found with Error-Based
    [*] Trying Union-Based strategy...
    [+] VULNERABLE! Found with Union-Based
    [*] Trying Boolean-Based strategy...
    [*] Trying Time-Based strategy...
[*] Starting enumeration...
[*] Current database: acuart
[*] Listing databases...
[*] Enumerating tables...
=== Vaccine Scan Summary ===
Target      : http://testphp.vulnweb.com/artists.php
HTTP Method : GET
DBMS        : MySQL
Vulnerabilities found: 2
```

---

## 📦 Build System

Vaccine uses a **professional Makefile** for build automation:

```bash
make          # Build the project
make clean    # Remove compiled files
make rebuild  # Clean and rebuild
make run      # Build and run with default settings
```

---

## 🏆 Why Vaccine Stands Out

1. **Professional Architecture**: Built like enterprise security software
2. **Extensible Design**: Easy to add new strategies, DBMS support, or features
3. **Multiple Attack Vectors**: 4 distinct strategies ensure comprehensive coverage
4. **Intelligent Detection**: DBMS fingerprinting enables targeted attacks
5. **Clean Code**: Follows industry best practices and SOLID principles
6. **Educational Value**: Demonstrates advanced software engineering in cybersecurity

---

## 🛠️ Advanced Usage

### Custom HTTP Method
```bash
./vaccine -X POST "http://example.com/login.php?id=1"
```

### Custom Output File
```bash
./vaccine -o custom_report.txt "http://testphp.vulnweb.com/artists.php?artist=1"
```

### Help
```bash
./vaccine --help
```

---

## 📚 Learning Outcomes

This project demonstrates mastery of:
- ✅ Object-Oriented Design Principles (SOLID)
- ✅ Design Patterns (Strategy, Factory, Facade, Provider)
- ✅ Security Testing Methodologies
- ✅ Clean Code Architecture
- ✅ Dependency Management
- ✅ Professional Build Systems
- ✅ SQL Injection Techniques
- ✅ HTTP Protocol Implementation

---

## 📄 License

Educational project for **42 Beirut - Cybersecurity Piscine**.

---

## 🎯 Conclusion

Vaccine is more than a SQL injection scanner—it's a **showcase of software craftsmanship** applied to cybersecurity. Every line of code reflects careful consideration of design principles, maintainability, and extensibility. This project proves that security tools can and should be built with the same rigor as any enterprise application.

**Built with precision. Designed for power. Crafted for security.**

