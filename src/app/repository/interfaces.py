"""Proste fabryki i interfejsy repozytoriów.

W prawdziwej aplikacji można tu podpiąć SQLAlchemy, ale na razie zostawiamy
interfejsy/funkcje pomocnicze, które można łatwo zaimplementować później.
"""

from typing import Protocol, List, Optional
from datetime import datetime

from src.app.analysis.models import Measurement, EnergyStats
from src.app.prediction.models import Prediction


class MeasurementRepository(Protocol):
    def get_measurements(self, sensor_id: int, start: datetime, end: datetime) -> List[Measurement]:  # pragma: no cover - interfejs
        ...

    def get_all_for_sensor(self, sensor_id: int) -> List[Measurement]:  # pragma: no cover - interfejs
        ...


class EnergyStatsRepository(Protocol):
    def save(self, stats: EnergyStats) -> EnergyStats:  # pragma: no cover - interfejs
        ...

    def get(self, id: int) -> Optional[EnergyStats]:  # pragma: no cover - interfejs
        ...


class PredictionRepository(Protocol):
    def save(self, prediction: Prediction) -> Prediction:  # pragma: no cover - interfejs
        ...

    def get(self, id: int) -> Optional[Prediction]:  # pragma: no cover - interfejs
        ...


# Placeholdery do wstrzykiwania zależności – można je potem podmienić na
# prawdziwe implementacje, np. korzystające z SQLAlchemy.

# Na razie używamy implementacji mock z mock_impl.py
from src.app.repository.mock_impl import (
    get_measurement_repo,
    get_energy_stats_repo,
    get_prediction_repo,
    get_report_repo,
    get_plot_generator,
)

__all__ = [
    "MeasurementRepository",
    "EnergyStatsRepository",
    "PredictionRepository",
    "get_measurement_repo",
    "get_energy_stats_repo",
    "get_prediction_repo",
    "get_report_repo",
    "get_plot_generator",
]

