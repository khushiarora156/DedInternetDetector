@echo off
echo ========================================================
echo   Dead Internet Detector - Standalone Test Runner
echo ========================================================
if not exist target\classes mkdir target\classes
if not exist target\test-classes mkdir target\test-classes

dir /s /B src\main\java\*.java > sources.txt
javac -d target\classes @sources.txt
del sources.txt

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Application compilation failed.
    pause
    exit /b 1
)

javac -cp target\classes -d target\test-classes src\test\java\com\detector\TestHarness.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Test harness compilation failed.
    pause
    exit /b 1
)

java -cp "target\classes;target\test-classes" com.detector.TestHarness
pause
