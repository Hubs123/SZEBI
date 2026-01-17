// layouts/AlertsLayout.js
import React from "react";
import { Outlet, Link, useLocation } from "react-router-dom";

const AlertsLayout = () => {
    const location = useLocation();

    // Styl zakładek wzorowany na Twoim CSS (nav-tabs)
    const containerStyle = {
        display: "flex",
        justifyContent: "center",
        gap: "1rem",
        marginBottom: "2rem",
        padding: "0.5rem",
        background: "rgba(255, 255, 255, 0.1)",
        width: "fit-content",
        margin: "0 auto",
        borderRadius: "50px",
        border: "1px solid rgba(255, 255, 255, 0.2)"
    };

    const linkStyle = (path) => ({
        textDecoration: "none",
        padding: "0.75rem 2rem",
        color: location.pathname === path ? "#667eea" : "rgba(255, 255, 255, 0.8)",
        backgroundColor: location.pathname === path ? "white" : "transparent",
        fontWeight: "600",
        borderRadius: "30px",
        transition: "all 0.3s ease",
        boxShadow: location.pathname === path ? "0 4px 15px rgba(0, 0, 0, 0.1)" : "none"
    });

    return (
        <div style={{ padding: "20px" }}>
            <h2 style={{ textAlign: "center", color: "white", marginBottom: "20px" }}>Moduł Alertów</h2>

            <nav style={containerStyle}>
                <Link to="/alerty" style={linkStyle("/alerty")}>
                    Przegląd Alertów
                </Link>
                <Link to="/alerty/konfiguracja" style={linkStyle("/alerty/konfiguracja")}>
                    Grupy urządzeń
                </Link>
            </nav>

            <div className="panel">
                <Outlet />
            </div>
        </div>
    );
};

export default AlertsLayout;