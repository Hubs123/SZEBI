import React from "react";
import { NavLink, Outlet, Link, useNavigate } from 'react-router-dom';
import '../App.css';

const MainPageLayout = () => {
    const navigate = useNavigate();
    const username = sessionStorage.getItem("username");

    const handleLogout = () => {
        sessionStorage.clear();
        navigate('/login');
        window.location.reload();
    };

    return (
        <div className="app-container">
            <nav className="navbar">
                <Link to="/" className="logo">SZEBI</Link>

                <ul className="navbar-links">
                    <li><NavLink to="/alerty">Alerty</NavLink></li>
                    <li><NavLink to="/analiza">Analiza</NavLink></li>
                    <li><NavLink to="/komunikacja">Komunikacja</NavLink></li>
                    <li><NavLink to="/optymalizacja">Optymalizacja</NavLink></li>
                    <li><NavLink to="/sterowanie">Sterowanie</NavLink></li>
                    <li><NavLink to="/symulacja">Symulacja</NavLink></li>
                </ul>

                <div className="header-right">
                    <div className="user-session-container">
                        <span className="user-name-display">
                            <strong>{username}</strong>
                        </span>
                        <button onClick={handleLogout} className="logout-button-small">
                            Wyloguj
                        </button>
                    </div>
                </div>
            </nav>

            <main className="content">
                <Outlet />
            </main>
        </div>
    )
}

export default MainPageLayout;