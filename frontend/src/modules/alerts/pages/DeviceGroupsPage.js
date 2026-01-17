import React, { useEffect, useState } from "react";
import { getDeviceGroups, getThresholds, addThreshold, deleteThreshold } from "../../../services/alertsApi";

const DeviceGroupsPage = () => {
    const [groups, setGroups] = useState([]);
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [thresholds, setThresholds] = useState([]);

    // Stan formularza
    const [newThreshold, setNewThreshold] = useState({
        thresholdType: "",
        valueWarning: "",
        valueEmergency: ""
    });

    // 1. Pobierz grupy z Backendu (teraz zwróci te 3 zhardcodowane)
    useEffect(() => {
        getDeviceGroups().then((data) => {
            setGroups(data);
            // Automatycznie wybierz pierwszą grupę dla wygody
            if (data.length > 0) {
                setSelectedGroup(data[0]);
            }
        });
    }, []);

    // 2. Pobierz thresholdy po kliknięciu w grupę
    useEffect(() => {
        if (selectedGroup) {
            fetchThresholds(selectedGroup.id);
        }
    }, [selectedGroup]);

    const fetchThresholds = (groupId) => {
        getThresholds(groupId).then(setThresholds);
    };

    const handleGroupClick = (group) => {
        setSelectedGroup(group);
        setNewThreshold({ thresholdType: "", valueWarning: "", valueEmergency: "" });
    };

    const handleAdd = async (e) => {
        e.preventDefault();
        if (!selectedGroup) return;

        const payload = {
            thresholdType: newThreshold.thresholdType,
            valueWarning: parseFloat(newThreshold.valueWarning),
            valueEmergency: parseFloat(newThreshold.valueEmergency)
        };

        const success = await addThreshold(selectedGroup.id, payload);
        if (success) {
            fetchThresholds(selectedGroup.id);
            setNewThreshold({ thresholdType: "", valueWarning: "", valueEmergency: "" });
        } else {
            alert("Błąd zapisu. Sprawdź backend.");
        }
    };

    const handleDelete = async (thresholdId) => {
        if (!window.confirm("Usunąć ten próg?")) return;
        const success = await deleteThreshold(selectedGroup.id, thresholdId);
        if (success) fetchThresholds(selectedGroup.id);
    };

    return (
        <div style={{ display: "flex", gap: "2rem", minHeight: "500px" }}>

            {/* LEWA KOLUMNA: Lista Grup */}
            <div className="panel" style={{ flex: "1", padding: "1.5rem" }}>
                <h3 style={{ borderBottom: "1px solid #eee", paddingBottom: "10px" }}>Grupy Urządzeń</h3>
                <ul style={{ listStyle: "none", padding: 0 }}>
                    {groups.map(group => (
                        <li
                            key={group.id}
                            onClick={() => handleGroupClick(group)}
                            style={{
                                padding: "12px 15px",
                                margin: "8px 0",
                                cursor: "pointer",
                                borderRadius: "8px",
                                backgroundColor: selectedGroup && selectedGroup.id === group.id ? "#e0f2fe" : "white",
                                border: selectedGroup && selectedGroup.id === group.id ? "1px solid #0284c7" : "1px solid #eee",
                                color: selectedGroup && selectedGroup.id === group.id ? "#0284c7" : "#333",
                                fontWeight: selectedGroup && selectedGroup.id === group.id ? "bold" : "normal",
                                transition: "all 0.2s"
                            }}
                        >
                            {group.groupName}
                        </li>
                    ))}
                </ul>
            </div>

            {/* PRAWA KOLUMNA: Thresholdy */}
            <div className="panel" style={{ flex: "2", padding: "1.5rem" }}>
                {selectedGroup ? (
                    <>
                        <h3 style={{ marginBottom: "20px", color: "#333" }}>
                            Limity dla: <span style={{color: "#667eea"}}>{selectedGroup.groupName}</span>
                        </h3>

                        {/* Tabela istniejących progów */}
                        <table style={{ width: "100%", borderCollapse: "collapse", marginBottom: "30px" }}>
                            <thead>
                            <tr style={{ borderBottom: "2px solid #ddd", textAlign: "left", color: "#666" }}>
                                <th style={{ padding: "10px" }}>Typ (np. TEMP)</th>
                                <th style={{ padding: "10px", color: "#d97706" }}>Próg Warning</th>
                                <th style={{ padding: "10px", color: "#dc2626" }}>Próg Emergency</th>
                                <th style={{ padding: "10px" }}>Akcja</th>
                            </tr>
                            </thead>
                            <tbody>
                            {thresholds.map(t => (
                                <tr key={t.id} style={{ borderBottom: "1px solid #eee" }}>
                                    <td style={{ padding: "10px", fontWeight: "bold" }}>{t.thresholdType}</td>
                                    <td style={{ padding: "10px" }}>{t.valueWarning}</td>
                                    <td style={{ padding: "10px" }}>{t.valueEmergency}</td>
                                    <td style={{ padding: "10px" }}>
                                        <button onClick={() => handleDelete(t.id)} style={{ color: "red", border: "none", background: "none", cursor: "pointer", fontWeight: "bold" }}>Usuń</button>
                                    </td>
                                </tr>
                            ))}
                            {thresholds.length === 0 && <tr><td colSpan="4" style={{padding:"20px", textAlign:"center", color:"#888"}}>Brak ustawionych limitów.</td></tr>}
                            </tbody>
                        </table>

                        {/* Formularz definiowania 3 poziomów */}
                        <div style={{ backgroundColor: "#f8f9fa", padding: "20px", borderRadius: "10px", border: "1px solid #e9ecef" }}>
                            <h4 style={{ marginTop: 0 }}>Zdefiniuj poziomy alertów</h4>
                            <p style={{ fontSize: "0.85em", color: "#666", marginBottom: "15px" }}>
                                Ustawiając progi Warning i Emergency, definiujesz 3 strefy:<br/>
                                1. <strong>Information</strong> (Domyślny, bezpieczny)<br/>
                                2. <strong>Warning</strong> (Powyżej/Poniżej pierwszego progu)<br/>
                                3. <strong>Emergency</strong> (Przekroczenie drugiego progu)
                            </p>

                            <form onSubmit={handleAdd} style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "15px" }}>
                                <div>
                                    <label style={{ display: "block", fontSize: "0.85em", fontWeight: "bold", marginBottom: "5px" }}>Typ Anomalii</label>
                                    <input
                                        type="text" placeholder="np. TEMP, POWER"
                                        value={newThreshold.thresholdType}
                                        onChange={e => setNewThreshold({...newThreshold, thresholdType: e.target.value})}
                                        required
                                        style={{ width: "100%", padding: "8px", borderRadius: "5px", border: "1px solid #ccc" }}
                                    />
                                </div>
                                <div>
                                    <label style={{ display: "block", fontSize: "0.85em", fontWeight: "bold", marginBottom: "5px", color: "#d97706" }}>Granica Ostrzeżenia</label>
                                    <input
                                        type="number" step="0.1" placeholder="Wartość Warning"
                                        value={newThreshold.valueWarning}
                                        onChange={e => setNewThreshold({...newThreshold, valueWarning: e.target.value})}
                                        required
                                        style={{ width: "100%", padding: "8px", borderRadius: "5px", border: "1px solid #ccc" }}
                                    />
                                </div>
                                <div>
                                    <label style={{ display: "block", fontSize: "0.85em", fontWeight: "bold", marginBottom: "5px", color: "#dc2626" }}>Granica Awarii</label>
                                    <input
                                        type="number" step="0.1" placeholder="Wartość Emergency"
                                        value={newThreshold.valueEmergency}
                                        onChange={e => setNewThreshold({...newThreshold, valueEmergency: e.target.value})}
                                        required
                                        style={{ width: "100%", padding: "8px", borderRadius: "5px", border: "1px solid #ccc" }}
                                    />
                                </div>
                                <div style={{ gridColumn: "1 / -1", textAlign: "right" }}>
                                    <button type="submit" className="btn" style={{ padding: "10px 25px" }}>Zapisz Progi</button>
                                </div>
                            </form>
                        </div>
                    </>
                ) : (
                    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%", color: "#999" }}>
                        Wybierz grupę z menu po lewej stronie.
                    </div>
                )}
            </div>
        </div>
    );
};

export default DeviceGroupsPage;