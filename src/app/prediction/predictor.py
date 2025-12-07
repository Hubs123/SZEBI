from dataclasses import dataclass, field
from datetime import datetime, timedelta
from typing import Optional, List

from src.app.analysis.data_manager import MeasurementRepositoryProtocol
from src.app.analysis.models import Measurement

from .models import Prediction, PredictionModelType, PredictionConfig


class ModelNotSelectedError(Exception):
    pass


class NoHistoryDataError(Exception):
    pass


class PredictionRepository:
    """Interfejs repozytorium predykcji (do implementacji w warstwie DB)."""

    def save(self, prediction: Prediction) -> Prediction:  # pragma: no cover - interfejs
        raise NotImplementedError


@dataclass
class Predictor:
    timestamp: datetime
    measurement_repo: MeasurementRepositoryProtocol
    prediction_repo: PredictionRepository
    config: PredictionConfig = field(default_factory=PredictionConfig)
    id_model: Optional[int] = None
    model_type: Optional[PredictionModelType] = None

    def selectModel(self, id_model: int, model_type: PredictionModelType) -> None:
        self.id_model = id_model
        self.model_type = model_type

    def loadModel(self) -> None:
        if self.model_type is None or self.id_model is None:
            raise ModelNotSelectedError("Model must be selected before loading")
        # dla prostych modeli nic więcej nie trzeba robić

    def _get_history_for_last_days(self, sensor_id: int, days: int) -> List[Measurement]:
        end = self.timestamp
        start = end - timedelta(days=days)
        return self.measurement_repo.get_measurements(sensor_id, start, end)

    def _linear_regression_next_day(self, measurements: List[Measurement]) -> float:
        # prosty placeholder; w przyszłości można zaimplementować regresję liniową
        # aktualnie użyjemy średniej jako fallback
        if not measurements:
            raise NoHistoryDataError("No history data for linear regression")
        return sum(m.grid_consumption for m in measurements) / len(measurements)

    def predictNextDayAverageConsumption(self, sensor_id: int) -> float:
        if self.model_type is None:
            raise ModelNotSelectedError("Model is not selected")

        if self.model_type in (PredictionModelType.SIMPLE_AVG, PredictionModelType.MOVING_AVG):
            measurements = self._get_history_for_last_days(
                sensor_id, self.config.moving_avg_window_days
            )
            if not measurements:
                raise NoHistoryDataError("No history data")
            return sum(m.grid_consumption for m in measurements) / len(measurements)

        if self.model_type == PredictionModelType.LINEAR_TREND:
            measurements = self._get_history_for_last_days(
                sensor_id, self.config.history_days_for_linear
            )
            if not measurements:
                raise NoHistoryDataError("No history data")
            return self._linear_regression_next_day(measurements)

        raise ModelNotSelectedError("Unsupported model type")

    def buildPredictionEntity(self, sensor_id: int, predicted_value: float) -> Prediction:

        predicted_for = (self.timestamp + timedelta(days=1)).date()
        prediction = Prediction(
            id=None,
            sensor_id=sensor_id,
            timestamp=self.timestamp,
            predicted_for_date=predicted_for,
            value=predicted_value,
            model_id=self.id_model if self.id_model is not None else -1,
        )
        return self.prediction_repo.save(prediction)

