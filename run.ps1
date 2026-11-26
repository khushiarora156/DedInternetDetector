# Dead Internet Detector - PowerShell Run Script
Write-Host "Compiling Java sources..." -ForegroundColor Cyan
New-Item -ItemType Directory -Force -Path target/classes | Out-Null
$sources = (Get-ChildItem -Path src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -d target/classes $sources

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful. Launching application..." -ForegroundColor Green
    java -cp target/classes com.detector.main.Main
} else {
    Write-Host "Compilation failed! Please check error messages above." -ForegroundColor Red
}
