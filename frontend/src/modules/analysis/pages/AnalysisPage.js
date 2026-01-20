import React, { useState } from 'react';
import { analysisApi, dataApi } from '../../../services/api';
import EnergyChart from '../components/EnergyChart';
import './AnalysisPage.css';

const AnalysisPage = () => {
  const [sensorId, setSensorId] = useState(1);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);
  const [measurements, setMeasurements] = useState([]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Konwertuj daty na format ISO 8601 z timezone
      const startISO = new Date(startTime).toISOString();
      const endISO = new Date(endTime).toISOString();
      
      const response = await analysisApi.runAnalysis(sensorId, startISO, endISO);
      setResult(response.data);

      // Pobierz dane pomiarowe do wykresu
      const measurementsResponse = await dataApi.getMeasurements(sensorId, startISO, endISO);
      setMeasurements(measurementsResponse.data || []);
    } catch (err) {
      console.error('Błąd analizy:', err);
      setError(err.response?.data?.message || err.message || 'Wystąpił błąd podczas analizy');
    } finally {
      setLoading(false);
    }
  };

  // Ustaw domyślne daty (ostatnie 7 dni)
  React.useEffect(() => {
    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 7);
    
    setEndTime(end.toISOString().slice(0, 16));
    setStartTime(start.toISOString().slice(0, 16));
  }, []);

  return (
    <div className="panel">
      <h2>Analiza Zużycia Energii</h2>
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>ID Sensora</label>
          <input
            type="number"
            value={sensorId}
            onChange={(e) => setSensorId(parseInt(e.target.value))}
            required
            min="1"
          />
        </div>

        <div className="form-group">
          <label>Data początkowa</label>
          <input
            type="datetime-local"
            value={startTime}
            onChange={(e) => setStartTime(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label>Data końcowa</label>
          <input
            type="datetime-local"
            value={endTime}
            onChange={(e) => setEndTime(e.target.value)}
            required
          />
        </div>

        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Przetwarzanie...' : 'Uruchom analizę'}
        </button>
      </form>

      {error && <div className="error">{error}</div>}

      {result && (
        <div className="results">
          <div className="success">Analiza zakończona pomyślnie!</div>
          
          <div className="stats-grid">
            <div className="stat-card">
              <h3>Średnie zużycie</h3>
              <div className="value">{result.stats.avg.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Dzienne zużycie</h3>
              <div className="value">{result.stats.daily.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Roczne zużycie</h3>
              <div className="value">{result.stats.annual.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Minimum</h3>
              <div className="value">{result.stats.min.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Maximum</h3>
              <div className="value">{result.stats.max.toFixed(2)} kWh</div>
            </div>
          </div>

          {measurements.length > 0 && (
            <div className="chart-container">
              <h3>Wykres zużycia energii</h3>
              <EnergyChart measurements={measurements} />
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AnalysisPage;

