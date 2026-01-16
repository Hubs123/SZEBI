from dataclasses import dataclass
from datetime import datetime, date
from enum import Enum
from typing import Optional


class PredictionModelType(str, Enum):
    SIMPLE_AVG = "SIMPLE_AVG"
    MOVING_AVG = "MOVING_AVG"
    LINEAR_TREND = "LINEAR_TREND"


@dataclass
class Prediction:
    id: Optional[int]
    sensor_id: int
    timestamp: datetime  # kiedy obliczono
    predicted_for_date: date
    value: float
    model_id: int

    def saveToDataBase(self, repo: "PredictionRepository") -> "Prediction":
        return repo.save(self)


@dataclass
class PredictionConfig:
    moving_avg_window_days: int = 7
    history_days_for_linear: int = 30

