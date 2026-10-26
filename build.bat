@echo off
title Build Railway Management System
echo =====================================================================
echo Compiling Railway Management System Java Source Files
echo =====================================================================

if not exist bin mkdir bin

set CP=lib\mysql-connector-j-8.3.0.jar;lib\postgresql-42.7.2.jar;lib\jbcrypt-0.4.jar;lib\flatlaf-3.4.jar;lib\junit-platform-console-standalone-1.10.2.jar

if exist "C:\Program Files\Java\jdk-26.0.2\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-26.0.2\bin\javac.exe"
) else (
    set "JAVAC_CMD=javac"
)

echo Compiling src/main/java...
"%JAVAC_CMD%" -encoding UTF-8 -cp "%CP%" -d bin src\main\java\com\railway\*.java src\main\java\com\railway\config\*.java src\main\java\com\railway\database\*.java src\main\java\com\railway\model\*.java src\main\java\com\railway\model\enums\*.java src\main\java\com\railway\model\dto\*.java src\main\java\com\railway\dao\*.java src\main\java\com\railway\dao\impl\*.java src\main\java\com\railway\service\*.java src\main\java\com\railway\service\impl\*.java src\main\java\com\railway\util\*.java src\main\java\com\railway\exception\*.java src\main\java\com\railway\gui\*.java src\main\java\com\railway\gui\panels\*.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Main build failed!
    if "%1" neq "/nopause" pause
    exit /b %ERRORLEVEL%
)

echo Compiling src/test/java...
"%JAVAC_CMD%" -encoding UTF-8 -cp "bin;%CP%" -d bin src\test\java\com\railway\*.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Test build failed!
    if "%1" neq "/nopause" pause
    exit /b %ERRORLEVEL%
)

echo Copying classpath resources (db.properties, schema.sql, sample_data.sql)...
xcopy /s /y /q src\main\resources\* bin\

echo =====================================================================
echo BUILD SUCCESSFUL! (MySQL and PostgreSQL supported, run run.bat to start)
echo =====================================================================
if "%1" neq "/nopause" pause
