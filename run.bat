@echo off
REM Build and run script for ERP Application (Windows)
REM This script compiles all Java files and runs the application

echo ==========================================
echo   ERP Application Build ^& Run Script
echo ==========================================

REM Navigate to script directory
cd /d "%~dp0"

REM Create output directory if it doesn't exist
if not exist "out" mkdir out

echo.
echo [1/3] Cleaning previous build...
if exist "out\*" del /q /s out\* >nul 2>&1

echo [2/3] Compiling Java files...

REM Compile all Java files
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt

REM Check if compilation was successful
if %ERRORLEVEL% == 0 (
    echo       Compilation successful!
    del sources.txt

    echo [3/3] Running application...
    echo.
    echo ==========================================
    java -cp out com.erp.ERPApplication
) else (
    echo       Compilation failed!
    del sources.txt
    exit /b 1
)
