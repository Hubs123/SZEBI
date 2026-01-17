import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import MainPageLayout from './layouts/MainPageLayout';
import AnalysisLayout from './layouts/AnalysisLayout';

import DashboardPage from './modules/analysis/pages/DashboardPage';
import AnalysisPage from './modules/analysis/pages/AnalysisPage';
import PredictionPanel from './modules/analysis/pages/PredictionPage';

import AlertLayout from './layouts/AlertsLayout';
import {AlertsPage, DeviceGroupsPage, GroupDetailsPage} from './modules/alerts';


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
                    <Route path="alerty" element={<AlertLayout />}>
                        <Route index element={<AlertsPage role="ADMIN" />} />
                        <Route path="grupy" element={<DeviceGroupsPage />} />
                        <Route path="grupy/:groupId" element={<GroupDetailsPage role="ADMIN" />} />
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