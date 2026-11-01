# System Architecture — Railway Management System (RMS)

## 1. Architectural Overview

The Railway Management System is structured as a layered desktop application following clean OOP principles:

```
GUI Presentation Layer (Java Swing + FlatLaf)
                │
                ▼
      Service / Business Layer
  (Reservation, Train, Auth, Admin, Fare)
                │
                ▼
       Data Access Layer (JDBC DAOs)
  (UserDAO, TrainDAO, RouteDAO, SeatDAO, ReservationDAO)
                │
                ▼
   Database Layer (MySQL 8.0 InnoDB)
```

## 2. Core Components

### 2.1 Dependency Injection & Context (`com.railway.config`)
- **`ApplicationContext`**: Central IoC container constructing DAOs and injecting them into business services.
- **`AppConfig`**: Resolves configuration from environment variables first (`DB_URL`, `DB_USER`, `DB_PASSWORD`), then classpath `db.properties`.
- **`SessionContext`**: Thread-safe holder of authenticated `User` context.

### 2.2 Persistence & Transactions (`com.railway.database`)
- **`DatabaseConnection`**: Manages MySQL Connector/J JDBC connections.
- **`TransactionManager`**: Functional transaction boundaries enforcing `setAutoCommit(false)`, `commit()`, and `rollbackQuietly()`.
- **Concurrency & Pessimistic Locking**: `SELECT ... FOR UPDATE` row locks on candidate seats combined with database-level `UNIQUE KEY uk_train_date_seat (train_id, journey_date, seat_id)`.

### 2.3 Domain Model (`com.railway.model`)
- **`User` (Abstract)**: Extended by `PassengerUser` and `AdminUser` (Polymorphic authorization `hasAdminPrivileges()`).
- **`Train` (Abstract)**: Extended by `RajdhaniExpress`, `ShatabdiExpress`, and `MailExpressTrain` (Polymorphic pricing multipliers and track speed priorities).
- **`Reservation`**: Composition root managing `ReservationPassenger` collections and assigned seats.

### 2.4 Service Contracts (`com.railway.service`)
- **`ReservationService`**: Atomic seat allocation, multi-passenger ticketing, and cancellations.
- **`FareCalculationService`**: Authoritative calculation of distance fare, multipliers, reservation charges, and taxes.
- **`TrainService`**: Fleet querying, route stop validation, and seat availability.
- **`AuthService`**: Credential validation, BCrypt verification, and user registration.
- **`AdminService`**: Fleet additions, route modifications, platform reassignment.
- **`AuditService`**: Auditing administrative actions and reservation events.
