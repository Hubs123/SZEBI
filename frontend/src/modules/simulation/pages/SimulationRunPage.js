import React, { useState } from 'react';
import { dataApi } from '../../../services/api';
import './SimulationRunPage.css';

const SimulationRunPage = () => {
  const [date, setDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);

  React.useEffect(() => {
    // Ustaw domyślną datę na dzisiaj
    const today = new Date();
    const dateStr = today.toISOString().slice(0, 10);
    setDate(dateStr);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await dataApi.runSimulation(date);
      setResult(response.data);
    } catch (err) {
      console.error('Błąd uruchamiania symulacji:', err);
      setError(err.response?.data?.message || err.message || 'Wystąpił błąd podczas uruchamiania symulacji');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="panel">
      <h2>▶️ Uruchom Symulację</h2>
      
      <div className="simulation-info">
        <p>Symulacja generuje dane dla 6 okresów po 4 godziny każdy (łącznie 24h).</p>
        <p>Każdy okres obejmuje:</p>
        <ul>
          <li>Produkcję energii z paneli PV</li>
          <li>Zużycie energii przez urządzenia w domu</li>
          <li>Magazynowanie energii w akumulatorze</li>
          <li>Sprzedaż energii do sieci (gdy występuje nadmiar)</li>
          <li>Pobór energii z sieci (gdy brakuje energii)</li>
        </ul>
      </div>

      <form onSubmit={handleSubmit} className="simulation-form">
        <div className="form-group">
          <label>Data symulacji</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            required
          />
          <small className="form-hint">Wybierz datę, dla której ma zostać wykonana symulacja</small>
        </div>

        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Uruchamianie symulacji...' : '▶ Uruchom Symulację'}
        </button>
      </form>

      {error && <div className="error">{error}</div>}

      {result && (
        <div className="results">
          <div className="success">
            ✅ Symulacja zakończona pomyślnie!
          </div>
          
          <div className="simulation-result-details">
            <div className="result-item">
              <span className="result-label">Data symulacji:</span>
              <span className="result-value">{result.date || date}</span>
            </div>
            <div className="result-item">
              <span className="result-label">Liczba rekordów:</span>
              <span className="result-value">{result.recordCount || 6}</span>
            </div>
            <div className="result-item">
              <span className="result-label">Status:</span>
              <span className="result-value success-text">{result.message || 'Sukces'}</span>
            </div>
          </div>

          <div className="result-actions">
            <p>Symulacja została wykonana. Sprawdź wyniki w zakładce <strong>Dashboard</strong>.</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default SimulationRunPage;
