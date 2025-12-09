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
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 7); // Ostatnie 7 dni
      
      const response = await dataApi.getMeasurements(
        sensorId,
        start.toISOString(),
        end.toISOString()
      );
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
      setSimulationData(response.data || []);
    } catch (err) {
      console.warn('Błąd ładowania danych symulacji:', err);
      // Nie ustawiamy pustej tablicy, żeby zachować poprzednie dane
    } finally {
      setSimulationLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
      const date = new Date(dateString);
      return date.toLocaleString('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
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
          {simulationLoading && <span className="loading-indicator">🔄 Ładowanie...</span>}
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
                      <span className={`battery-level ${record.batteryLevel >= 50 ? 'high' : record.batteryLevel >= 20 ? 'medium' : 'low'}`}>
                        {record.batteryLevel?.toFixed(1) || '0.0'}%
                      </span>
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

