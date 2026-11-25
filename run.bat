@echo off
echo ========================================================
echo   Dead Internet & Fake Engagement Detector Launcher
echo ========================================================
if not exist target\classes mkdir target\classes

dir /s /B src\main\java\*.java > sources.txt
javac -d target\classes @sources.txt
del sources.txt

if %ERRORLEVEL% equ 0 (
    echo [OK] Compilation successful. Starting application...
    java -cp target\classes com.detector.main.Main
) else (
    echo [ERROR] Compilation failed.
    pause
)
