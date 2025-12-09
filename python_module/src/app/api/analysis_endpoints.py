
from fastapi import APIRouter, HTTPException

from app.analysis.data_manager import DataManager, InvalidTimeRangeError
from app.analysis.energy_analyzer import EnergyAnalyzer, NoDataError
from app.analysis.models import TimeRange, EnergyStats
from .schemas import AnalysisRequest, AnalysisResponse, EnergyStatsSchema


router = APIRouter(prefix="/analysis", tags=["analysis"])

def _energy_stats_to_schema(stats: EnergyStats) -> EnergyStatsSchema:
    return EnergyStatsSchema(
        id=stats.id,
        sensorId=stats.sensor_id,
        startTime=stats.start_time,
        endTime=stats.end_time,
        avg=stats.avg,
        daily=stats.daily,
        annual=stats.annual,
        min=stats.min,
        max=stats.max,
    )


@router.post("", response_model=AnalysisResponse)
def run_analysis(request: AnalysisRequest):
    # Placeholdery repozytoriów/serwisów - jak dostaniemy od symulatorow to uzupelnimy
    from app.repository.interfaces import (
        get_measurement_repo,
        get_energy_stats_repo,
        get_report_repo,
        get_plot_generator,
    )
    from app.reporting.reporting_service import Reporting
    import traceback

    try:
        measurement_repo = get_measurement_repo()
        energy_stats_repo = get_energy_stats_repo()
        report_repo = get_report_repo()
        plot_generator = get_plot_generator()

        data_manager = DataManager(
            id_sensor=request.sensorId,
            timestamp_s=request.startTime,
            timestamp_e=request.endTime,
            measurement_repo=measurement_repo,
        )

        try:
            # pobierz dane (chociaż EnergyAnalyzer i tak je pobierze z repo; to krok zgodny z UC)
            _ = data_manager.uploadAcquisitionData(request.startTime, request.endTime)
        except InvalidTimeRangeError as e:
            raise HTTPException(status_code=400, detail={"code": "INVALID_TIME_RANGE", "message": str(e)})

        time_range = TimeRange(start=request.startTime, end=request.endTime)
        analyzer = EnergyAnalyzer(sensor_id=request.sensorId, measurement_repo=measurement_repo)

        try:
            stats = analyzer.averageConsumption(time_range)
        except NoDataError as e:
            raise HTTPException(status_code=404, detail={"code": "NO_DATA", "message": str(e)})

        # zapis statystyk do bazy
        stats = stats.saveToDataBase(energy_stats_repo)

        reporting = Reporting(report_repo=report_repo, plot_generator=plot_generator)
        report = reporting.generateReport(sensor_id=request.sensorId, energyStats=stats)
        report = reporting.saveToDataBase(report)

        stats_schema = _energy_stats_to_schema(stats)

        return AnalysisResponse(
            stats=stats_schema,
            reportId=report.id,
            plots=report.plots_paths,
            saveSucceeded=True,
        )
    except HTTPException:
        raise
    except Exception as e:
        # Logowanie szczegółowego błędu
        print(f"ERROR in /analysis endpoint: {e}")
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail={"code": "INTERNAL_ERROR", "message": f"Internal server error: {str(e)}"}
        )

