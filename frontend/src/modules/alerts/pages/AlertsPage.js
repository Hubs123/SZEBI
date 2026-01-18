import React, { useEffect, useState } from "react";
import { getAlerts } from "../../../services/alertsApi";

const AlertsPage = () => {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedAlert, setSelectedAlert] = useState(null);

    // Stan do wyświetlania nazwy roli w nagłówku
    const [userRoleDisplay, setUserRoleDisplay] = useState("");

    useEffect(() => {
        const fetchWithAutoRole = async (isBackground = false) => {
            if (!isBackground) {
                setLoading(true);
            }

            const token = sessionStorage.getItem("token");
            let apiRole = "RESIDENT";
            let displayRole = "Mieszkaniec";

            if (token) {
                try {
                    const payload = JSON.parse(atob(token.split('.')[1]));
                    const userRole = payload.role;

                    if (userRole === "ROLE_ENGINEER") {
                        apiRole = "ENGINEER";
                        displayRole = "Inżynier";
                    } else if (userRole === "ROLE_ADMIN") {
                        apiRole = "ADMIN";
                        displayRole = "Administrator";
                    } else {
                        apiRole = "RESIDENT";
                        displayRole = "Mieszkaniec";
                    }
                } catch (e) {
                    console.error("Błąd parsowania tokena:", e);
                }
            }

            setUserRoleDisplay(displayRole);

            const data = await getAlerts(apiRole);
            setAlerts(data);

            if (!isBackground) {
                setLoading(false);
            }
        };

        fetchWithAutoRole(false);

        const intervalId = setInterval(() => {
            fetchWithAutoRole(true);
        }, 5000);

        return () => clearInterval(intervalId);

    }, []);

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleString('pl-PL', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    };

    const handleReadMessage = (alert) => {
        setSelectedAlert(alert);
    };

    const closeModal = () => {
        setSelectedAlert(null);
    };

    return (
        <div style={{ padding: "20px", position: "relative" }}>
            <div style={{ marginBottom: "20px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h3 style={{ color: "#333" }}>Przegląd Alertów</h3>

                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                    <span style={{ color: "#666", fontSize: "0.9em" }}>
                        Zalogowany jako: <strong>{userRoleDisplay}</strong>
                    </span>
                </div>
            </div>

            {loading ? (
                <p style={{ textAlign: "center", color: "#666" }}>Ładowanie danych...</p>
            ) : (
                <div style={{ overflowX: "auto", boxShadow: "0 0 10px rgba(0,0,0,0.1)", borderRadius: "8px" }}>
                    <table style={{ width: "100%", borderCollapse: "collapse", backgroundColor: "white" }}>
                        <thead>
                        <tr style={{ backgroundColor: "#f8f9fa", textAlign: "left" }}>
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>ID</th>
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>Data</th>
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>Typ Anomalii</th>
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>Wartość</th>
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>Urządzenie (ID)</th>
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>Priorytet</th>
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>Akcja</th>
                        </tr>
                        </thead>
                        <tbody>
                        {alerts.length > 0 ? (
                            alerts.map((alert, index) => (
                                <tr key={alert.id || index} style={{ borderBottom: "1px solid #dee2e6" }}>
                                    <td style={{ padding: "12px" }}>{alert.id}</td>
                                    <td style={{ padding: "12px" }}>{formatDate(alert.alertDate)}</td>
                                    <td style={{ padding: "12px" }}>{alert.anomalyType}</td>
                                    <td style={{ padding: "12px" }}>{alert.anomalyValue}</td>
                                    <td style={{ padding: "12px" }}>{alert.deviceId}</td>
                                    <td style={{ padding: "12px" }}>
                                            <span style={{
                                                padding: "4px 8px",
                                                borderRadius: "4px",
                                                color: "white",
                                                fontSize: "0.85em",
                                                backgroundColor:
                                                    alert.priority === 'Emergency' ? '#dc3545' :
                                                        alert.priority === 'Warning' ? '#ffc107' :
                                                            '#17a2b8'
                                            }}>
                                                {alert.priority}
                                            </span>
                                    </td>
                                    <td style={{ padding: "12px" }}>
                                        <button
                                            onClick={() => handleReadMessage(alert)}
                                            style={{
                                                padding: "6px 12px",
                                                borderRadius: "5px",
                                                border: "1px solid #667eea",
                                                background: "white",
                                                color: "#667eea",
                                                cursor: "pointer",
                                                fontWeight: "bold",
                                                transition: "all 0.2s"
                                            }}
                                            onMouseOver={(e) => { e.target.style.background = "#667eea"; e.target.style.color = "white"; }}
                                            onMouseOut={(e) => { e.target.style.background = "white"; e.target.style.color = "#667eea"; }}
                                        >
                                            Odczytaj
                                        </button>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="7" style={{ padding: "20px", textAlign: "center", color: "#888" }}>
                                    Brak alertów dla Twojej roli.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            )}

            {selectedAlert && (
                <>
                    <div
                        onClick={closeModal}
                        style={{
                            position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
                            backgroundColor: "rgba(0,0,0,0.5)", zIndex: 999
                        }}
                    />
                    <div style={{
                        position: "fixed", top: "50%", left: "50%", transform: "translate(-50%, -50%)",
                        backgroundColor: "white", padding: "25px", zIndex: 1000,
                        boxShadow: "0 4px 15px rgba(0,0,0,0.3)", borderRadius: "10px",
                        minWidth: "350px", maxWidth: "500px"
                    }}>
                        <h4 style={{ marginTop: 0, color: "#333", borderBottom: "1px solid #eee", paddingBottom: "10px" }}>
                            Szczegóły Alertu #{selectedAlert.id}
                        </h4>
                        <div style={{
                            padding: "15px",
                            backgroundColor: "#fff7e6",
                            borderLeft: "4px solid #ffc107",
                            fontFamily: "monospace",
                            whiteSpace: "pre-line",
                            color: "#333",
                            lineHeight: "1.5"
                        }}>
                            {selectedAlert.message || "Brak wiadomości systemowej."}
                        </div>
                        <div style={{ textAlign: "right", marginTop: "20px" }}>
                            <button
                                onClick={closeModal}
                                className="btn"
                                style={{ padding: "8px 20px" }}
                            >
                                Zamknij
                            </button>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

export default AlertsPage;