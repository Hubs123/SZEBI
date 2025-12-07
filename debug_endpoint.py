"""Test endpointu /analysis bezpośrednio."""
import sys
sys.path.insert(0, r'C:\Users\User\PycharmProjects\SZEBI')

from datetime import datetime
from src.app.api.schemas import AnalysisRequest
from src.app.api.analysis_endpoints import run_analysis

print("Test wywołania endpointu run_analysis...")

request = AnalysisRequest(
    sensorId=1,
    startTime=datetime(2025, 12, 1, 0, 0, 0),
    endTime=datetime(2025, 12, 7, 0, 0, 0),
)

print(f"Request: sensorId={request.sensorId}, startTime={request.startTime}, endTime={request.endTime}")

try:
    response = run_analysis(request)
    print("\n✓ Response otrzymana!")
    print(f"  - Report ID: {response.reportId}")
    print(f"  - Stats ID: {response.stats.id}")
    print(f"  - Avg: {response.stats.avg:.2f} kWh")
    print(f"  - Daily: {response.stats.daily:.2f} kWh")
    print(f"  - Plots: {response.plots}")
    print(f"  - Save succeeded: {response.saveSucceeded}")
except Exception as e:
    print(f"\n✗ Błąd: {e}")
    import traceback
    traceback.print_exc()

