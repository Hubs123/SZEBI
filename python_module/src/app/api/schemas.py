from datetime import datetime, date
from typing import Optional, Dict, Any, Literal

from pydantic import BaseModel


class TimeRangeSchema(BaseModel):
    start: datetime
    end: datetime


class AnalysisRequest(BaseModel):
    sensorId: int
    startTime: datetime
    endTime: datetime


class EnergyStatsSchema(BaseModel):
    id: Optional[int]
    sensorId: int
    startTime: datetime
    endTime: datetime
    avg: float
    daily: float
    annual: float
    min: float
    max: float


class AnalysisResponse(BaseModel):
    stats: EnergyStatsSchema
    reportId: int
    plots: Dict[str, str]
    saveSucceeded: bool


class PredictionRequest(BaseModel):
    sensorId: int
    modelId: int
    modelType: Literal["SIMPLE_AVG", "MOVING_AVG", "LINEAR_TREND"]
    historyDays: Optional[int] = None


class PredictionSchema(BaseModel):
    id: int
    sensorId: int
    timestamp: datetime
    predictedForDate: date
    value: float
    modelId: int


class PredictionApiResponse(BaseModel):
    prediction: PredictionSchema
    plotUrl: str
    saveSucceeded: bool


class ReportResponse(BaseModel):
    id: int
    type: str
    sensorId: int
    startTime: datetime
    endTime: datetime
    statsId: Optional[int]
    predictionId: Optional[int]
    textSummary: str
    plotsPaths: Dict[str, str]
    createdAt: datetime


class HealthResponse(BaseModel):
    status: str
    timestamp: datetime

