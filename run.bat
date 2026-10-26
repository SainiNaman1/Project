@echo off
title Railway Management System
echo =====================================================================
echo Launching Railway Management System (Java Swing + JDBC)
echo =====================================================================

if exist "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.2\bin\java.exe"
) else (
    set "JAVA_CMD=java"
)

set CP=bin;lib\mysql-connector-j-8.3.0.jar;lib\postgresql-42.7.2.jar;lib\jbcrypt-0.4.jar;lib\flatlaf-3.4.jar

"%JAVA_CMD%" -cp "%CP%" com.railway.Main

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application exited with code %ERRORLEVEL%.
    pause
)
