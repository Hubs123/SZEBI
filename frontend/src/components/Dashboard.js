import React, { useState, useEffect, useRef } from 'react';
import { dataApi } from '../services/api';
import EnergyChart from './EnergyChart';
import './Dashboard.css';

const Dashboard = () => {
  const [sensorId] = useState(1);
  const [measurements, setMeasurements] = useState([]);
  const [simulationData, setSimulationData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [simulationLoading, setSimulationLoading] = useState(false);
  const [simulationRunning, setSimulationRunning] = useState(false);
  const intervalRef = useRef(null);

  useEffect(() => {
    loadData();
    loadSimulationData();
    
    // Ustaw interwał na odświeżanie danych symulacji co 3 sekundy
    intervalRef.current = setInterval(() => {
      loadSimulationData();
    }, 3000);

    // Cleanup: wyczyść interwał przy unmount
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadData = async () => {
    try {
      let response;

      // Spróbuj pobrać wyniki symulacji najpierw
      try {
        response = await dataApi.getSimulationResults();
        console.log('Dane załadowane z symulacji');
      } catch (simErr) {
        console.warn('Nie udało się pobrać wyników symulacji, powracam do pomiarów z bazy:', simErr);
        // Fallback na pomiary z bazy danych
        const end = new Date();
        const start = new Date();
        start.setDate(start.getDate() - 7); // Ostatnie 7 dni

        response = await dataApi.getMeasurements(
          sensorId,
          start.toISOString(),
          end.toISOString()
        );
      }

      setMeasurements(response.data || []);
    } catch (err) {
      console.error('Błąd ładowania danych:', err);
      setMeasurements([]);
    } finally {
      setLoading(false);
    }
  };

  const loadSimulationData = async () => {
    try {
      setSimulationLoading(true);
      const response = await dataApi.getSimulationResults();
      const rawData = response.data || [];
      
      console.log('Otrzymane dane symulacji:', rawData); // Debug log

      // Przekształć dane symulacji na format kompatybilny z measurements
      const transformedData = rawData.map(record => ({
        timestamp: record.periodStart ? (record.periodStart.includes(':') ? record.periodStart + ':00' : record.periodStart) : new Date().toISOString(),
        gridConsumption: record.gridConsumption || 0,
        gridFeedIn: record.gridFeedIn || 0,
        pvProduction: record.pvProduction || 0,
        batteryLevel: record.batteryLevel || 0,
        periodNumber: record.periodNumber,
        ...record // Zachowaj wszystkie oryginalne pola
      }));

      setSimulationData(rawData);
      setMeasurements(transformedData); // Ustaw measurements dla wykresu i statystyk
    } catch (err) {
      console.error('Błąd ładowania danych symulacji:', err);
      console.error('Szczegóły błędu:', err.response?.data || err.message);
      // Nie ustawiamy pustej tablicy, żeby zachować poprzednie dane
    } finally {
      setSimulationLoading(false);
    }
  };

  const runSimulation = async () => {
    try {
      setSimulationRunning(true);
      const response = await dataApi.runSimulation();
      console.log('Symulacja uruchomiona:', response.data);
      // Poczekaj chwilę i odśwież dane
      setTimeout(() => {
        loadSimulationData();
      }, 500);
    } catch (err) {
      console.error('Błąd uruchamiania symulacji:', err);
      console.error('Szczegóły błędu:', err.response?.data || err.message);
      alert('Nie udało się uruchomić symulacji. Sprawdź konsolę.');
    } finally {
      setSimulationRunning(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
      // Obsługa formatu "2025-12-10T00:00" (bez sekund i bez 'Z')
      let dateStr = dateString;
      if (dateStr.includes('T') && !dateStr.includes('Z') && !dateStr.includes('+')) {
        // Dodaj sekundy jeśli brakuje
        if (!dateStr.includes(':')) {
          dateStr = dateStr + 'T00:00:00';
        } else if (dateStr.match(/T\d{2}:\d{2}$/)) {
          dateStr = dateStr + ':00';
        }
      }
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) {
        return dateString; // Zwróć oryginalny string jeśli parsowanie się nie powiodło
      }
      return date.toLocaleString('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dateString;
    }
  };

  if (loading) {
    return <div className="panel loading">Ładowanie danych...</div>;
  }

  const stats = measurements.length > 0 ? {
    avg: measurements.reduce((sum, m) => sum + m.gridConsumption, 0) / measurements.length,
    min: Math.min(...measurements.map(m => m.gridConsumption)),
    max: Math.max(...measurements.map(m => m.gridConsumption)),
    total: measurements.reduce((sum, m) => sum + m.gridConsumption, 0),
  } : null;

  return (
    <div>
      <div className="panel">
        <h2>📈 Dashboard</h2>
        {stats && (
          <div className="stats-grid">
            <div className="stat-card">
              <h3>Średnie zużycie</h3>
              <div className="value">{stats.avg.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Minimum</h3>
              <div className="value">{stats.min.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Maximum</h3>
              <div className="value">{stats.max.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Całkowite zużycie</h3>
              <div className="value">{stats.total.toFixed(2)} kWh</div>
            </div>
          </div>
        )}

        {measurements.length > 0 && (
          <div className="chart-container">
            <h3>Zużycie energii - ostatnie 7 dni</h3>
            <EnergyChart measurements={measurements} />
          </div>
        )}
      </div>

      {/* Tabela danych symulacji */}
      <div className="panel">
        <div className="simulation-header">
          <h2>⚡ Dane Symulacji (Odświeżanie co 3s)</h2>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            {simulationLoading && <span className="loading-indicator">🔄 Ładowanie...</span>}
            <button 
              onClick={runSimulation} 
              disabled={simulationRunning}
              style={{
                padding: '8px 16px',
                backgroundColor: simulationRunning ? '#ccc' : '#4CAF50',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: simulationRunning ? 'not-allowed' : 'pointer',
                fontSize: '14px'
              }}
            >
              {simulationRunning ? 'Uruchamianie...' : '▶ Uruchom Symulację'}
            </button>
          </div>
        </div>
        
        {simulationData.length > 0 ? (
          <div className="simulation-table-container">
            <table className="simulation-table">
              <thead>
                <tr>
                  <th>Okres rozpoczęcia</th>
                  <th>Okres zakończenia</th>
                  <th>Zużycie z sieci (kWh)</th>
                  <th>Oddanie do sieci (kWh)</th>
                  <th>Produkcja PV (kWh)</th>
                  <th>Poziom baterii (%)</th>
                </tr>
              </thead>
              <tbody>
                {simulationData.map((record, index) => (
                  <tr key={index}>
                    <td>{formatDate(record.periodStart)}</td>
                    <td>{formatDate(record.periodEnd)}</td>
                    <td className="value-cell">{record.gridConsumption?.toFixed(3) || '0.000'}</td>
                    <td className="value-cell">{record.gridFeedIn?.toFixed(3) || '0.000'}</td>
                    <td className="value-cell">{record.pvProduction?.toFixed(3) || '0.000'}</td>
                    <td className="value-cell">
                      {(() => {
                        // batteryLevel jest w kWh, musimy przeliczyć na procent używając batteryCapacity
                        const batteryLevelKwh = record.batteryLevel || 0;
                        const batteryCapacity = record.batteryCapacity || 100; // domyślnie 100 kWh jeśli brak
                        const batteryLevelPercent = batteryCapacity > 0 ? (batteryLevelKwh / batteryCapacity) * 100 : 0;
                        const levelClass = batteryLevelPercent >= 50 ? 'high' : batteryLevelPercent >= 20 ? 'medium' : 'low';
                        return (
                          <span className={`battery-level ${levelClass}`}>
                            {batteryLevelPercent.toFixed(1)}%
                          </span>
                        );
                      })()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="no-data-message">
            {simulationLoading ? 'Ładowanie danych...' : 'Brak danych symulacji. Sprawdź czy symulacja jest uruchomiona.'}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;

