from datetime import datetime, timezone
from typing import Optional
from fastapi import APIRouter, HTTPException, Query
import httpx
import os

router = APIRouter(prefix="/data", tags=["data"])

# URL Spring Boot backendu
SPRINGBOOT_URL = os.getenv("SPRINGBOOT_URL", "http://localhost:8080")


@router.get("/measurements")
def get_measurements(
    sensorId: int = Query(..., description="ID sensora"),
    start: Optional[str] = Query(None, description="Data początkowa (ISO 8601)"),
    end: Optional[str] = Query(None, description="Data końcowa (ISO 8601)"),
):
    """Zwraca dane w formacie 'measurements', ale źródłem są wyniki symulacji (getSimulationResults)."""

    def _parse_iso(dt_str: str, field_name: str) -> datetime:
        try:
            dt = datetime.fromisoformat(dt_str.replace("Z", "+00:00"))
        except ValueError:
            raise HTTPException(
                status_code=400,
                detail={"code": "INVALID_DATE", "message": f"Invalid {field_name} date format: {dt_str}"},
            )
        # jeśli brak strefy, traktujemy jako UTC
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        return dt

    try:
        start_dt = _parse_iso(start, "start") if start else None
        end_dt = _parse_iso(end, "end") if end else None

        # Pobierz wyniki symulacji z Spring Boot
        with httpx.Client() as client:
            response = client.get(f"{SPRINGBOOT_URL}/api/data/simulation/results", timeout=10.0)
            response.raise_for_status()
            simulation_results = response.json() or []

        # Mapowanie: rekord symulacji -> rekord 'measurement'
        measurements = []
        for r in simulation_results:
            period_start = r.get("periodStart")
            rec_dt: Optional[datetime] = None
            if isinstance(period_start, str) and period_start:
                try:
                    rec_dt = datetime.fromisoformat(period_start.replace("Z", "+00:00"))
                    if rec_dt.tzinfo is None:
                        rec_dt = rec_dt.replace(tzinfo=timezone.utc)
                except ValueError:
                    # Jeśli Spring zwróci format nie-ISO, nie filtrujemy po dacie, ale nadal zwracamy rekord
                    rec_dt = None

            # Filtrowanie po start/end jeśli podano (porównujemy periodStart)
            if start_dt and rec_dt and rec_dt < start_dt:
                continue
            if end_dt and rec_dt and rec_dt > end_dt:
                continue

            measurements.append(
                {
                    "id": r.get("id", r.get("periodNumber")),
                    "timestamp": period_start or r.get("periodEnd") or datetime.now(timezone.utc).isoformat(),
                    "sensorId": sensorId,
                    "gridConsumption": r.get("gridConsumption", 0),
                    # historycznie było 'powerOutput' – w symulacji najbliższe znaczeniowo jest pvProduction
                    "powerOutput": r.get("pvProduction", 0),
                    "gridFeedIn": r.get("gridFeedIn", 0),
                    # Dodatkowe pola, które frontend i tak potrafi wykorzystać
                    "pvProduction": r.get("pvProduction", 0),
                    "batteryLevel": r.get("batteryLevel", 0),
                    "batteryCapacity": r.get("batteryCapacity"),
                    "periodNumber": r.get("periodNumber"),
                    "periodEnd": r.get("periodEnd"),
                }
            )

        return measurements
    except httpx.HTTPError as e:
        raise HTTPException(
            status_code=503,
            detail={"code": "SPRINGBOOT_UNAVAILABLE", "message": f"Spring Boot service is not available: {str(e)}"},
        )
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"code": "INTERNAL_ERROR", "message": f"Internal server error: {str(e)}"},
        )


@router.get("/simulation/results")
def get_simulation_results():
    """
    Pobiera wyniki ostatniej symulacji z SimulationManager.
    Tablica zawiera 6 rekordów (po jednym na każdy okres).

    Wyniki pobierane są z Spring Boot endpoint /api/data/simulation/results,
    który z kolei pobiera je z SimulationManager.getSimulationResults().
    """
    try:
        # Wywołaj Spring Boot endpoint
        with httpx.Client() as client:
            response = client.get(f"{SPRINGBOOT_URL}/api/data/simulation/results", timeout=10.0)
            response.raise_for_status()
            return response.json()
    except httpx.HTTPError as e:
        raise HTTPException(
            status_code=503,
            detail={"code": "SPRINGBOOT_UNAVAILABLE", "message": f"Spring Boot service is not available: {str(e)}"}
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"code": "INTERNAL_ERROR", "message": f"Internal server error: {str(e)}"}
        )
