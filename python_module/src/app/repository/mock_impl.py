# """Implementacje repozytoriów z zamockowanymi danymi (in-memory)."""
#
# from datetime import datetime
# from typing import List, Optional, Dict
#
# from src.app.analysis.models import Measurement, EnergyStats
# from src.app.prediction.models import Prediction
# from src.app.reporting.reporting_service import Report
#
#
# class InMemoryMeasurementRepository:
#     """Repository pomiarów z danymi mock."""
#
#     def __init__(self):
#         # Mock data - przykładowe pomiary dla sensor_id=1
#         self.measurements: List[Measurement] = self._generate_mock_measurements()
#         self.next_id = len(self.measurements) + 1
#
#     def _generate_mock_measurements(self) -> List[Measurement]:
#         """Generuje przykładowe dane pomiarowe."""
#         from datetime import timedelta, timezone
#
#         measurements = []
#         # Używamy UTC timezone, żeby być kompatybilnym z API (offset-aware)
#         base_date = datetime(2025, 11, 1, 0, 0, 0, tzinfo=timezone.utc)
#
#         # Generujemy pomiary co godzinę przez 60 dni
#         for day in range(60):
#             for hour in range(24):
#                 timestamp = base_date + timedelta(days=day, hours=hour)
#
#                 # Symulacja zmiennego zużycia: więcej w dzień, mniej w nocy
#                 if 6 <= hour <= 22:
#                     consumption = 2.5 + (hour % 5) * 0.5  # 2.5 - 4.5 kWh w dzień
#                 else:
#                     consumption = 0.8 + (hour % 3) * 0.2  # 0.8 - 1.2 kWh w nocy
#
#                 measurements.append(
#                     Measurement(
#                         id=len(measurements) + 1,
#                         timestamp=timestamp,
#                         power_output=consumption * 1000,  # W
#                         grid_feed_in=0.1 * consumption,
#                         grid_consumption=consumption,
#                     )
#                 )
#
#         return measurements
#
#     def get_measurements(
#         self, sensor_id: int, start: datetime, end: datetime
#     ) -> List[Measurement]:
#         """Pobiera pomiary w zadanym zakresie czasu."""
#         return [
#             m for m in self.measurements
#             if start <= m.timestamp <= end
#         ]
#
#     def get_all_for_sensor(self, sensor_id: int) -> List[Measurement]:
#         """Pobiera wszystkie pomiary dla sensora."""
#         return self.measurements.copy()
#
#
# class InMemoryEnergyStatsRepository:
#     """Repository statystyk energii."""
#
#     def __init__(self):
#         self.stats: Dict[int, EnergyStats] = {}
#         self.next_id = 1
#
#     def save(self, stats: EnergyStats) -> EnergyStats:
#         """Zapisuje statystyki i zwraca z przypisanym ID."""
#         if stats.id is None:
#             stats.id = self.next_id
#             self.next_id += 1
#
#         # Tworzymy nową instancję z ID (dataclass jest immutable częściowo)
#         saved_stats = EnergyStats(
#             id=stats.id,
#             sensor_id=stats.sensor_id,
#             start_time=stats.start_time,
#             end_time=stats.end_time,
#             avg=stats.avg,
#             daily=stats.daily,
#             annual=stats.annual,
#             min=stats.min,
#             max=stats.max,
#         )
#         self.stats[saved_stats.id] = saved_stats
#         return saved_stats
#
#     def get(self, id: int) -> Optional[EnergyStats]:
#         """Pobiera statystyki po ID."""
#         return self.stats.get(id)
#
#
# class InMemoryPredictionRepository:
#     """Repository predykcji."""
#
#     def __init__(self):
#         self.predictions: Dict[int, Prediction] = {}
#         self.next_id = 1
#
#     def save(self, prediction: Prediction) -> Prediction:
#         """Zapisuje predykcję i zwraca z przypisanym ID."""
#         if prediction.id is None:
#             prediction.id = self.next_id
#             self.next_id += 1
#
#         saved_prediction = Prediction(
#             id=prediction.id,
#             sensor_id=prediction.sensor_id,
#             timestamp=prediction.timestamp,
#             predicted_for_date=prediction.predicted_for_date,
#             value=prediction.value,
#             model_id=prediction.model_id,
#         )
#         self.predictions[saved_prediction.id] = saved_prediction
#         return saved_prediction
#
#     def get(self, id: int) -> Optional[Prediction]:
#         """Pobiera predykcję po ID."""
#         return self.predictions.get(id)
#
#
# class InMemoryReportRepository:
#     """Repository raportów."""
#
#     def __init__(self):
#         self.reports: Dict[int, Report] = {}
#         self.next_id = 1
#
#     def save(self, report: Report) -> Report:
#         """Zapisuje raport i zwraca z przypisanym ID."""
#         if report.id is None:
#             report.id = self.next_id
#             self.next_id += 1
#
#         saved_report = Report(
#             id=report.id,
#             type=report.type,
#             sensor_id=report.sensor_id,
#             start_time=report.start_time,
#             end_time=report.end_time,
#             stats_id=report.stats_id,
#             prediction_id=report.prediction_id,
#             text_summary=report.text_summary,
#             plots_paths=report.plots_paths,
#             created_at=report.created_at,
#         )
#         self.reports[saved_report.id] = saved_report
#         return saved_report
#
#     def get(self, id: int) -> Optional[Report]:
#         """Pobiera raport po ID."""
#         return self.reports.get(id)
#
#
# class MockPlotGenerator:
#     """Generator wykresów (mock - zwraca ścieżki placeholder)."""
#
#     def generate_energy_plot(self, stats: EnergyStats) -> str:
#         """Generuje wykres energii (mock)."""
#         return f"/plots/energy_{stats.sensor_id}_{stats.start_time.isoformat()}.png"
#
#     def generate_prediction_plot(self, stats: EnergyStats, prediction: Prediction) -> str:
#         """Generuje wykres predykcji (mock)."""
#         return f"/plots/prediction_{prediction.sensor_id}_{prediction.id}.png"
#
#
# # Singletony dla uproszczenia (w prawdziwej aplikacji użyj Depends z FastAPI)
# _measurement_repo = InMemoryMeasurementRepository()
# _energy_stats_repo = InMemoryEnergyStatsRepository()
# _prediction_repo = InMemoryPredictionRepository()
# _report_repo = InMemoryReportRepository()
# _plot_generator = MockPlotGenerator()
#
#
# def get_measurement_repo() -> InMemoryMeasurementRepository:
#     return _measurement_repo
#
#
# def get_energy_stats_repo() -> InMemoryEnergyStatsRepository:
#     return _energy_stats_repo
#
#
# def get_prediction_repo() -> InMemoryPredictionRepository:
#     return _prediction_repo
#
#
# def get_report_repo() -> InMemoryReportRepository:
#     return _report_repo
#
#
# def get_plot_generator() -> MockPlotGenerator:
#     return _plot_generator
#
