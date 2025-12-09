from fastapi import APIRouter, HTTPException

from app.api.schemas import ReportResponse
from app.repository.interfaces import get_report_repo


router = APIRouter(prefix="/reports", tags=["reports"])


@router.get("/{report_id}", response_model=ReportResponse)
def get_report(report_id: int):
    """Pobiera zapisany raport po ID."""
    report_repo = get_report_repo()
    report = report_repo.get(report_id)

    if report is None:
        raise HTTPException(status_code=404, detail={"code": "REPORT_NOT_FOUND", "message": f"Report {report_id} not found"})

    return ReportResponse(
        id=report.id,
        type=report.type,
        sensorId=report.sensor_id,
        startTime=report.start_time,
        endTime=report.end_time,
        statsId=report.stats_id,
        predictionId=report.prediction_id,
        textSummary=report.text_summary,
        plotsPaths=report.plots_paths,
        createdAt=report.created_at,
    )

