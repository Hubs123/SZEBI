"""Implementacje repozytoriów oparte o bazę PostgreSQL."""

from __future__ import annotations

from datetime import datetime, timezone
from typing import List, Optional

import os
import psycopg2
from psycopg2.extras import RealDictCursor, Json
from dotenv import load_dotenv
import httpx

from app.analysis.models import Measurement, EnergyStats
from app.prediction.models import Prediction
from app.reporting.reporting_service import Report

# Załaduj zmienne środowiskowe z .env
load_dotenv()

# URL Spring Boot backendu
SPRINGBOOT_URL = os.getenv("SPRINGBOOT_URL", "http://localhost:8080")

# --- Connection pool helpers (replace single persistent connection) ---
from psycopg2.pool import ThreadedConnectionPool
from contextlib import contextmanager
import time
import logging

_db_pool: Optional[ThreadedConnectionPool] = None


def init_db_pool():
    """Initialize a ThreadedConnectionPool singleton with simple retry/backoff."""
    global _db_pool
    if _db_pool is not None:
        return _db_pool

    db_url = os.getenv("DB_URL")
    db_user = os.getenv("DB_USER")
    db_password = os.getenv("DB_PASSWORD")

    if not db_url or not db_user or not db_password:
        raise RuntimeError(
            "Brak konfiguracji bazy danych. Ustaw zmienne środowiskowe:\n"
            "  - DB_URL (np. postgresql://host:port/dbname)\n"
            "  - DB_USER\n"
            "  - DB_PASSWORD\n"
        )

    if db_url.startswith("jdbc:"):
        db_url = db_url[len("jdbc:"):]

    min_conn = int(os.getenv("DB_POOL_MIN", "1"))
    max_conn = int(os.getenv("DB_POOL_MAX", "10"))
    retries = int(os.getenv("DB_POOL_CONNECT_RETRIES", "3"))
    backoff = float(os.getenv("DB_POOL_CONNECT_BACKOFF", "1"))

    last_exc = None
    for attempt in range(1, retries + 1):
        try:
            _db_pool = ThreadedConnectionPool(
                min_conn,
                max_conn,
                dsn=db_url,
                user=db_user,
                password=db_password,
                sslmode="require",
                cursor_factory=RealDictCursor,
            )
            logging.getLogger(__name__).info("Initialized DB pool (min=%s,max=%s)", min_conn, max_conn)
            return _db_pool
        except psycopg2.Error as e:
            last_exc = e
            logging.getLogger(__name__).warning("DB pool init attempt %s failed: %s", attempt, e)
            if attempt < retries:
                time.sleep(backoff * attempt)

    raise RuntimeError(f"Nie można połączyć się z bazą danych: {last_exc}") from last_exc


def get_db_pool() -> ThreadedConnectionPool:
    global _db_pool
    if _db_pool is None:
        _db_pool = init_db_pool()
    return _db_pool


@contextmanager
def connection_from_pool(pool: Optional[ThreadedConnectionPool] = None):
    """Yield a connection from the pool and ensure it is returned.

    If pool is None, uses the module-level pool.
    """
    if pool is None:
        pool = get_db_pool()
    conn = None
    try:
        conn = pool.getconn()
        yield conn
    except Exception:
        # attempt rollback on error
        try:
            if conn is not None and not conn.closed:
                conn.rollback()
        except Exception:
            pass
        raise
    finally:
        if conn is not None:
            try:
                pool.putconn(conn)
            except Exception:
                try:
                    conn.close()
                except Exception:
                    pass


# --- Repository implementations: accept a pool (or legacy single conn) and borrow per-operation ---
class DbMeasurementRepository:
    """Repozytorium pomiarów oparte o tabelę measurements w PostgreSQL."""

    def __init__(self, conn_or_pool):
        # keep attribute name for compatibility; store pool reference
        self.pool = conn_or_pool

    def get_measurements(self, sensor_id: int, start: datetime, end: datetime) -> List[Measurement]:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
                    cur.execute(
                        """
                        SELECT id, device_id, start_time, avg_power_w, daily_kwh, annual_kwh
                        FROM energy_stats
                        WHERE device_id = %s AND start_time BETWEEN %s AND %s
                        ORDER BY start_time
                        """,
                        (sensor_id, start, end),
                    )
                    rows = cur.fetchall()

            return [
                Measurement(
                    id=row['id'],
                    timestamp=row["start_time"],
                    power_output=row["avg_power_w"],
                    grid_feed_in=row["daily_kwh"],
                    grid_consumption=row["annual_kwh"],
                )
                for row in rows
            ]
        except psycopg2.Error as e:
            raise RuntimeError(f"Błąd pobierania pomiarów z bazy danych: {e}") from e

    def get_all_for_sensor(self, sensor_id: int) -> List[Measurement]:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
                    cur.execute(
                        """
                        SELECT id, device_id, start_time, avg_power_w, daily_kwh, annual_kwh
                        FROM energy_stats
                        WHERE device_id = %s
                        ORDER BY start_time
                        """,
                        (sensor_id,),
                    )
                    rows = cur.fetchall()

            return [
                Measurement(
                    id=row['id'],
                    timestamp=row["start_time"],
                    power_output=row["avg_power_w"],
                    grid_feed_in=row["daily_kwh"],
                    grid_consumption=row["annual_kwh"],
                )
                for row in rows
            ]
        except psycopg2.Error as e:
            raise RuntimeError(f"Błąd pobierania pomiarów z bazy danych: {e}") from e

    def get_simulation_results(self) -> List[Measurement]:
        try:
            with httpx.Client() as client:
                response = client.get(f"{SPRINGBOOT_URL}/api/data/simulation/results", timeout=10.0)
                response.raise_for_status()
                data = response.json()

            def _parse_dt(dt_str: str) -> datetime:
                dt = datetime.fromisoformat(dt_str.replace("Z", "+00:00"))
                if dt.tzinfo is None:
                    dt = dt.replace(tzinfo=timezone.utc)
                return dt

            measurements = []
            for record in data:
                period_start = record.get("periodStart")
                measurement = Measurement(
                    id=record.get("id"),
                    timestamp=_parse_dt(period_start) if period_start else datetime.now(timezone.utc),
                    power_output=record.get("pvProduction", 0.0),
                    grid_feed_in=record.get("gridFeedIn", 0.0),
                    grid_consumption=record.get("gridConsumption", 0.0),
                )
                measurements.append(measurement)

            return measurements
        except httpx.HTTPError as e:
            raise RuntimeError(f"Błąd pobierania wyników symulacji z Spring Boot: {e}") from e
        except Exception as e:
            raise RuntimeError(f"Błąd podczas konwersji wyników symulacji: {e}") from e


class DbEnergyStatsRepository:
    def __init__(self, conn_or_pool):
        self.pool = conn_or_pool

    def save(self, stats: EnergyStats) -> EnergyStats:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
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
                        conn.commit()
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
                        conn.commit()
            return stats
        except psycopg2.Error as e:
            try:
                # conn may be out of scope here; best effort rollback
                conn.rollback()
            except Exception:
                pass
            raise RuntimeError(f"Błąd zapisu statystyk do bazy danych: {e}") from e

    def get(self, id: int) -> Optional[EnergyStats]:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
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
    def __init__(self, conn_or_pool):
        self.pool = conn_or_pool

    def save(self, prediction: Prediction) -> Prediction:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
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
                        conn.commit()
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
                        conn.commit()
            return prediction
        except psycopg2.Error as e:
            try:
                conn.rollback()
            except Exception:
                pass
            raise RuntimeError(f"Błąd zapisu predykcji do bazy danych: {e}") from e

    def get(self, id: int) -> Optional[Prediction]:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
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
    def __init__(self, conn_or_pool):
        self.pool = conn_or_pool

    def save(self, report: Report) -> Report:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
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
                        conn.commit()
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
                        conn.commit()
            return report
        except psycopg2.Error as e:
            try:
                conn.rollback()
            except Exception:
                pass
            raise RuntimeError(f"Błąd zapisu raportu do bazy danych: {e}") from e

    def get(self, id: int) -> Optional[Report]:
        try:
            with connection_from_pool(self.pool) as conn:
                with conn.cursor() as cur:
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
    def generate_energy_plot(self, stats: EnergyStats) -> str:
        return f"/plots/energy_{stats.sensor_id}_{stats.start_time.isoformat()}.png"

    def generate_prediction_plot(self, stats: EnergyStats, prediction: Prediction) -> str:
        return f"/plots/prediction_{prediction.sensor_id}_{prediction.id}.png"

