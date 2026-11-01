# Railway Management System (RMS)
**Desktop Railway Simulation | Java OOP, Swing, FlatLaf, JDBC & MySQL 8.0**

---

## 1. Project Overview

The **Railway Management System (RMS)** is an educational desktop simulation of an Indian Railways passenger portal and operational console. Built with **Java 17+**, **Java Swing**, and **JDBC**, it demonstrates:

- **Layered Architecture**: Presentation (`gui`) $\to$ Core Services (`service`) $\to$ Data Access Objects (`dao`) $\to$ Relational Database (`MySQL 8.0 InnoDB`).
- **Standardized MySQL 8.0 Persistence**: Dedicated MySQL Connector/J driver configuration, explicit connection pooling, and configuration overrides via environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
- **ACID Transaction Integrity**: Concurrent seat allocation with row locking (`SELECT ... FOR UPDATE`), database-level seat uniqueness (`UNIQUE(train_id, journey_date, seat_id)`), and functional transaction orchestration (`TransactionManager`).
- **Authoritative Fare Engine**: Single financial source of truth (`FareCalculationService`) providing itemized breakdowns (distance fare, train multiplier, class multiplier, reservation charge, GST).
- **Defensive Security & RBAC**: BCrypt password hashing (work factor 12) with salt, centralized role-based authorization (`AuthorizationService`), and ownership-enforced cancellations.
- **Modern UI**: Styled with **FlatLaf Look and Feel** (Navy Blue & Slate aesthetic) featuring persistent navigation sidebars, non-blocking asynchronous database operations (`SwingWorker`), and guided multi-step booking flows.

---

## 2. Default Demo Accounts

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | Fleet management, live platform reassignment, system audit trail |
| **Passenger** | `rahul` | `Rahul@123` | Train search, real-time availability, multi-step booking, ticket print preview |

*Note: New passenger accounts can also be registered directly from the application login screen.*

---

## 3. Database Configuration

The application is pre-configured in `src/main/resources/db.properties` for your local MySQL server:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://127.0.0.1:3306/railway_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=saini
```

Environment variable overrides (`DB_URL`, `DB_USER`, `DB_PASSWORD`) are automatically prioritized if set in your environment.

The database `railway_db` contains 10 InnoDB tables initialized via `src/main/resources/schema_mysql.sql` and seeded via `src/main/resources/sample_data_mysql.sql`.

---

## 4. How to Run

### Option 1: 1-Click Launch (Windows)
- Double-click **`run.bat`** in the project folder to start the application.
- To recompile changes at any time, double-click **`build.bat`**.
- To execute the 36-test JUnit 5 suite, double-click **`test.bat`**.

### Option 2: Command Line
```cmd
java -cp "bin;lib\mysql-connector-j-8.3.0.jar;lib\jbcrypt-0.4.jar;lib\flatlaf-3.4.jar" com.railway.Main
```

---

## 5. Directory Structure

```
railway-management-system/
├── pom.xml                                <-- Maven Project Configuration (MySQL 8.3.0)
├── run.bat                                <-- 1-Click Application Launcher
├── build.bat                              <-- 1-Click Compiler Script
├── test.bat                               <-- 1-Click JUnit 5 Test Runner
├── CHANGELOG.md                           <-- Change history & release notes
├── ARCHITECTURE.md                        <-- Layered architecture & ADR documentation
├── IMPLEMENTATION_PLAN.md                 <-- Refinement stage tracking
├── lib/                                   <-- Bundled JARs (MySQL Driver, FlatLaf, BCrypt, JUnit)
├── src/
│   ├── main/
│   │   ├── java/com/railway/
│   │   │   ├── Main.java                  <-- Application Entry Point
│   │   │   ├── config/                    <-- AppConfig, SessionContext, ApplicationContext
│   │   │   ├── database/                  <-- DatabaseConnection, TransactionManager, SchemaInitializer
│   │   │   ├── model/                     <-- OOP Domain Hierarchy & Enums
│   │   │   ├── dao/                       <-- DAO Interfaces & JDBC Implementations
│   │   │   ├── service/                   <-- Business Logic & Transaction Coordinators
│   │   │   ├── security/                  <-- AuthorizationService (RBAC enforcement)
│   │   │   ├── audit/                     <-- AuditService, AuditLog, AuditLogDAO
│   │   │   ├── util/                      <-- PasswordUtil (BCrypt), PNRGenerator, FareCalculator
│   │   │   ├── exception/                 <-- Domain Checked Exceptions
│   │   │   └── gui/                       <-- Swing Dashboards, Dialogs & Panels
│   │   └── resources/
│   │       ├── db.properties              <-- Active Database Connection Settings
│   │       ├── schema_mysql.sql           <-- Authoritative MySQL 8 Schema DDL
│   │       └── sample_data_mysql.sql      <-- MySQL 8 Sample Data Seeds
│   └── test/java/com/railway/             <-- JUnit 5 Unit & Concurrency Tests (36 Passing)
└── bin/                                   <-- Compiled Bytecode & Resources
```
