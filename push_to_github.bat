@echo off
title Push Railway Management System to GitHub
echo =====================================================================
echo Pushing Railway Management System to GitHub
echo Repository: https://github.com/SainiNaman1/Project
echo =====================================================================
echo.

set "GIT_CMD=%~dp0.git_bin\cmd\git.exe"

if not exist "%GIT_CMD%" (
    set "GIT_CMD=git"
)

echo Checking Git status...
"%GIT_CMD%" status
echo.

echo ---------------------------------------------------------------------
echo Attempting to push to branch 'main'...
echo Note: If GitHub prompts for credentials:
echo   - Username: SainiNaman1
echo   - Password: Use a GitHub Personal Access Token (PAT)
echo     (Generate one at https://github.com/settings/tokens with 'repo' scope)
echo ---------------------------------------------------------------------
echo.

"%GIT_CMD%" push -u origin main

if %ERRORLEVEL% equ 0 (
    echo.
    echo =====================================================================
    echo SUCCESS! Repository successfully pushed to:
    echo https://github.com/SainiNaman1/Project
    echo =====================================================================
) else (
    echo.
    echo [ERROR] Push failed or authentication required.
    echo If you need to authenticate with a GitHub Token, you can also run:
    echo   .git_bin\cmd\git.exe push https://YOUR_TOKEN@github.com/SainiNaman1/Project.git main
)

echo.
pause
