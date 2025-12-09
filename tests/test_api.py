"""Skrypt testowy do weryfikacji API."""
import requests

BASE_URL = "http://localhost:8000"


def test_health():
    response = requests.get(f"{BASE_URL}/health")
    print(f"GET /health -> {response.status_code}")
    assert response.status_code == 200
    assert response.json()["status"] == "OK"


def test_analysis():
    payload = {
        "sensorId": 1,
        "startTime": "2025-11-01T00:00:00Z",
        "endTime": "2025-11-07T00:00:00Z"
    }
    response = requests.post(f"{BASE_URL}/analysis", json=payload)
    print(f"POST /analysis -> {response.status_code}")
    assert response.status_code == 200
    data = response.json()
    assert "stats" in data
    assert "reportId" in data


def test_analysis_invalid_range():
    payload = {
        "sensorId": 1,
        "startTime": "2025-11-07T00:00:00Z",
        "endTime": "2025-11-01T00:00:00Z"
    }
    response = requests.post(f"{BASE_URL}/analysis", json=payload)
    print(f"POST /analysis (invalid range) -> {response.status_code}")
    assert response.status_code == 400


def test_analysis_no_data():
    payload = {
        "sensorId": 1,
        "startTime": "2030-01-01T00:00:00Z",
        "endTime": "2030-01-07T00:00:00Z"
    }
    response = requests.post(f"{BASE_URL}/analysis", json=payload)
    print(f"POST /analysis (no data) -> {response.status_code}")
    assert response.status_code == 404


def test_prediction():
    payload = {
        "sensorId": 1,
        "modelId": 1,
        "modelType": "MOVING_AVG",
        "historyDays": 7
    }
    response = requests.post(f"{BASE_URL}/prediction", json=payload)
    print(f"POST /prediction -> {response.status_code}")
    assert response.status_code == 200
    assert "prediction" in response.json()


def test_prediction_invalid_model():
    payload = {
        "sensorId": 1,
        "modelId": 1,
        "modelType": "INVALID_MODEL",
        "historyDays": 7
    }
    response = requests.post(f"{BASE_URL}/prediction", json=payload)
    print(f"POST /prediction (invalid model) -> {response.status_code}")
    assert response.status_code in [400, 422]


def test_prediction_missing_fields():
    payload = {"sensorId": 1}
    response = requests.post(f"{BASE_URL}/prediction", json=payload)
    print(f"POST /prediction (missing fields) -> {response.status_code}")
    assert response.status_code == 422


def test_get_report():
    response = requests.get(f"{BASE_URL}/reports/1")
    print(f"GET /reports/1 -> {response.status_code}")
    if response.status_code == 200:
        assert "id" in response.json()


def test_get_report_not_found():
    response = requests.get(f"{BASE_URL}/reports/99999")
    print(f"GET /reports/99999 -> {response.status_code}")
    assert response.status_code == 404


if __name__ == "__main__":
    print("\nSZEBI API - Testy\n")

    tests = [
        ("Health", test_health),
        ("Analysis", test_analysis),
        ("Analysis - invalid range", test_analysis_invalid_range),
        ("Analysis - no data", test_analysis_no_data),
        ("Prediction", test_prediction),
        ("Prediction - invalid model", test_prediction_invalid_model),
        ("Prediction - missing fields", test_prediction_missing_fields),
        ("Get report", test_get_report),
        ("Get report - not found", test_get_report_not_found),
    ]

    passed = 0
    failed = 0

    for name, test_func in tests:
        try:
            test_func()
            passed += 1
        except requests.exceptions.ConnectionError:
            print(f"{name}: SKIPPED (API nie działa)\n")
        except AssertionError as e:
            print(f"{name}: FAILED - {e}\n")
            failed += 1
        except Exception as e:
            print(f"{name}: ERROR - {e}\n")
            failed += 1

    print(f"\nPrzeszlo: {passed}, Nie przeszlo: {failed}")
