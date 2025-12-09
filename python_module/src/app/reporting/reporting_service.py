from dataclasses import dataclass
from datetime import datetime
from typing import Optional, Dict

from app.analysis.models import EnergyStats
from app.prediction.models import Prediction


@dataclass
class Report:
    id: Optional[int]
    type: str
    sensor_id: int
    start_time: datetime
    end_time: datetime
    stats_id: Optional[int]
    prediction_id: Optional[int]
    text_summary: str
    plots_paths: Dict[str, str]
    created_at: datetime


class ReportRepository:
    """Interfejs repozytorium raportów."""

    def save(self, report: Report) -> Report:  # pragma: no cover - interfejs
        raise NotImplementedError


class PlotGenerator:
    """Interfejs generatora wykresów (np. matplotlib)."""

    def generate_energy_plot(self, stats: EnergyStats) -> str:  # pragma: no cover - interfejs
        raise NotImplementedError

    def generate_prediction_plot(self, stats: EnergyStats, prediction: Prediction) -> str:  # pragma: no cover - interfejs
        raise NotImplementedError


class Reporting:
    def __init__(self, report_repo: ReportRepository, plot_generator: PlotGenerator):
        self.report_repo = report_repo
        self.plot_generator = plot_generator

    def generateEnergyPlots(self, energyStats: EnergyStats) -> Dict[str, str]:
        energy_plot_path = self.plot_generator.generate_energy_plot(energyStats)
        return {"energy_plot": energy_plot_path}

    def generatePredictionPlots(
        self, energyStats: EnergyStats, prediction: Prediction
    ) -> Dict[str, str]:
        prediction_plot_path = self.plot_generator.generate_prediction_plot(
            energyStats, prediction
        )
        return {"prediction_plot": prediction_plot_path}

    def generateReport(
        self,
        sensor_id: int,
        energyStats: EnergyStats,
        prediction: Optional[Prediction] = None,
    ) -> Report:
        plots_paths = self.generateEnergyPlots(energyStats)
        stats_id = energyStats.id
        prediction_id = prediction.id if prediction else None
        if prediction:
            plots_paths.update(self.generatePredictionPlots(energyStats, prediction))

        text_summary = (
            f"Average consumption: {energyStats.avg}, "
            f"Daily: {energyStats.daily}, Annual: {energyStats.annual}"
        )
        if prediction:
            text_summary += f". Predicted next day average: {prediction.value}"

        from datetime import timezone

        report = Report(
            id=None,
            type="ANALYSIS_WITH_PREDICTION" if prediction else "ANALYSIS",
            sensor_id=sensor_id,
            start_time=energyStats.start_time,
            end_time=energyStats.end_time,
            stats_id=stats_id,
            prediction_id=prediction_id,
            text_summary=text_summary,
            plots_paths=plots_paths,
            created_at=datetime.now(timezone.utc),
        )
        return report

    def saveToDataBase(self, report: Report) -> Report:
        return self.report_repo.save(report)

