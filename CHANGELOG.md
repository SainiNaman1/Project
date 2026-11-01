# Changelog - Railway Management System (RMS)

All notable changes to the Railway Management System will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased] - 2026-11-01

### Added
- **Database Standardization (MySQL 8.x)**: Added `com.mysql:mysql-connector-j:8.3.0` to `pom.xml`, updated scripts (`build.bat`, `run.bat`, `test.bat`).
- **ApplicationContext**: Introduced `com.railway.config.ApplicationContext` central dependency injection container to coordinate DAOs and Services.
- **TransactionManager**: Introduced `com.railway.database.TransactionManager` providing functional atomic transaction management with auto-rollback.
- **Database-Level Seat Constraint**: Added `UNIQUE KEY uk_train_date_seat (train_id, journey_date, seat_id)` to `seat_assignments` in `schema_mysql.sql`.
- **Audit Table**: Added `audit_logs` table schema in `schema_mysql.sql` and auto-migration support in `SchemaInitializer.java`.
- **Environment Variable Overrides**: Supported `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_DRIVER` in `AppConfig.java`.

### Changed
- **DatabaseConnection**: Removed PostgreSQL fallback logic and hardcoded fallback credentials; standardized strictly on MySQL 8.x.
- **SchemaInitializer**: Streamlined to execute MySQL DDL (`schema_mysql.sql`) and sample data (`sample_data_mysql.sql`).
- **Main**: Updated startup messaging and pre-warmed `ApplicationContext`.

### Removed
- Removed legacy PostgreSQL files `schema.sql` and `sample_data.sql`.
- Removed PostgreSQL driver dependency from `pom.xml` and runtime scripts.
