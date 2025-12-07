from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import Optional


@dataclass(frozen=True)
class TimeRange:
    start: datetime
    end: datetime

    def is_valid(self) -> bool:
        return self.start < self.end

    def duration(self) -> timedelta:
        return self.end - self.start

    def contains(self, ts: datetime) -> bool:
        return self.start <= ts <= self.end


@dataclass
class Measurement:
    id: int
    timestamp: datetime
    power_output: float
    grid_feed_in: float
    grid_consumption: float


@dataclass
class EnergyStats:
    id: Optional[int]
    sensor_id: int
    start_time: datetime
    end_time: datetime
    avg: float
    daily: float
    annual: float
    min: float
    max: float

    def saveToDataBase(self, repo: "EnergyStatsRepository") -> "EnergyStats":
        # deleguje zapis do repozytorium
        return repo.save(self)
