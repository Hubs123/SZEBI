#!/bin/bash

# Skrypt do uruchomienia wszystkich komponentów projektu
# Użyj: ./URUCHOM_PROJEKT.sh

echo "=========================================="
echo "Energy Analysis & Prediction System"
echo "=========================================="
echo ""
echo "Ten skrypt uruchomi wszystkie komponenty w osobnych terminalach"
echo ""
echo "Wymagane:"
echo "  - Python 3.8+"
echo "  - Java 17+"
echo "  - Maven 3.6+"
echo "  - Node.js 16+"
echo ""
read -p "Naciśnij Enter aby kontynuować lub Ctrl+C aby anulować..."

# Ścieżki (skrypt jest już w katalogu SZEBI)
SZEBI_DIR="$(cd "$(dirname "$0")" && pwd)"
SPRINGBOOT_DIR="$SZEBI_DIR"
FRONTEND_DIR="$SZEBI_DIR/frontend"

# Sprawdź czy macOS (dla osascript)
if [[ "$OSTYPE" == "darwin"* ]]; then
    USE_OSASCRIPT=true
else
    USE_OSASCRIPT=false
fi

# Funkcja uruchomienia w nowym terminalu (macOS)
launch_terminal() {
    local title=$1
    local command=$2
    local dir=$3
    
    if [ "$USE_OSASCRIPT" = true ]; then
        osascript -e "tell application \"Terminal\" to do script \"cd '$dir' && echo '=== $title ===' && $command\""
    else
        echo "Uruchom w osobnym terminalu:"
        echo "  cd $dir"
        echo "  $command"
    fi
}

echo ""
echo "Uruchamianie komponentów..."
echo ""

# 1. FastAPI
echo "1. Uruchamianie FastAPI (port 8000)..."
launch_terminal "FastAPI Backend" "python3 -m src.app.main" "$SZEBI_DIR"
sleep 2

# 2. Spring Boot
echo "2. Uruchamianie Spring Boot (port 8080)..."
launch_terminal "Spring Boot Gateway" "mvn spring-boot:run \"-Dmaven.test.skip=true\"" "$SPRINGBOOT_DIR"
sleep 2

# 3. React
echo "3. Uruchamianie React (port 3000)..."
launch_terminal "React Frontend" "npm start" "$FRONTEND_DIR"

echo ""
echo "=========================================="
echo "Wszystkie komponenty zostały uruchomione!"
echo ""
echo "Sprawdź terminale które się otworzyły."
echo ""
echo "Dostępne adresy:"
echo "  📊 FastAPI:    http://localhost:8000/docs"
echo "  🔌 Spring Boot: http://localhost:8080/api/health"
echo "  🌐 Frontend:    http://localhost:3000"
echo ""
echo "Aby zatrzymać, zamknij terminale lub użyj Ctrl+C w każdym z nich."
echo "=========================================="

Wymagane:
  - Python 3.8+
  - Java 17+
  - Maven 3.6+
  - Node.js 16+

Naciśnij Enter aby kontynuować lub Ctrl+C aby anulować...

Uruchamianie komponentów...

1. Uruchamianie FastAPI (port 8000)...
Uruchom w osobnym terminalu:
  cd /c/Users/User/PycharmProjects/SZEBI
  python3 -m src.app.main
2. Uruchamianie Spring Boot (port 8080)...
Uruchom w osobnym terminalu:
  cd /c/Users/User/PycharmProjects/SZEBI
  mvn spring-boot:run -Dmaven.test.skip=true

