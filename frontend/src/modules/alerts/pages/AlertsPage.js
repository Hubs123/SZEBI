// modules/alerts/pages/AlertsPage.js
import React, { useEffect, useState } from "react";
import { getAlerts } from "../../../services/alertsApi";

const AlertsPage = () => {
    const [alerts, setAlerts] = useState([]);
    const [role, setRole] = useState("RESIDENT");
    const [loading, setLoading] = useState(false);

    // Stan dla okna modalnego (wybrany alert do wyświetlenia wiadomości)
    const [selectedAlert, setSelectedAlert] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            const data = await getAlerts(role);
            setAlerts(data);
            setLoading(false);
        };
        fetchData();
    }, [role]);

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleString('pl-PL', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    };

    // Funkcja otwierająca okno z wiadomością
    const handleReadMessage = (alert) => {
        setSelectedAlert(alert);
    };

    // Funkcja zamykająca okno
    const closeModal = () => {
        setSelectedAlert(null);
    };

    return (
        <div style={{ padding: "20px", position: "relative" }}>
            <div style={{ marginBottom: "20px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <h3 style={{ color: "#333" }}>Przegląd Alertów</h3>

                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                    <label style={{ fontWeight: "bold", color: "#555" }}>Widok Roli:</label>
                    <select
                        value={role}
                        onChange={(e) => setRole(e.target.value)}
                        style={{ padding: "8px", borderRadius: "5px", border: "1px solid #ccc" }}
                    >
                        <option value="RESIDENT">Mieszkaniec (Wszystkie)</option>
                        <option value="ENGINEER">Inżynier (Ostrzeżenia/Awarię)</option>
                        <option value="ADMIN">Administrator (Informacyjne)</option>
                    </select>
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
                            <th style={{ padding: "12px", borderBottom: "2px solid #dee2e6" }}>Akcja</th> {/* Nowa kolumna */}
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
                                    Brak alertów do wyświetlenia dla roli: <strong>{role}</strong>
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* OKNO MODALNE (POPUP) */}
            {selectedAlert && (
                <>
                    {/* Tło przyciemniające */}
                    <div
                        onClick={closeModal}
                        style={{
                            position: "fixed", top: 0, left: 0, right: 0, bottom: 0,
                            backgroundColor: "rgba(0,0,0,0.5)", zIndex: 999
                        }}
                    />

                    {/* Okno komunikatu */}
                    <div style={{
                        position: "fixed", top: "50%", left: "50%", transform: "translate(-50%, -50%)",
                        backgroundColor: "white", padding: "25px", zIndex: 1000,
                        boxShadow: "0 4px 15px rgba(0,0,0,0.3)", borderRadius: "10px",
                        minWidth: "350px", maxWidth: "500px"
                    }}>
                        <h4 style={{ marginTop: 0, color: "#333", borderBottom: "1px solid #eee", paddingBottom: "10px" }}>
                            Szczegóły Alertu #{selectedAlert.id}
                        </h4>

                        {/* Wyświetlanie wiadomości z createMessage() */}
                        <div style={{
                            padding: "15px",
                            backgroundColor: "#fff7e6",
                            borderLeft: "4px solid #ffc107",
                            fontFamily: "monospace",
                            whiteSpace: "pre-line", // To sprawia, że \n z Javy robi nową linię
                            color: "#333",
                            lineHeight: "1.5"
                        }}>
                            {selectedAlert.message || "Brak wiadomości systemowej."}
                        </div>

                        <div style={{ textAlign: "right", marginTop: "20px" }}>
                            <button
                                onClick={closeModal}
                                className="btn" // Używamy klasy z App.css
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