import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import '../App.css';

const AnalysisLayout = () => {
    return (
        <div style={{ width: '100%' }}>
            <div className="module-header-container" style={{ marginBottom: '2rem' }}>
                <h2 style={{ marginBottom: '1rem', color: '#333' }}>Moduł Analizy</h2>

                <nav className="nav-tabs">
                    <NavLink to="/analiza" end>Dashboard</NavLink>
                    <NavLink to="/analiza/panel">Analiza</NavLink>
                    <NavLink to="/analiza/predykcja">Prognozowanie</NavLink>
                </nav>
            </div>

            <div className="module-content">
                <Outlet />
            </div>
        </div>
    );
};

export default AnalysisLayout;