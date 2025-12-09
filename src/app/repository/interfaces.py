"""Proste fabryki i interfejsy repozytoriów.

Ten moduł dostarcza interfejsy oraz funkcje fabrykujące
konkretne implementacje (mock lub DB) zależnie od konfiguracji środowiska.
"""

from typing import Protocol, List, Optional
from datetime import datetime
import os

from src.app.analysis.models import Measurement, EnergyStats
from src.app.prediction.models import Prediction
from src.app.reporting.reporting_service import ReportRepository, PlotGenerator


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


# Importy implementacji
# from src.app.repository.mock_impl import (
#     InMemoryMeasurementRepository,
#     InMemoryEnergyStatsRepository,
#     InMemoryPredictionRepository,
#     InMemoryReportRepository,
#     MockPlotGenerator,
# )
from src.app.repository.db_impl import (
    DbMeasurementRepository,
    DbEnergyStatsRepository,
    DbPredictionRepository,
    DbReportRepository,
    DbPlotGenerator,
    get_db_connection,
)


def _use_db() -> bool:
    """Sprawdza, czy powinniśmy używać implementacji DB.

    Sterowane zmienną środowiskową BACKEND_REPOSITORY_MODE=db.
    """
    return os.getenv("BACKEND_REPOSITORY_MODE", "mock").lower() == "db"


# Singletony / cache połączenia i repozytoriów
_db_conn = None
_db_measurement_repo: Optional[MeasurementRepository] = None
_db_energy_stats_repo: Optional[EnergyStatsRepository] = None
_db_prediction_repo: Optional[PredictionRepository] = None
_db_report_repo: Optional[ReportRepository] = None
_db_plot_generator: Optional[PlotGenerator] = None

# _mock_measurement_repo = InMemoryMeasurementRepository()
# _mock_energy_stats_repo = InMemoryEnergyStatsRepository()
# _mock_prediction_repo = InMemoryPredictionRepository()
# _mock_report_repo = InMemoryReportRepository()
# _mock_plot_generator = MockPlotGenerator()


def _get_db_conn():
    global _db_conn
    if _db_conn is None:
        _db_conn = get_db_connection()
    return _db_conn


def get_measurement_repo() -> MeasurementRepository:
    if _use_db():
        global _db_measurement_repo
        if _db_measurement_repo is None:
            _db_measurement_repo = DbMeasurementRepository(_get_db_conn())
        return _db_measurement_repo
    # return _mock_measurement_repo


def get_energy_stats_repo() -> EnergyStatsRepository:
    if _use_db():
        global _db_energy_stats_repo
        if _db_energy_stats_repo is None:
            _db_energy_stats_repo = DbEnergyStatsRepository(_get_db_conn())
        return _db_energy_stats_repo
    # return _mock_energy_stats_repo


def get_prediction_repo() -> PredictionRepository:
    if _use_db():
        global _db_prediction_repo
        if _db_prediction_repo is None:
            _db_prediction_repo = DbPredictionRepository(_get_db_conn())
        return _db_prediction_repo
    # return _mock_prediction_repo


def get_report_repo() -> ReportRepository:
    if _use_db():
        global _db_report_repo
        if _db_report_repo is None:
            _db_report_repo = DbReportRepository(_get_db_conn())
        return _db_report_repo
    # return _mock_report_repo


def get_plot_generator() -> PlotGenerator:
    if _use_db():
        global _db_plot_generator
        if _db_plot_generator is None:
            _db_plot_generator = DbPlotGenerator()
        return _db_plot_generator
    # return _mock_plot_generator


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
