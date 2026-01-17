import React, { useState, useEffect } from 'react';
import AlertService from '../../../services/AlertService';

const AlertList = () => {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedAlert, setSelectedAlert] = useState(null);

    const fetchAlerts = () => {
        setLoading(true);
        AlertService.getAllAlerts('RESIDENT')
            .then(response => {
                setAlerts(response.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Błąd:", err);
                setError("Nie udało się pobrać listy alertów.");
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchAlerts();
    }, []);

    const getPriorityColor = (priority) => {
        if (priority === 'Emergency') return 'red';
        if (priority === 'Warning') return 'orange';
        return 'blue';
    };

    // --- STYLE ---
    // Definiujemy style tutaj, żeby mieć pewność, że zadziałają
    // niezależnie od tego, czy masz Bootstrapa, czy nie.
    const tableStyle = {
        width: '100%',
        borderCollapse: 'collapse',
        marginTop: '20px',
        backgroundColor: 'rgba(255, 255, 255, 0.5)', // Lekko przezroczyste tło dla czytelności
        borderRadius: '8px',
        overflow: 'hidden'
    };

    const headerStyle = {
        padding: '15px 20px', // Duży odstęp w nagłówku
        textAlign: 'left',
        borderBottom: '2px solid #ddd',
        backgroundColor: 'rgba(255, 255, 255, 0.8)',
        fontWeight: 'bold',
        color: '#333'
    };

    const cellStyle = {
        padding: '15px 20px', // Duży odstęp w komórkach z danymi
        borderBottom: '1px solid #eee',
        verticalAlign: 'middle',
        color: '#333'
    };

    const buttonStyle = {
        padding: '8px 16px',
        borderRadius: '20px',
        border: 'none',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white',
        cursor: 'pointer',
        fontSize: '0.9rem',
        fontWeight: '500',
        boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
    };

    if (loading) return <div style={{ textAlign: 'center', padding: '40px' }}>Ładowanie alertów...</div>;
    if (error) return <div style={{ color: 'red', padding: '20px' }}>{error}</div>;

    return (
        <div style={{ padding: '20px' }}>
            {/* Kontener tabeli */}
            <div style={{ overflowX: 'auto' }}>
                <table style={tableStyle}>
                    <thead>
                    <tr>
                        <th style={headerStyle}>Data</th>
                        <th style={headerStyle}>Typ Anomalii</th>
                        <th style={headerStyle}>Priorytet</th>
                        <th style={{ ...headerStyle, textAlign: 'center' }}>Akcja</th>
                    </tr>
                    </thead>
                    <tbody>
                    {alerts.length === 0 ? (
                        <tr>
                            <td colSpan="4" style={{ ...cellStyle, textAlign: 'center', padding: '40px' }}>
                                Brak aktywnych alertów w systemie.
                            </td>
                        </tr>
                    ) : (
                        alerts.map(alert => (
                            <tr key={alert.id} style={{ backgroundColor: 'transparent' }}>
                                <td style={cellStyle}>
                                    {new Date(alert.alertDate).toLocaleString()}
                                </td>
                                <td style={cellStyle}>
                                    {alert.anomalyType}
                                </td>
                                <td style={{ ...cellStyle, fontWeight: 'bold', color: getPriorityColor(alert.priority) }}>
                                    {alert.priority}
                                </td>
                                <td style={{ ...cellStyle, textAlign: 'center' }}>
                                    <button
                                        style={buttonStyle}
                                        onClick={() => setSelectedAlert(alert)}
                                        onMouseOver={(e) => e.target.style.opacity = '0.9'}
                                        onMouseOut={(e) => e.target.style.opacity = '1'}
                                    >
                                        Odczytaj
                                    </button>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>

            {/* Modal / Okno ze szczegółami */}
            {selectedAlert && (
                <div style={{
                    position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
                    backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex',
                    justifyContent: 'center', alignItems: 'center', zIndex: 2000,
                    backdropFilter: 'blur(3px)'
                }}>
                    <div style={{
                        backgroundColor: 'white', padding: '30px', borderRadius: '15px',
                        width: '90%', maxWidth: '600px',
                        boxShadow: '0 10px 25px rgba(0,0,0,0.2)'
                    }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                            <h3 style={{ margin: 0, color: '#333' }}>Alert #{selectedAlert.id}</h3>
                            <button
                                onClick={() => setSelectedAlert(null)}
                                style={{ background: 'none', border: 'none', fontSize: '1.5rem', cursor: 'pointer', color: '#666' }}
                            >
                                ✕
                            </button>
                        </div>

                        <div style={{ background: '#f8f9fa', padding: '20px', borderRadius: '10px', border: '1px solid #e9ecef' }}>
                            <pre style={{
                                whiteSpace: 'pre-wrap',
                                fontFamily: 'inherit',
                                margin: 0,
                                fontSize: '1rem',
                                color: '#444',
                                lineHeight: '1.5'
                            }}>
                                {selectedAlert.message || "Brak treści komunikatu."}
                            </pre>
                        </div>

                        <div style={{ marginTop: '20px', textAlign: 'right' }}>
                            <button
                                onClick={() => setSelectedAlert(null)}
                                style={{ ...buttonStyle, background: '#6c757d' }}
                            >
                                Zamknij
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AlertList;