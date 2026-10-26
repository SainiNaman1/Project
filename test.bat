@echo off
title Run Railway Management System Tests
echo =====================================================================
echo Running JUnit 5 Test Suite
echo =====================================================================

if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

set CP=bin;lib\mysql-connector-j-8.3.0.jar;lib\postgresql-42.7.2.jar;lib\jbcrypt-0.4.jar;lib\flatlaf-3.4.jar

"%JAVA_CMD%" -jar lib\junit-platform-console-standalone-1.10.2.jar --class-path "%CP%" --scan-class-path

echo.
if "%1" neq "/nopause" pause
