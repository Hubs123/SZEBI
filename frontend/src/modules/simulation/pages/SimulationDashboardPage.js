import React, { useState, useEffect, useRef } from 'react';
import { dataApi } from '../../../services/api';
import { ControlApi } from '../../../services/controlApi';
import EnergyChart from '../../analysis/components/EnergyChart';
import './SimulationDashboardPage.css';

const SimulationDashboardPage = () => {
  const [simulationData, setSimulationData] = useState([]);
  const [measurements, setMeasurements] = useState([]);
  const [devices, setDevices] = useState([]);
  const [deviceConsumption, setDeviceConsumption] = useState([]);
  const [loading, setLoading] = useState(true);
  const [simulationLoading, setSimulationLoading] = useState(false);
  const [simulationRunning, setSimulationRunning] = useState(false);
  const intervalRef = useRef(null);

  useEffect(() => {
    loadSimulationData();
    loadDevices();
    
    // Ustaw interwał na odświeżanie danych symulacji co 3 sekundy
    intervalRef.current = setInterval(() => {
      loadSimulationData();
      loadDevices();
    }, 3000);

    // Cleanup: wyczyść interwał przy unmount
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadSimulationData = async () => {
    try {
      setSimulationLoading(true);
      const response = await dataApi.getSimulationResults();
      const rawData = response.data || [];
      
      // Przekształć dane symulacji na format kompatybilny z measurements
      const transformedData = rawData.map(record => ({
        timestamp: record.periodStart ? (record.periodStart.includes(':') ? record.periodStart + ':00' : record.periodStart) : new Date().toISOString(),
        gridConsumption: record.gridConsumption || 0,
        gridFeedIn: record.gridFeedIn || 0,
        pvProduction: record.pvProduction || 0,
        batteryLevel: record.batteryLevel || 0,
        periodNumber: record.periodNumber,
        ...record
      }));

      setSimulationData(rawData);
      setMeasurements(transformedData);
    } catch (err) {
      console.error('Błąd ładowania danych symulacji:', err);
    } finally {
      setLoading(false);
      setSimulationLoading(false);
    }
  };

  const loadDevices = async () => {
    try {
      const devicesList = await ControlApi.listDevices();
      setDevices(devicesList || []);

      // Oblicz zużycie energii dla każdego urządzenia
      const consumptionData = [];
      for (const device of devicesList || []) {
        try {
          const states = await ControlApi.getDeviceStates(device.id);
          const isOn = states?.power === 1.0;
          
          // Oblicz moc zużycia na podstawie typu urządzenia (zgodnie z SimulationManager.java)
          let devicePower = 0.010; // Domyślnie 10W dla noSimulation
          switch (device.type) {
            case 'thermometer':
              devicePower = 0.001; // 1W
              break;
            case 'smokeDetector':
              devicePower = 0.005; // 5W
              break;
            case 'noSimulation':
              devicePower = 0.010; // 10W
              break;
            default:
              devicePower = 0.010; // 10W
          }

          // Oblicz zużycie energii dla całej doby (24h = 6 okresów po 4h)
          // Symulacja ma 6 okresów, każdy po 4h, więc 24h
          const periodDurationHours = 4.0;
          const dailyConsumption = isOn ? devicePower * 24 : 0; // kWh na dzień
          
          consumptionData.push({
            id: device.id,
            name: device.name,
            type: device.type,
            isOn: isOn,
            power: devicePower,
            dailyConsumption: dailyConsumption
          });
        } catch (err) {
          console.error(`Błąd pobierania stanu urządzenia ${device.id}:`, err);
        }
      }
      
      setDeviceConsumption(consumptionData);
    } catch (err) {
      console.error('Błąd ładowania urządzeń:', err);
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
      alert('Nie udało się uruchomić symulacji. Sprawdź konsolę.');
    } finally {
      setSimulationRunning(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
      let dateStr = dateString;
      if (dateStr.includes('T') && !dateStr.includes('Z') && !dateStr.includes('+')) {
        if (!dateStr.includes(':')) {
          dateStr = dateStr + 'T00:00:00';
        } else if (dateStr.match(/T\d{2}:\d{2}$/)) {
          dateStr = dateStr + ':00';
        }
      }
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) {
        return dateString;
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
    avg: measurements.reduce((sum, m) => sum + (m.gridConsumption || 0), 0) / measurements.length,
    min: Math.min(...measurements.map(m => m.gridConsumption || 0)),
    max: Math.max(...measurements.map(m => m.gridConsumption || 0)),
    total: measurements.reduce((sum, m) => sum + (m.gridConsumption || 0), 0),
    totalPV: measurements.reduce((sum, m) => sum + (m.pvProduction || 0), 0),
    totalFeedIn: measurements.reduce((sum, m) => sum + (m.gridFeedIn || 0), 0),
  } : null;

  return (
    <div>
      <div className="panel">
        <h2>📊 Dashboard Symulacji</h2>
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
            <div className="stat-card">
              <h3>Produkcja PV</h3>
              <div className="value">{stats.totalPV.toFixed(2)} kWh</div>
            </div>
            <div className="stat-card">
              <h3>Oddanie do sieci</h3>
              <div className="value">{stats.totalFeedIn.toFixed(2)} kWh</div>
            </div>
          </div>
        )}

        {measurements.length > 0 && (
          <div className="chart-container">
            <h3>Zużycie energii - wyniki symulacji</h3>
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
              className="btn"
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
                  <th>Okres #</th>
                  <th>Rozpoczęcie</th>
                  <th>Zakończenie</th>
                  <th>Zużycie z sieci (kWh)</th>
                  <th>Oddanie do sieci (kWh)</th>
                  <th>Produkcja PV (kWh)</th>
                  <th>Energia zmagazynowana (kWh)</th>
                  <th>Poziom baterii (kWh)</th>
                  <th>Nasłonecznienie</th>
                </tr>
              </thead>
              <tbody>
                {simulationData.map((record, index) => (
                  <tr key={index}>
                    <td className="period-number">{record.periodNumber || index + 1}</td>
                    <td>{formatDate(record.periodStart)}</td>
                    <td>{formatDate(record.periodEnd)}</td>
                    <td className="value-cell">{record.gridConsumption?.toFixed(3) || '0.000'}</td>
                    <td className="value-cell">{record.gridFeedIn?.toFixed(3) || '0.000'}</td>
                    <td className="value-cell">{record.pvProduction?.toFixed(3) || '0.000'}</td>
                    <td className="value-cell">{record.energyStored?.toFixed(3) || '0.000'}</td>
                    <td className="value-cell">
                      {(() => {
                        const batteryLevelKwh = record.batteryLevel || 0;
                        const batteryCapacity = record.batteryCapacity || 100;
                        const batteryLevelPercent = batteryCapacity > 0 ? (batteryLevelKwh / batteryCapacity) * 100 : 0;
                        const levelClass = batteryLevelPercent >= 50 ? 'high' : batteryLevelPercent >= 20 ? 'medium' : 'low';
                        return (
                          <span className={`battery-level ${levelClass}`} title={`${batteryLevelKwh.toFixed(2)} kWh / ${batteryCapacity} kWh`}>
                            {batteryLevelPercent.toFixed(1)}%
                          </span>
                        );
                      })()}
                    </td>
                    <td className="value-cell">{(record.sunlightIntensity * 100)?.toFixed(1) || '0.0'}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="no-data-message">
            {simulationLoading ? 'Ładowanie danych...' : 'Brak danych symulacji. Uruchom symulację, aby zobaczyć wyniki.'}
          </div>
        )}
      </div>

      {/* Lista urządzeń z zużyciem energii */}
      <div className="panel">
        <h2>🔌 Urządzenia i Zużycie Energii</h2>
        {deviceConsumption.length > 0 ? (
          <div className="devices-table-container">
            <table className="devices-table">
              <thead>
                <tr>
                  <th>Nazwa urządzenia</th>
                  <th>Typ</th>
                  <th>Status</th>
                  <th>Moc (kW)</th>
                  <th>Zużycie dzienne (kWh)</th>
                </tr>
              </thead>
              <tbody>
                {deviceConsumption.map((device) => (
                  <tr key={device.id}>
                    <td className="device-name">{device.name}</td>
                    <td className="device-type">{device.type}</td>
                    <td>
                      <span className={`device-status ${device.isOn ? 'on' : 'off'}`}>
                        {device.isOn ? '● Włączone' : '○ Wyłączone'}
                      </span>
                    </td>
                    <td className="value-cell">{device.power.toFixed(3)}</td>
                    <td className="value-cell">{device.dailyConsumption.toFixed(3)}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="total-row">
                  <td colSpan="4" className="total-label">Całkowite zużycie wszystkich urządzeń:</td>
                  <td className="value-cell total-value">
                    {deviceConsumption.reduce((sum, d) => sum + d.dailyConsumption, 0).toFixed(3)} kWh
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        ) : (
          <div className="no-data-message">
            Brak urządzeń w systemie. Dodaj urządzenia w module sterowania.
          </div>
        )}
      </div>
    </div>
  );
};

export default SimulationDashboardPage;
