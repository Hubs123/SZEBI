from datetime import datetime
from typing import List

import pandas as pd

from .data_manager import MeasurementRepositoryProtocol
from .models import Measurement, TimeRange, EnergyStats


class NoDataError(Exception):
    pass


class EnergyAnalyzer:
    def __init__(self, sensor_id: int, measurement_repo: MeasurementRepositoryProtocol):
        self.sensor_id = sensor_id
        self.measurement_repo = measurement_repo

    def _to_dataframe(self, measurements: List[Measurement]) -> pd.DataFrame:
        if not measurements:
            raise NoDataError("No measurements provided")
        return pd.DataFrame(
            [
                {
                    "timestamp": m.timestamp,
                    "grid_consumption": m.grid_consumption,
                }
                for m in measurements
            ]
        ).set_index("timestamp").sort_index()

    def averageConsumption(self, time_range: TimeRange) -> EnergyStats:
        measurements = self.measurement_repo.get_measurements(
            sensor_id=self.sensor_id,
            start=time_range.start,
            end=time_range.end,
        )
        if not measurements:
            raise NoDataError("No measurements in given range")

        df = self._to_dataframe(measurements)

        avg = float(df["grid_consumption"].mean())
        daily_series = df["grid_consumption"].resample("D").sum()
        daily = float(daily_series.mean()) if not daily_series.empty else 0.0
        annual = daily * 365.0
        min_val = float(df["grid_consumption"].min())
        max_val = float(df["grid_consumption"].max())

        return EnergyStats(
            id=None,
            sensor_id=self.sensor_id,
            start_time=time_range.start,
            end_time=time_range.end,
            avg=avg,
            daily=daily,
            annual=annual,
            min=min_val,
            max=max_val,
        )

    def dailyConsumption(self, time_range: TimeRange) -> EnergyStats:
        return self.averageConsumption(time_range)

    def annualConsumption(self, time_range: TimeRange) -> EnergyStats:
        return self.averageConsumption(time_range)

    def lowestConsumption(self) -> EnergyStats:
        measurements = self.measurement_repo.get_all_for_sensor(self.sensor_id)
        if not measurements:
            raise NoDataError("No measurements for sensor")
        df = self._to_dataframe(measurements)
        min_ts = df["grid_consumption"].idxmin()
        min_val = float(df["grid_consumption"].min())
        tr = TimeRange(start=min_ts, end=min_ts)
        return EnergyStats(
            id=None,
            sensor_id=self.sensor_id,
            start_time=tr.start,
            end_time=tr.end,
            avg=min_val,
            daily=min_val,
            annual=min_val * 365.0,
            min=min_val,
            max=min_val,
        )

    def highestConsumption(self) -> EnergyStats:
        measurements = self.measurement_repo.get_all_for_sensor(self.sensor_id)
        if not measurements:
            raise NoDataError("No measurements for sensor")
        df = self._to_dataframe(measurements)
        max_ts = df["grid_consumption"].idxmax()
        max_val = float(df["grid_consumption"].max())
        tr = TimeRange(start=max_ts, end=max_ts)
        return EnergyStats(
            id=None,
            sensor_id=self.sensor_id,
            start_time=tr.start,
            end_time=tr.end,
            avg=max_val,
            daily=max_val,
            annual=max_val * 365.0,
            min=max_val,
            max=max_val,
        )

