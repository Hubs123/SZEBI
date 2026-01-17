import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import MainPageLayout from './layouts/MainPageLayout';
import AnalysisLayout from './layouts/AnalysisLayout';

import DashboardPage from './modules/analysis/pages/DashboardPage';
import AnalysisPage from './modules/analysis/pages/AnalysisPage';
import PredictionPanel from './modules/analysis/pages/PredictionPage';

// Upewnij się, że ścieżki są poprawne
import ChatPage from "./modules/communication/pages/ChatPage"; // To działa jako Layout dla czatu
import ChatWindow from "./modules/communication/pages/ChatWindow";
import LoginPage from "./modules/auth/pages/LoginPage";
import RegisterPage from "./modules/auth/pages/RegisterPage";

// Prosty placeholder dla czatu (możesz go przenieść do osobnego plika)
const ChatPlaceholder = () => (
    <div className="d-flex align-items-center justify-content-center h-100 text-muted">
        <h5><i className="bi bi-chat-left-dots me-2"></i> Wybierz konwersację z menu po lewej</h5>
    </div>
);

function App() {
    const token = localStorage.getItem("token");

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/register" element={<RegisterPage/>}/>

                <Route path="/" element={token ? <MainPageLayout/> : <Navigate to="/login"/>}>
                    <Route index element={<Navigate to="/analiza"/>} />

                    <Route path="komunikacja" element={<ChatPage/>}>
                        <Route index element={<ChatPlaceholder/>}/>
                        <Route path=":chatId" element={<ChatWindow/>}/>
                    </Route>

                    <Route path="analiza" element={<AnalysisLayout/>}>
                        <Route index element={<DashboardPage/>}/>
                        <Route path="panel" element={<AnalysisPage/>}/>
                        <Route path="predykcja" element={<PredictionPanel/>}/>
                    </Route>

                    <Route path="symulacja" element={<Placeholder title="Moduł Symulacji"/>}/>
                    <Route path="sterowanie" element={<Placeholder title="Moduł Sterowania"/>}/>
                    <Route path="optymalizacja" element={<Placeholder title="Moduł Optymalizacji"/>}/>
                    <Route path="alerty" element={<Placeholder title="Moduł Alertów"/>}/>

                    <Route path="*" element={<Navigate to="/" replace/>}/>
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