import React from "react";
import { NavLink, Outlet, Link } from 'react-router-dom';
import '../App.css';

const MainPageLayout = () => {
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
            </nav>

            <main className="content">
                <Outlet />
            </main>
        </div>
    )
}

export default MainPageLayout;