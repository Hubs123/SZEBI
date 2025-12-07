"""Skrypt testowy do weryfikacji API."""
import requests
import json

BASE_URL = "http://localhost:8000"

def test_health():
    """Test health check endpoint."""
    print("=" * 50)
    print("TEST 1: GET /health")
    print("=" * 50)

    response = requests.get(f"{BASE_URL}/health")
    print(f"Status: {response.status_code}")
    print(f"Response:\n{json.dumps(response.json(), indent=2)}")
    print()


def test_analysis():
    """Test analysis endpoint."""
    print("=" * 50)
    print("TEST 2: POST /analysis")
    print("=" * 50)

    payload = {
        "sensorId": 1,
        "startTime": "2025-12-01T00:00:00Z",
        "endTime": "2025-12-07T00:00:00Z"
    }

    print(f"Request:\n{json.dumps(payload, indent=2)}")

    response = requests.post(f"{BASE_URL}/analysis", json=payload)
    print(f"\nStatus: {response.status_code}")
    print(f"Response:\n{json.dumps(response.json(), indent=2)}")
    print()


def test_prediction():
    """Test prediction endpoint."""
    print("=" * 50)
    print("TEST 3: POST /prediction")
    print("=" * 50)

    payload = {
        "sensorId": 1,
        "modelId": 1,
        "modelType": "MOVING_AVG",
        "historyDays": 7
    }

    print(f"Request:\n{json.dumps(payload, indent=2)}")

    response = requests.post(f"{BASE_URL}/prediction", json=payload)
    print(f"\nStatus: {response.status_code}")
    print(f"Response:\n{json.dumps(response.json(), indent=2)}")
    print()


def test_get_report():
    """Test get report endpoint."""
    print("=" * 50)
    print("TEST 4: GET /reports/1")
    print("=" * 50)

    response = requests.get(f"{BASE_URL}/reports/1")
    print(f"Status: {response.status_code}")
    if response.status_code == 200:
        print(f"Response:\n{json.dumps(response.json(), indent=2)}")
    else:
        print(f"Response: {response.text}")
    print()


if __name__ == "__main__":
    print("\n SZEBI API - Testy Integracyjne\n")

    try:
        test_health()
        test_analysis()
        test_prediction()
        test_get_report()

        print("✅ Wszystkie testy zakończone pomyślnie!")

    except requests.exceptions.ConnectionError:
        print(" Błąd: Nie można połączyć się z API na http://localhost:8000")
        print("Sprawdź czy serwer jest uruchomiony: python -m app.main")
    except Exception as e:
        print(f" Błąd: {e}")

