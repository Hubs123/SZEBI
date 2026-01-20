import React, { useState } from 'react';
import './SimulationSettingsPage.css';

const SimulationSettingsPage = () => {
  const [panelPower, setPanelPower] = useState(5.0);
  const [batteryCapacity, setBatteryCapacity] = useState(10.0);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSaved(false);

    // Walidacja
    if (panelPower <= 0) {
      setError('Moc paneli musi być większa od zera');
      return;
    }
    if (batteryCapacity <= 0) {
      setError('Pojemność magazynu musi być większa od zera');
      return;
    }

    try {
      // TODO: W przyszłości można dodać endpoint API do zapisywania ustawień
      // Na razie tylko pokazujemy informację o aktualnych ustawieniach
      console.log('Ustawienia symulacji:', { panelPower, batteryCapacity });
      setSaved(true);
      
      // Ukryj komunikat sukcesu po 3 sekundach
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      setError('Nie udało się zapisać ustawień');
      console.error(err);
    }
  };

  return (
    <div className="panel">
      <h2>Ustawienia Symulacji</h2>
      
      <div className="settings-info">
        <p>Konfiguracja parametrów systemu energetycznego używanych w symulacji.</p>
        <p className="note">
          <strong>Uwaga:</strong> Obecnie ustawienia są używane tylko podczas uruchamiania symulacji przez API.
          Zmiany w tych ustawieniach nie są jeszcze zapisywane permanentnie.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="settings-form">
        <div className="form-group">
          <label htmlFor="panelPower">
            Moc paneli fotowoltaicznych (kW)
          </label>
          <input
            id="panelPower"
            type="number"
            step="0.1"
            min="0.1"
            value={panelPower}
            onChange={(e) => setPanelPower(parseFloat(e.target.value))}
            required
          />
          <small className="form-hint">
            Moc zainstalowanych paneli fotowoltaicznych. Wpływa na ilość energii produkowanej przez panele.
          </small>
        </div>

        <div className="form-group">
          <label htmlFor="batteryCapacity">
            Pojemność magazynu energii (kWh)
          </label>
          <input
            id="batteryCapacity"
            type="number"
            step="0.1"
            min="0.1"
            value={batteryCapacity}
            onChange={(e) => setBatteryCapacity(parseFloat(e.target.value))}
            required
          />
          <small className="form-hint">
            Pojemność akumulatora służącego do magazynowania energii. 
            Nadmiarowa energia z paneli jest magazynowana w akumulatorze.
          </small>
        </div>

        <button type="submit" className="btn">
          Zapisz ustawienia
        </button>
      </form>

      {error && <div className="error">{error}</div>}
      {saved && <div className="success">✅ Ustawienia zostały zapisane!</div>}

      <div className="settings-summary">
        <h3>Podsumowanie konfiguracji</h3>
        <div className="summary-grid">
          <div className="summary-item">
            <span className="summary-label">Moc paneli PV:</span>
            <span className="summary-value">{panelPower} kW</span>
          </div>
          <div className="summary-item">
            <span className="summary-label">Pojemność magazynu:</span>
            <span className="summary-value">{batteryCapacity} kWh</span>
          </div>
          <div className="summary-item">
            <span className="summary-label">Teoretyczna produkcja dzienna (przy 100% nasłonecznienia):</span>
            <span className="summary-value">{(panelPower * 24).toFixed(2)} kWh</span>
          </div>
          <div className="summary-item">
            <span className="summary-label">Pojemność magazynu (% produkcji dziennej):</span>
            <span className="summary-value">{((batteryCapacity / (panelPower * 24)) * 100).toFixed(1)}%</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SimulationSettingsPage;
