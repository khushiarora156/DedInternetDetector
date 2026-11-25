# Dead Internet Detector - PowerShell Test Script
Write-Host "Compiling application classes..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path target/classes | Out-Null
$sources = (Get-ChildItem -Path src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -d target/classes $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "Main compilation failed." -ForegroundColor Red
    exit 1
}

Write-Host "Compiling test harness..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path target/test-classes | Out-Null
javac -cp target/classes -d target/test-classes src/test/java/com/detector/TestHarness.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "Test harness compilation failed." -ForegroundColor Red
    exit 1
}

Write-Host "Executing test suite..." -ForegroundColor Green
java -cp "target/classes;target/test-classes" com.detector.TestHarness
