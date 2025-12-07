"""Prosty test do debugowania błędu w endpointcie /analysis."""
import sys
sys.path.insert(0, r'C:\Users\User\PycharmProjects\SZEBI')

from datetime import datetime
from src.app.analysis.data_manager import DataManager
from src.app.analysis.energy_analyzer import EnergyAnalyzer
from src.app.analysis.models import TimeRange
from src.app.repository import (
    get_measurement_repo,
    get_energy_stats_repo,
)

print("Test 1: Inicjalizacja repozytoriów...")
measurement_repo = get_measurement_repo()
energy_stats_repo = get_energy_stats_repo()
print(f"✓ Repozytoria zainicjalizowane")
print(f"  - Liczba pomiarów mock: {len(measurement_repo.measurements)}")

print("\nTest 2: DataManager...")
data_manager = DataManager(
    id_sensor=1,
    timestamp_s=datetime(2025, 12, 1),
    timestamp_e=datetime(2025, 12, 7),
    measurement_repo=measurement_repo,
)
print("✓ DataManager utworzony")

print("\nTest 3: Pobieranie danych...")
try:
    measurements = data_manager.uploadAcquisitionData(
        start=datetime(2025, 12, 1, 0, 0, 0),
        end=datetime(2025, 12, 7, 0, 0, 0),
    )
    print(f"✓ Pobrano {len(measurements)} pomiarów")
    if measurements:
        print(f"  - Pierwszy: {measurements[0].timestamp} -> {measurements[0].grid_consumption} kWh")
        print(f"  - Ostatni: {measurements[-1].timestamp} -> {measurements[-1].grid_consumption} kWh")
except Exception as e:
    print(f"✗ Błąd: {e}")
    import traceback
    traceback.print_exc()

print("\nTest 4: EnergyAnalyzer...")
try:
    analyzer = EnergyAnalyzer(sensor_id=1, measurement_repo=measurement_repo)
    time_range = TimeRange(
        start=datetime(2025, 12, 1, 0, 0, 0),
        end=datetime(2025, 12, 7, 0, 0, 0),
    )
    stats = analyzer.averageConsumption(time_range)
    print(f"✓ Statystyki obliczone:")
    print(f"  - Średnia: {stats.avg:.2f} kWh")
    print(f"  - Dzienna: {stats.daily:.2f} kWh")
    print(f"  - Roczna: {stats.annual:.2f} kWh")
    print(f"  - Min: {stats.min:.2f} kWh")
    print(f"  - Max: {stats.max:.2f} kWh")
except Exception as e:
    print(f"✗ Błąd: {e}")
    import traceback
    traceback.print_exc()

print("\nTest 5: Zapis statystyk do repo...")
try:
    saved_stats = stats.saveToDataBase(energy_stats_repo)
    print(f"✓ Statystyki zapisane z ID: {saved_stats.id}")
except Exception as e:
    print(f"✗ Błąd: {e}")
    import traceback
    traceback.print_exc()

print("\n✅ Wszystkie testy zakończone")

