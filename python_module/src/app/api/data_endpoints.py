from datetime import datetime
from typing import Optional
from fastapi import APIRouter, HTTPException, Query
from app.repository.interfaces import get_measurement_repo

router = APIRouter(prefix="/data", tags=["data"])


@router.get("/measurements")
def get_measurements(
    sensorId: int = Query(..., description="ID sensora"),
    start: Optional[str] = Query(None, description="Data początkowa (ISO 8601)"),
    end: Optional[str] = Query(None, description="Data końcowa (ISO 8601)"),
):
    """Pobiera pomiary dla danego sensora w zadanym zakresie czasu."""
    try:
        measurement_repo = get_measurement_repo()
        
        # Parsuj daty jeśli podane
        start_dt = None
        end_dt = None
        
        if start:
            try:
                start_dt = datetime.fromisoformat(start.replace('Z', '+00:00'))
            except ValueError:
                raise HTTPException(
                    status_code=400,
                    detail={"code": "INVALID_DATE", "message": f"Invalid start date format: {start}"}
                )
        
        if end:
            try:
                end_dt = datetime.fromisoformat(end.replace('Z', '+00:00'))
            except ValueError:
                raise HTTPException(
                    status_code=400,
                    detail={"code": "INVALID_DATE", "message": f"Invalid end date format: {end}"}
                )
        
        # Jeśli nie podano dat, zwróć wszystkie pomiary dla sensora
        if start_dt is None or end_dt is None:
            measurements = measurement_repo.get_all_for_sensor(sensorId)
        else:
            measurements = measurement_repo.get_measurements(sensorId, start_dt, end_dt)
        
        # Konwertuj do formatu JSON
        return [
            {
                "id": m.id,
                "timestamp": m.timestamp.isoformat(),
                "sensorId": sensorId,
                "gridConsumption": m.grid_consumption,
                "powerOutput": m.power_output,
                "gridFeedIn": m.grid_feed_in,
            }
            for m in measurements
        ]
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"code": "INTERNAL_ERROR", "message": f"Internal server error: {str(e)}"}
        )

