import React, { useEffect, useState } from "react";
import { getAlerts } from "../../../services/alertsApi";

const AlertsPage = () => {
    const [alerts, setAlerts] = useState([]);
    const [role, setRole] = useState("RESIDENT");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            const data = await getAlerts(role);
            setAlerts(data);
            setLoading(false);
        };

        fetchData();
    }, [role]);

    // Funkcja pomocnicza do formatowania daty
    const formatDate = (dateString) => {
        if (!dateString) return "-";
        return new Date(dateString).toLocaleString('pl-PL', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    };

    return (
        <div style={{ padding: "20px" }}>
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
                                                    alert.priority === 'Emergency' ? '#dc3545' : // Czerwony
                                                        alert.priority === 'Warning' ? '#ffc107' :   // Żółty/Pomarańczowy
                                                            '#17a2b8' // Niebieski (Info)
                                            }}>
                                                {alert.priority}
                                            </span>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="6" style={{ padding: "20px", textAlign: "center", color: "#888" }}>
                                    Brak alertów do wyświetlenia dla roli: <strong>{role}</strong>
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default AlertsPage;