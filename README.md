# Railway Management System (RMS)
**Desktop Railway Simulation | Java OOP, Swing, JDBC, MySQL 8.0 & PostgreSQL**

---

## 1. Project Overview

The **Railway Management System (RMS)** is an educational desktop simulation of an Indian Railways passenger portal and operations console. Built with clean **Java 17+**, **Java Swing**, and **JDBC**, it demonstrates:

- **Layered Architecture**: Presentation (`gui`) $\to$ Business (`service`) $\to$ Data Access (`dao`) $\to$ Relational Database (`MySQL` / `PostgreSQL`).
- **Resilient Database Auto-Detection**: Configured for local **MySQL 8.0** (`localhost:3306`), with automatic fallback support for **PostgreSQL** (`localhost:5432`).
- **ACID Transaction Integrity**: Concurrent seat allocation with row locking (`SELECT ... FOR UPDATE`) and rollback management to prevent double-booking.
- **Defensive Security**: BCrypt password hashing with work factor 12 and parameterized `PreparedStatement`s throughout.
- **Modern UI**: Styled with **FlatLaf Look and Feel** (Navy Blue & Slate aesthetic) for a contemporary desktop experience.

---

## 2. Default Demo Accounts

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | Fleet management, live platform reassignment, system audit trail |
| **Passenger** | `rahul` | `Rahul@123` | Train search, real-time availability, seat booking, ticket print preview |

*Note: New passenger accounts can also be registered directly from the application login screen.*

---

## 3. Database Configuration

The application is pre-configured in `src/main/resources/db.properties` and `bin/db.properties` for your local MySQL server:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/railway_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=saini
```

Database `railway_db` has been created and initialized with all 9 tables, routes, trains, coach seat layouts, rules, and demo accounts.

---

## 4. How to Run

### Option 1: 1-Click Launch (Windows)
- Double-click **`run.bat`** in the project folder to start the application.
- To recompile changes at any time, double-click **`build.bat`**.
- To execute the 28-test JUnit 5 suite, double-click **`test.bat`**.

### Option 2: Command Line
```cmd
java -cp "bin;lib\mysql-connector-j-8.3.0.jar;lib\postgresql-42.7.2.jar;lib\jbcrypt-0.4.jar;lib\flatlaf-3.4.jar" com.railway.Main
```

---

## 5. Directory Structure

```
railway-management-system/
├── pom.xml                                <-- Maven Project Configuration
├── run.bat                                <-- 1-Click Application Launcher
├── build.bat                              <-- 1-Click Compiler Script
├── test.bat                               <-- 1-Click JUnit 5 Test Runner
├── lib/                                   <-- Bundled JARs (MySQL Driver, PostgreSQL Driver, FlatLaf, BCrypt, JUnit)
├── src/
│   ├── main/
│   │   ├── java/com/railway/
│   │   │   ├── Main.java                  <-- Application Entry Point
│   │   │   ├── config/                    <-- AppConfig & SessionContext
│   │   │   ├── database/                  <-- Connection Manager (Dual MySQL/Postgres)
│   │   │   ├── model/                     <-- OOP Domain Hierarchy & Enums
│   │   │   ├── dao/                       <-- DAO Interfaces & JDBC Implementations
│   │   │   ├── service/                   <-- Business Logic & Transaction Coordinators
│   │   │   ├── util/                      <-- PasswordUtil (BCrypt), PNRGenerator, FareCalculator
│   │   │   ├── exception/                 <-- Domain Checked Exceptions
│   │   │   └── gui/                       <-- Swing Dashboards, Dialogs & Panels
│   │   └── resources/
│   │       ├── db.properties              <-- Active Database Connection Settings
│   │       ├── schema_mysql.sql           <-- MySQL Schema DDL
│   │       ├── sample_data_mysql.sql      <-- MySQL Sample Data Seeds
│   │       ├── schema.sql                 <-- PostgreSQL Schema DDL
│   │       └── sample_data.sql            <-- PostgreSQL Sample Data Seeds
│   └── test/java/com/railway/             <-- JUnit 5 Unit & Live DB Tests (28/28 Passing)
└── bin/                                   <-- Compiled Bytecode & Resources
```
