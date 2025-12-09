"""Implementacje repozytoriów oparte o bazę PostgreSQL."""

from __future__ import annotations

from datetime import datetime
from typing import List, Optional

import os
import psycopg2
from psycopg2.extras import RealDictCursor, Json
from dotenv import load_dotenv

from app.analysis.models import Measurement, EnergyStats
from app.prediction.models import Prediction
from app.reporting.reporting_service import Report

# Załaduj zmienne środowiskowe z .env
load_dotenv()


def get_db_connection():
    """Tworzy nowe połączenie do bazy PostgreSQL na podstawie zmiennych środowiskowych.

    Oczekujemy, że w środowisku są ustawione zmienne:
    - DB_URL (JDBC / lub klasyczny URL PostgreSQL)
    - DB_USER
    - DB_PASSWORD
    
    Zmienne mogą być w pliku .env lub w środowisku systemowym.
    """
    db_url = os.getenv("DB_URL")
    db_user = os.getenv("DB_USER")
    db_password = os.getenv("DB_PASSWORD")

    if not db_url or not db_user or not db_password:
        raise RuntimeError(
            "Brak konfiguracji bazy danych. Ustaw zmienne środowiskowe:\n"
            "  - DB_URL (np. postgresql://host:port/dbname)\n"
            "  - DB_USER\n"
            "  - DB_PASSWORD\n"
            "Lub utwórz plik .env w katalogu SZEBI z tymi zmiennymi."
        )

    # Dopuszczamy format JDBC z .env i wyciągamy właściwy fragment dla psycopg2
    # Przykład: jdbc:postgresql://host:port/dbname
    if db_url.startswith("jdbc:"):
        db_url = db_url[len("jdbc:") :]

    try:
        conn = psycopg2.connect(
            db_url, 
            user=db_user, 
            password=db_password, 
            cursor_factory=RealDictCursor
        )
        return conn
    except psycopg2.Error as e:
        raise RuntimeError(f"Nie można połączyć się z bazą danych: {e}") from e


class DbMeasurementRepository:
    """Repozytorium pomiarów oparte o tabelę measurements w PostgreSQL.

    Ten szkielet zakłada istnienie tabeli measurements z kolumnami m.in.:
    - id bigserial primary key
    - device_id integer references devices(id)
    - timestamp timestamptz
    - power_output_w double precision
    - grid_feed_in_kwh double precision
    - grid_consumption_kwh double precision
    """

    def __init__(self, conn):
        self.conn = conn

    def get_measurements(self, sensor_id: int, start: datetime, end: datetime) -> List[Measurement]:
        try:
            with self.conn.cursor() as cur:
                cur.execute(
                    """
                    SELECT id, device_id, timestamp, power_output_w, grid_feed_in_kwh, grid_consumption_kwh
                    FROM measurements
                    WHERE device_id = %s AND timestamp BETWEEN %s AND %s
                    ORDER BY timestamp
                    """,
                    (sensor_id, start, end),
                )
                rows = cur.fetchall()

            return [
                Measurement(
                    id=row["id"],
                    timestamp=row["timestamp"],
                    power_output=row["power_output_w"],
                    grid_feed_in=row["grid_feed_in_kwh"],
                    grid_consumption=row["grid_consumption_kwh"],
                )
                for row in rows
            ]
        except psycopg2.Error as e:
            raise RuntimeError(f"Błąd pobierania pomiarów z bazy danych: {e}") from e

    def get_all_for_sensor(self, sensor_id: int) -> List[Measurement]:
        try:
            with self.conn.cursor() as cur:
                cur.execute(
                    """
                    SELECT id, device_id, timestamp, power_output_w, grid_feed_in_kwh, grid_consumption_kwh
                    FROM measurements
                    WHERE device_id = %s
                    ORDER BY timestamp
                    """,
                    (sensor_id,),
                )
                rows = cur.fetchall()

            return [
                Measurement(
                    id=row["id"],
                    timestamp=row["timestamp"],
                    power_output=row["power_output_w"],
                    grid_feed_in=row["grid_feed_in_kwh"],
                    grid_consumption=row["grid_consumption_kwh"],
                )
                for row in rows
            ]
        except psycopg2.Error as e:
            raise RuntimeError(f"Błąd pobierania pomiarów z bazy danych: {e}") from e


class DbEnergyStatsRepository:
    """Repozytorium statystyk energii (energy_stats)."""

    def __init__(self, conn):
        self.conn = conn

    def save(self, stats: EnergyStats) -> EnergyStats:
        try:
            with self.conn.cursor() as cur:
                if stats.id is None:
                    cur.execute(
                        """
                        INSERT INTO energy_stats (
                            device_id, start_time, end_time, avg_power_w, daily_kwh, annual_kwh, min_power_w, max_power_w
                        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                        RETURNING id
                        """,
                        (
                            stats.sensor_id,
                            stats.start_time,
                            stats.end_time,
                            stats.avg,
                            stats.daily,
                            stats.annual,
                            stats.min,
                            stats.max,
                        ),
                    )
                    new_id = cur.fetchone()["id"]
                    self.conn.commit()
                    stats.id = new_id
                else:
                    cur.execute(
                        """
                        UPDATE energy_stats
                        SET device_id = %s, start_time = %s, end_time = %s,
                            avg_power_w = %s, daily_kwh = %s, annual_kwh = %s,
                            min_power_w = %s, max_power_w = %s
                        WHERE id = %s
                        """,
                        (
                            stats.sensor_id,
                            stats.start_time,
                            stats.end_time,
                            stats.avg,
                            stats.daily,
                            stats.annual,
                            stats.min,
                            stats.max,
                            stats.id,
                        ),
                    )
                    self.conn.commit()
            return stats
        except psycopg2.Error as e:
            self.conn.rollback()
            raise RuntimeError(f"Błąd zapisu statystyk do bazy danych: {e}") from e

    def get(self, id: int) -> Optional[EnergyStats]:
        try:
            with self.conn.cursor() as cur:
                cur.execute(
                    """
                    SELECT id, device_id, start_time, end_time,
                           avg_power_w, daily_kwh, annual_kwh, min_power_w, max_power_w
                    FROM energy_stats
                    WHERE id = %s
                    """,
                    (id,),
                )
                row = cur.fetchone()

            if not row:
                return None

            return EnergyStats(
                id=row["id"],
                sensor_id=row["device_id"],
                start_time=row["start_time"],
                end_time=row["end_time"],
                avg=row["avg_power_w"],
                daily=row["daily_kwh"],
                annual=row["annual_kwh"],
                min=row["min_power_w"],
                max=row["max_power_w"],
            )
        except psycopg2.Error as e:
            raise RuntimeError(f"Błąd pobierania statystyk z bazy danych: {e}") from e


class DbPredictionRepository:
    """Repozytorium predykcji (predictions)."""

    def __init__(self, conn):
        self.conn = conn

    def save(self, prediction: Prediction) -> Prediction:
        try:
            with self.conn.cursor() as cur:
                if prediction.id is None:
                    cur.execute(
                        """
                        INSERT INTO predictions (
                            device_id, timestamp, predicted_for_date, value_kwh, model_id
                        ) VALUES (%s, %s, %s, %s, %s)
                        RETURNING id
                        """,
                        (
                            prediction.sensor_id,
                            prediction.timestamp,
                            prediction.predicted_for_date,
                            prediction.value,
                            prediction.model_id,
                        ),
                    )
                    new_id = cur.fetchone()["id"]
                    self.conn.commit()
                    prediction.id = new_id
                else:
                    cur.execute(
                        """
                        UPDATE predictions
                        SET device_id = %s, timestamp = %s, predicted_for_date = %s,
                            value_kwh = %s, model_id = %s
                        WHERE id = %s
                        """,
                        (
                            prediction.sensor_id,
                            prediction.timestamp,
                            prediction.predicted_for_date,
                            prediction.value,
                            prediction.model_id,
                            prediction.id,
                        ),
                    )
                    self.conn.commit()
            return prediction
        except psycopg2.Error as e:
            self.conn.rollback()
            raise RuntimeError(f"Błąd zapisu predykcji do bazy danych: {e}") from e

    def get(self, id: int) -> Optional[Prediction]:
        try:
            with self.conn.cursor() as cur:
                cur.execute(
                    """
                    SELECT id, device_id, timestamp, predicted_for_date, value_kwh, model_id
                    FROM predictions
                    WHERE id = %s
                    """,
                    (id,),
                )
                row = cur.fetchone()

            if not row:
                return None

            return Prediction(
                id=row["id"],
                sensor_id=row["device_id"],
                timestamp=row["timestamp"],
                predicted_for_date=row["predicted_for_date"],
                value=row["value_kwh"],
                model_id=row["model_id"],
            )
        except psycopg2.Error as e:
            raise RuntimeError(f"Błąd pobierania predykcji z bazy danych: {e}") from e


class DbReportRepository:
    """Repozytorium raportów (reports)."""

    def __init__(self, conn):
        self.conn = conn

    def save(self, report: Report) -> Report:
        try:
            with self.conn.cursor() as cur:
                if report.id is None:
                    cur.execute(
                        """
                        INSERT INTO reports (
                            type, device_id, start_time, end_time, stats_id, prediction_id,
                            text_summary, plots_paths, created_at
                        ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                        RETURNING id
                        """,
                        (
                            report.type,
                            report.sensor_id,
                            report.start_time,
                            report.end_time,
                            report.stats_id,
                            report.prediction_id,
                            report.text_summary,
                            Json(report.plots_paths),
                            report.created_at,
                        ),
                    )
                    new_id = cur.fetchone()["id"]
                    self.conn.commit()
                    report.id = new_id
                else:
                    cur.execute(
                        """
                        UPDATE reports
                        SET type = %s, device_id = %s, start_time = %s, end_time = %s,
                            stats_id = %s, prediction_id = %s, text_summary = %s,
                            plots_paths = %s, created_at = %s
                        WHERE id = %s
                        """,
                        (
                            report.type,
                            report.sensor_id,
                            report.start_time,
                            report.end_time,
                            report.stats_id,
                            report.prediction_id,
                            report.text_summary,
                            Json(report.plots_paths),
                            report.created_at,
                            report.id,
                        ),
                    )
                    self.conn.commit()
            return report
        except psycopg2.Error as e:
            self.conn.rollback()
            raise RuntimeError(f"Błąd zapisu raportu do bazy danych: {e}") from e

    def get(self, id: int) -> Optional[Report]:
        try:
            with self.conn.cursor() as cur:
                cur.execute(
                    """
                    SELECT id, type, device_id, start_time, end_time, stats_id, prediction_id,
                           text_summary, plots_paths, created_at
                    FROM reports
                    WHERE id = %s
                    """,
                    (id,),
                )
                row = cur.fetchone()

            if not row:
                return None

            return Report(
                id=row["id"],
                type=row["type"],
                sensor_id=row["device_id"],
                start_time=row["start_time"],
                end_time=row["end_time"],
                stats_id=row["stats_id"],
                prediction_id=row["prediction_id"],
                text_summary=row["text_summary"],
                plots_paths=row["plots_paths"],
                created_at=row["created_at"],
            )
        except psycopg2.Error as e:
            raise RuntimeError(f"Błąd pobierania raportu z bazy danych: {e}") from e


class DbPlotGenerator:
    """Docelowo generator wykresów zapisujący prawdziwe pliki.

    Na razie zachowuje się jak mock: generuje jedynie ścieżki do potencjalnych plików.
    """

    def generate_energy_plot(self, stats: EnergyStats) -> str:
        return f"/plots/energy_{stats.sensor_id}_{stats.start_time.isoformat()}.png"

    def generate_prediction_plot(self, stats: EnergyStats, prediction: Prediction) -> str:
        return f"/plots/prediction_{prediction.sensor_id}_{prediction.id}.png"

