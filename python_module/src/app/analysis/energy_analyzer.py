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

    def _normalize_dt_to_range(self, ts, time_range: TimeRange):
        """Ujednolica tzinfo dla bezpiecznych porównań naive/aware.

        Założenie: jeśli datetime jest naive, traktujemy go jako UTC.
        """
        if ts.tzinfo is None:
            if time_range.start.tzinfo is not None:
                return ts.replace(tzinfo=time_range.start.tzinfo)
            if time_range.end.tzinfo is not None:
                return ts.replace(tzinfo=time_range.end.tzinfo)
        return ts

    def _normalize_range(self, time_range: TimeRange) -> TimeRange:
        # Jeśli request przyszedł jako naive datetime, potraktuj go jako UTC
        if time_range.start.tzinfo is None and time_range.end.tzinfo is None:
            import datetime as _dt

            return TimeRange(
                start=time_range.start.replace(tzinfo=_dt.timezone.utc),
                end=time_range.end.replace(tzinfo=_dt.timezone.utc),
            )
        return time_range

    def averageConsumption(self, time_range: TimeRange) -> EnergyStats:
        time_range = self._normalize_range(time_range)

        measurements = self.measurement_repo.get_simulation_results()
        if not measurements:
            raise NoDataError("No measurements in given range")

        measurements = [
            m
            for m in measurements
            if time_range.contains(self._normalize_dt_to_range(m.timestamp, time_range))
        ]
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
        measurements = self.measurement_repo.get_simulation_results()
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
        measurements = self.measurement_repo.get_simulation_results()
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
