"""Proste fabryki i interfejsy repozytoriów.

Ten moduł dostarcza interfejsy oraz funkcje fabrykujące
implementacje oparte o bazę danych PostgreSQL.
"""

from typing import Protocol, List, Optional
from datetime import datetime

from app.analysis.models import Measurement, EnergyStats
from app.prediction.models import Prediction
from app.reporting.reporting_service import ReportRepository, PlotGenerator

from app.repository.db_impl import (
    DbMeasurementRepository,
    DbEnergyStatsRepository,
    DbPredictionRepository,
    DbReportRepository,
    DbPlotGenerator,
    get_db_connection,
)


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


# Singletony / cache połączenia i repozytoriów
_db_conn = None
_db_measurement_repo: Optional[MeasurementRepository] = None
_db_energy_stats_repo: Optional[EnergyStatsRepository] = None
_db_prediction_repo: Optional[PredictionRepository] = None
_db_report_repo: Optional[ReportRepository] = None
_db_plot_generator: Optional[PlotGenerator] = None


def _get_db_conn():
    """Pobiera połączenie z bazą danych (singleton)."""
    global _db_conn
    if _db_conn is None:
        _db_conn = get_db_connection()
    return _db_conn


def get_measurement_repo() -> MeasurementRepository:
    """Zwraca repozytorium pomiarów oparte o bazę danych."""
    global _db_measurement_repo
    if _db_measurement_repo is None:
        _db_measurement_repo = DbMeasurementRepository(_get_db_conn())
    return _db_measurement_repo


def get_energy_stats_repo() -> EnergyStatsRepository:
    """Zwraca repozytorium statystyk energii oparte o bazę danych."""
    global _db_energy_stats_repo
    if _db_energy_stats_repo is None:
        _db_energy_stats_repo = DbEnergyStatsRepository(_get_db_conn())
    return _db_energy_stats_repo


def get_prediction_repo() -> PredictionRepository:
    """Zwraca repozytorium predykcji oparte o bazę danych."""
    global _db_prediction_repo
    if _db_prediction_repo is None:
        _db_prediction_repo = DbPredictionRepository(_get_db_conn())
    return _db_prediction_repo


def get_report_repo() -> ReportRepository:
    """Zwraca repozytorium raportów oparte o bazę danych."""
    global _db_report_repo
    if _db_report_repo is None:
        _db_report_repo = DbReportRepository(_get_db_conn())
    return _db_report_repo


def get_plot_generator() -> PlotGenerator:
    """Zwraca generator wykresów."""
    global _db_plot_generator
    if _db_plot_generator is None:
        _db_plot_generator = DbPlotGenerator()
    return _db_plot_generator


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
