import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import '../App.css';

const SimulationLayout = () => {
    return (
        <div style={{ width: '100%' }}>
            <div className="module-header-container" style={{ marginBottom: '2rem' }}>
                <nav className="nav-tabs">
                    <NavLink to="/symulacja" end>Dashboard</NavLink>
                    <NavLink to="/symulacja/uruchom">Uruchom Symulację</NavLink>
                    <NavLink to="/symulacja/ustawienia">Ustawienia</NavLink>
                </nav>
            </div>

            <div className="module-content">
                <Outlet />
            </div>
        </div>
    );
};

export default SimulationLayout;
