from datetime import datetime

from fastapi import APIRouter, HTTPException

from app.api.schemas import PredictionRequest, PredictionApiResponse, PredictionSchema
from app.prediction.models import PredictionModelType
from app.prediction.predictor import (
    Predictor,
    ModelNotSelectedError,
    NoHistoryDataError,
)


router = APIRouter(prefix="/prediction", tags=["prediction"])


def _prediction_to_schema(prediction) -> PredictionSchema:
    return PredictionSchema(
        id=prediction.id,
        sensorId=prediction.sensor_id,
        timestamp=prediction.timestamp,
        predictedForDate=prediction.predicted_for_date,
        value=prediction.value,
        modelId=prediction.model_id,
    )


@router.post("", response_model=PredictionApiResponse)
def run_prediction(request: PredictionRequest):
    from app.repository.interfaces import (
        get_measurement_repo,
        get_prediction_repo,
        get_energy_stats_repo,
        get_report_repo,
        get_plot_generator,
    )
    from app.reporting.reporting_service import Reporting
    import traceback

    try:
        measurement_repo = get_measurement_repo()
        # cast typów dla analizatora/predictora
        from typing import cast
        from app.analysis.data_manager import MeasurementRepositoryProtocol
        measurement_repo = cast(MeasurementRepositoryProtocol, measurement_repo)

        prediction_repo = get_prediction_repo()
        energy_stats_repo = get_energy_stats_repo()
        report_repo = get_report_repo()
        plot_generator = get_plot_generator()

        from datetime import timezone as tz

        predictor = Predictor(
            timestamp=datetime.now(tz.utc),
            measurement_repo=measurement_repo,
            prediction_repo=prediction_repo,
        )

        # zastosuj okno historii z requestu, jeśli podane
        if request.historyDays is not None:
            predictor.config.moving_avg_window_days = request.historyDays

        try:
            predictor.selectModel(
                id_model=request.modelId,
                model_type=PredictionModelType(request.modelType),
            )
            predictor.loadModel()
        except ModelNotSelectedError as e:
            raise HTTPException(status_code=400, detail={"code": "MODEL_NOT_SELECTED", "message": str(e)})

        try:
            predicted_value = predictor.predictNextDayAverageConsumption(sensor_id=request.sensorId)
        except NoHistoryDataError as e:
            raise HTTPException(status_code=400, detail={"code": "NO_HISTORY_DATA", "message": str(e)})

        prediction = predictor.buildPredictionEntity(sensor_id=request.sensorId, predicted_value=predicted_value)

        # opcjonalnie: pobierz stats z ostatniego okresu dla raportu
        from app.analysis.energy_analyzer import EnergyAnalyzer, TimeRange

        analyzer = EnergyAnalyzer(sensor_id=request.sensorId, measurement_repo=measurement_repo)
        # zakres historii np. ostatnie N dni
        history_days = request.historyDays or predictor.config.moving_avg_window_days
        end = predictor.timestamp
        from datetime import timedelta

        start = end - timedelta(days=history_days)
        time_range = TimeRange(start=start, end=end)
        stats = analyzer.averageConsumption(time_range)
        stats = stats.saveToDataBase(energy_stats_repo)

        reporting = Reporting(report_repo=report_repo, plot_generator=plot_generator)
        report = reporting.generateReport(
            sensor_id=request.sensorId,
            energyStats=stats,
            prediction=prediction,
        )
        report = reporting.saveToDataBase(report)

        return PredictionApiResponse(
            prediction=_prediction_to_schema(prediction),
            plotUrl=report.plots_paths.get("prediction_plot", ""),
            saveSucceeded=True,
        )
    except HTTPException:
        raise
    except Exception as e:
        print(f"ERROR in /prediction endpoint: {e}")
        traceback.print_exc()
        raise HTTPException(
            status_code=500,
            detail={"code": "INTERNAL_ERROR", "message": f"Internal server error: {str(e)}"}
        )
