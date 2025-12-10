import React, { useState } from 'react';
import './App.css';
import AnalysisPanel from './components/AnalysisPanel';
import PredictionPanel from './components/PredictionPanel';
import Dashboard from './components/Dashboard';

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');

  return (
    <div className="App">
      <header className="App-header">
        <h1>⚡ Energy Analysis & Prediction System</h1>
        <nav className="nav-tabs">
          <button 
            className={activeTab === 'dashboard' ? 'active' : ''}
            onClick={() => setActiveTab('dashboard')}
          >
            Dashboard
          </button>
          <button 
            className={activeTab === 'analysis' ? 'active' : ''}
            onClick={() => setActiveTab('analysis')}
          >
            Analiza
          </button>
          <button 
            className={activeTab === 'prediction' ? 'active' : ''}
            onClick={() => setActiveTab('prediction')}
          >
            Prognozowanie
          </button>
        </nav>
      </header>

      <main className="App-main">
        {activeTab === 'dashboard' && <Dashboard />}
        {activeTab === 'analysis' && <AnalysisPanel />}
        {activeTab === 'prediction' && <PredictionPanel />}
      </main>
    </div>
  );
}

export default App;

