from dataclasses import dataclass
from datetime import datetime
from typing import List

from .models import Measurement, TimeRange


class InvalidTimeRangeError(Exception):
    pass


class MeasurementRepositoryProtocol:
    """Prosty protokół repozytorium pomiarów, implementacja będzie w warstwie DB."""

    def get_measurements(self, sensor_id: int, start: datetime, end: datetime) -> List[Measurement]:  # pragma: no cover - interfejs
        raise NotImplementedError


@dataclass
class DataManager:
    id_sensor: int
    timestamp_s: datetime
    timestamp_e: datetime
    measurement_repo: MeasurementRepositoryProtocol

    def _validate_range(self, start: datetime, end: datetime) -> TimeRange:
        tr = TimeRange(start=start, end=end)
        if not tr.is_valid():
            raise InvalidTimeRangeError("Start time must be before end time")
        return tr

    def uploadAcquisitionData(self, start: datetime, end: datetime) -> List[Measurement]:
        """Pobiera dane z bazy wspólnej z modułami w Javie."""
        tr = self._validate_range(start, end)
        return self.measurement_repo.get_measurements(self.id_sensor, tr.start, tr.end)

