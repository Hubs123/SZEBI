import React, { useState, useEffect } from 'react';
import { dataApi } from '../services/api';
import EnergyChart from './EnergyChart';
import './Dashboard.css';

const Dashboard = () => {
  const [sensorId] = useState(1);
  const [measurements, setMeasurements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
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
    </div>
  );
};

export default Dashboard;

