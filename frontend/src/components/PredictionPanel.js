import React, { useState } from 'react';
import { predictionApi, dataApi } from '../services/api';
import PredictionChart from './PredictionChart';
import './PredictionPanel.css';

const PredictionPanel = () => {
  const [sensorId, setSensorId] = useState(1);
  const [modelId, setModelId] = useState(1);
  const [modelType, setModelType] = useState('MOVING_AVG');
  const [historyDays, setHistoryDays] = useState(7);
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
      const response = await predictionApi.runPrediction(
        sensorId,
        modelId,
        modelType,
        historyDays
      );
      setResult(response.data);

      // Dane do wykresu bierzemy z wyników symulacji (getSimulationResults), a nie z tabeli measurements.
      const simResponse = await dataApi.getSimulationResults();
      const rawData = simResponse.data || [];

      const transformedData = rawData.map((record, index) => ({
        timestamp: record.periodStart ? (record.periodStart.includes(':') ? record.periodStart + ':00' : record.periodStart) : `Okres ${index + 1}`,
        gridConsumption: record.gridConsumption || 0,
        gridFeedIn: record.gridFeedIn || 0,
        pvProduction: record.pvProduction || 0,
        batteryLevel: record.batteryLevel || 0,
        periodNumber: record.periodNumber,
        ...record
      }));

      setMeasurements(transformedData);
    } catch (err) {
      console.error('Błąd prognozy:', err);
      setError(err.response?.data?.message || err.message || 'Wystąpił błąd podczas prognozowania');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="panel">
      <h2>🔮 Prognozowanie Zużycia Energii</h2>
      
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
          <label>ID Modelu</label>
          <input
            type="number"
            value={modelId}
            onChange={(e) => setModelId(parseInt(e.target.value))}
            required
            min="1"
          />
        </div>

        <div className="form-group">
          <label>Typ modelu</label>
          <select
            value={modelType}
            onChange={(e) => setModelType(e.target.value)}
            required
          >
            <option value="SIMPLE_AVG">Prosta średnia</option>
            <option value="MOVING_AVG">Średnia ruchoma</option>
            <option value="LINEAR_TREND">Trend liniowy</option>
          </select>
        </div>

        <div className="form-group">
          <label>Liczba dni historii</label>
          <input
            type="number"
            value={historyDays}
            onChange={(e) => setHistoryDays(parseInt(e.target.value))}
            min="1"
            max="60"
          />
        </div>

        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Przetwarzanie...' : 'Uruchom prognozę'}
        </button>
      </form>

      {error && <div className="error">{error}</div>}

      {result && (
        <div className="results">
          <div className="success">Prognoza wygenerowana pomyślnie!</div>
          
          <div className="stats-grid">
            <div className="stat-card">
              <h3>Prognozowane zużycie</h3>
              <div className="value">{result.prediction.value.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Data prognozy</h3>
              <div className="value">{new Date(result.prediction.predictedForDate).toLocaleDateString('pl-PL')}</div>
            </div>
            <div className="stat-card">
              <h3>Typ modelu</h3>
              <div className="value">{modelType}</div>
            </div>
          </div>

          {measurements.length > 0 && result.prediction && (
            <div className="chart-container">
              <h3>Wykres prognozy</h3>
              <PredictionChart 
                measurements={measurements} 
                prediction={result.prediction}
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default PredictionPanel;

