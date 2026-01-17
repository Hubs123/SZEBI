import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import MainPageLayout from './layouts/MainPageLayout';
import AnalysisLayout from './layouts/AnalysisLayout';

import DashboardPage from './modules/analysis/pages/DashboardPage';
import AnalysisPage from './modules/analysis/pages/AnalysisPage';
import PredictionPanel from './modules/analysis/pages/PredictionPage';

import AlertsLayout from './layouts/AlertsLayout';
import AlertsPage from './modules/alerts/pages/AlertsPage';
import DeviceGroupsPage from './modules/alerts/pages/DeviceGroupsPage';

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<MainPageLayout />}>
                    <Route path="analiza" element={<AnalysisLayout />}>
                        <Route index element={<DashboardPage />} />
                        <Route path="panel" element={<AnalysisPage />} />
                        <Route path="predykcja" element={<PredictionPanel />} />

                    </Route>

                    <Route path="symulacja" element={<Placeholder title="Moduł Symulacji" />} />
                    <Route path="sterowanie" element={<Placeholder title="Moduł Sterowania" />} />
                    <Route path="optymalizacja" element={<Placeholder title="Moduł Optymalizacji" />} />
                    <Route path="komunikacja" element={<Placeholder title="Moduł Komunikacji" />} />

                    <Route path="alerty" element={<AlertsLayout />}>
                        {/* Zakładka 1: Przegląd (domyślna) */}
                        <Route index element={<AlertsPage />} />

                        {/* Zakładka 2: Konfiguracja */}
                        <Route path="konfiguracja" element={<DeviceGroupsPage />} />
                    </Route>

                    <Route path="*" element={<Navigate to="/" replace />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}

const Placeholder = ({ title }) => (
    <div className="panel" style={{ textAlign: 'center', padding: '3rem' }}>
        <h2>{title}</h2>
    </div>
);

export default App;