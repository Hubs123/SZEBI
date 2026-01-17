# Skrypt do uruchomienia wszystkich komponentów projektu
# Użyj:  ./URUCHOM_PROJEKT.ps1

Write-Host "==========================================" 
Write-Host "Energy Analysis & Prediction System"
Write-Host "==========================================" 
Write-Host ""
Write-Host "Ten skrypt uruchomi wszystkie komponenty w osobnych terminalach"
Write-Host ""
Write-Host "Wymagane:"
Write-Host "  - Python 3.8+"
Write-Host "  - Java 17+"
Write-Host "  - Maven 3.6+"
Write-Host "  - Node.js 16+"
Write-Host ""

Read-Host "Naciśnij Enter aby kontynuować lub Ctrl+C aby anulować..."

# Ścieżki
$SZEBI_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$SPRINGBOOT_DIR = $SZEBI_DIR
$FRONTEND_DIR = Join-Path $SZEBI_DIR "frontend"
$FASTAPI_DIR = Join-Path $SZEBI_DIR "python_module/src"

# Funkcja uruchamiająca nowe okno PowerShell
function Launch-Terminal {
    param(
        [string]$Title,
        [string]$Command,
        [string]$Directory
    )

    $fullCommand = "cd `"$Directory`"; Write-Host '=== $Title ==='; $Command"
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $fullCommand
}

Write-Host ""
Write-Host "Uruchamianie komponentów..."
Write-Host ""

# 1. FastAPI
Write-Host "1. Uruchamianie FastAPI (port 8000)..."
Launch-Terminal -Title "FastAPI Backend" -Command "python -m app.main" -Directory $FASTAPI_DIR
Start-Sleep -Seconds 2

# 2. Spring Boot
Write-Host "2. Uruchamianie Spring Boot (port 8080)..."
Launch-Terminal -Title "Spring Boot Gateway" -Command "mvn spring-boot:run" -Directory $SPRINGBOOT_DIR
Start-Sleep -Seconds 2

# 3. React
Write-Host "3. Uruchamianie React (port 3000)..."
Launch-Terminal -Title "React Frontend" -Command "npm start" -Directory $FRONTEND_DIR

Write-Host ""
Write-Host "==========================================" 
Write-Host "Wszystkie komponenty zostały uruchomione!"
Write-Host ""
Write-Host "Sprawdź terminale które się otworzyły."
Write-Host ""
Write-Host "Dostępne adresy:"
Write-Host "  📊 FastAPI:     http://localhost:8000/docs"
Write-Host "  🔌 Spring Boot: http://localhost:8080/api/health"
Write-Host "  🌐 Frontend:     http://localhost:3000"
Write-Host ""
Write-Host "Aby zatrzymać, zamknij terminale lub użyj Ctrl+C w każdym z nich."
Write-Host "=========================================="