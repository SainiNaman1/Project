@echo off
title Push Railway Management System to GitHub
color 0B
echo ===============================================================================
echo            RAILWAY MANAGEMENT SYSTEM - GITHUB UPLOADER
echo            Repository: https://github.com/SainiNaman1/Project
echo ===============================================================================
echo.

set "GIT_CMD=%~dp0.git_bin\cmd\git.exe"

if not exist "%GIT_CMD%" (
    set "GIT_CMD=git"
)

echo [1/3] Checking Git status...
"%GIT_CMD%" status --short
echo.

echo [2/3] Configuring Git Credential Manager for browser authentication...
"%GIT_CMD%" config credential.helper manager
echo.

echo [3/3] Uploading all project files to GitHub branch 'main'...
echo Note: If a browser or popup appears, click 'Sign in with your browser' to authorize.
echo.

"%GIT_CMD%" push -u origin main

if %ERRORLEVEL% equ 0 (
    echo.
    echo ===============================================================================
    echo SUCCESS! All files have been successfully uploaded to GitHub:
    echo https://github.com/SainiNaman1/Project
    echo ===============================================================================
) else (
    echo.
    echo ===============================================================================
    echo [ERROR] Push failed or authentication was cancelled.
    echo If browser sign-in was not shown, you can push using a GitHub Token (PAT):
    echo   "%GIT_CMD%" push https://YOUR_TOKEN@github.com/SainiNaman1/Project.git main
    echo ===============================================================================
)

echo.
pause
