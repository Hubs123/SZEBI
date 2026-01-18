import React, { useEffect, useState } from "react";
import {
    getDeviceGroups, getThresholds,
    addThreshold, updateThreshold
    // Usunięto deleteThreshold z importów
} from "../../../services/alertsApi";

const DeviceGroupsPage = () => {
    const [groups, setGroups] = useState([]);
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [thresholds, setThresholds] = useState([]);

    // UI State
    const [activeTab, setActiveTab] = useState("thresholds");
    const [editingId, setEditingId] = useState(null);
    const [formData, setFormData] = useState({ thresholdType: "", valueWarning: "", valueEmergency: "" });

    // 1. Pobierz grupy
    useEffect(() => {
        getDeviceGroups().then((data) => {
            setGroups(data);
            if (data.length > 0) setSelectedGroup(data[0]);
        });
    }, []);

    // 2. Pobierz thresholdy
    useEffect(() => {
        if (selectedGroup) {
            fetchData(selectedGroup.id);
            cancelEdit();
        }
    }, [selectedGroup]);

    const fetchData = (groupId) => {
        getThresholds(groupId).then(setThresholds);
    };

    const handleGroupClick = (group) => setSelectedGroup(group);

    // --- LOGIKA DEKODOWANIA ID (Parzyste/Nieparzyste) ---
    const getReactionLabel = (reactionId) => {
        if (!reactionId) return "Brak (Tylko powiadomienie)";

        if (reactionId % 2 === 0) {
            return "Włącz urządzenie";
        } else {
            return "Wyłącz urządzenie";
        }
    };

    // --- TAB 1: PROGI ---
    const startEdit = (threshold) => {
        setEditingId(threshold.id);
        setFormData({
            thresholdType: threshold.thresholdType,
            valueWarning: threshold.valueWarning,
            valueEmergency: threshold.valueEmergency
        });
    };

    const cancelEdit = () => {
        setEditingId(null);
        setFormData({ thresholdType: "", valueWarning: "", valueEmergency: "" });
    };

    const handleThresholdSubmit = async (e) => {
        e.preventDefault();
        if (!selectedGroup) return;

        const payload = {
            thresholdType: formData.thresholdType,
            valueWarning: parseFloat(formData.valueWarning),
            valueEmergency: parseFloat(formData.valueEmergency),
            reactionName: null
        };

        let success = false;
        if (editingId) {
            success = await updateThreshold(selectedGroup.id, editingId, payload);
        } else {
            success = await addThreshold(selectedGroup.id, payload);
        }

        if (success) {
            fetchData(selectedGroup.id);
            cancelEdit();
        } else {
            alert("Błąd zapisu.");
        }
    };

    // --- USUNIĘTO FUNKCJĘ handleDelete ---

    // --- TAB 2: PRZYPISYWANIE ---
    const handleAssignReaction = async (threshold, reactionName) => {
        const payload = {
            valueWarning: threshold.valueWarning,
            valueEmergency: threshold.valueEmergency,
            reactionName: reactionName // "turnOn" lub "turnOff"
        };

        const success = await updateThreshold(selectedGroup.id, threshold.id, payload);
        if (success) {
            fetchData(selectedGroup.id);
        } else {
            alert("Nie udało się przypisać reakcji.");
        }
    };

    const tabStyle = (isActive) => ({
        padding: "10px 20px", cursor: "pointer",
        color: isActive ? "#667eea" : "#666",
        borderBottom: isActive ? "3px solid #667eea" : "3px solid transparent",
        background: "none", border: "none", fontSize: "1rem", fontWeight: isActive ? "bold" : "normal"
    });

    return (
        <div style={{ display: "flex", gap: "2rem", minHeight: "500px" }}>
            <div className="panel" style={{ flex: "1", padding: "1.5rem" }}>
                <h3 style={{ borderBottom: "1px solid #eee", paddingBottom: "10px" }}>Grupy</h3>
                <ul style={{ listStyle: "none", padding: 0 }}>
                    {groups.map(group => (
                        <li key={group.id} onClick={() => handleGroupClick(group)}
                            style={{
                                padding: "12px 15px", margin: "8px 0", cursor: "pointer", borderRadius: "8px",
                                backgroundColor: selectedGroup && selectedGroup.id === group.id ? "#e0f2fe" : "white",
                                border: selectedGroup && selectedGroup.id === group.id ? "1px solid #0284c7" : "1px solid #eee",
                                color: selectedGroup && selectedGroup.id === group.id ? "#0284c7" : "#333",
                                fontWeight: selectedGroup && selectedGroup.id === group.id ? "bold" : "normal",
                            }}
                        >
                            {group.groupName}
                        </li>
                    ))}
                </ul>
            </div>

            <div className="panel" style={{ flex: "3", padding: "1.5rem" }}>
                {selectedGroup ? (
                    <>
                        <h3 style={{ marginBottom: "5px", color: "#333" }}>{selectedGroup.groupName}</h3>
                        <div style={{ display: "flex", borderBottom: "1px solid #eee", marginBottom: "20px" }}>
                            <button style={tabStyle(activeTab === "thresholds")} onClick={() => setActiveTab("thresholds")}>1. Progi</button>
                            <button style={tabStyle(activeTab === "mapping")} onClick={() => setActiveTab("mapping")}>2. Reakcje</button>
                        </div>

                        {activeTab === "thresholds" && (
                            <>
                                <table style={{ width: "100%", borderCollapse: "collapse", marginBottom: "30px" }}>
                                    <thead>
                                    <tr style={{ borderBottom: "2px solid #ddd", textAlign: "left", color: "#666" }}>
                                        <th style={{ padding: "10px" }}>Typ</th>
                                        <th style={{ padding: "10px", color: "orange" }}>Warning</th>
                                        <th style={{ padding: "10px", color: "red" }}>Emergency</th>
                                        <th style={{ padding: "10px", textAlign: "right" }}>Edycja</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {thresholds.map(t => (
                                        <tr key={t.id} style={{ borderBottom: "1px solid #eee", background: editingId === t.id ? "#fff7ed" : "transparent" }}>
                                            <td style={{ padding: "10px", fontWeight: "bold" }}>{t.thresholdType}</td>
                                            <td style={{ padding: "10px" }}>{t.valueWarning}</td>
                                            <td style={{ padding: "10px" }}>{t.valueEmergency}</td>
                                            <td style={{ padding: "10px", textAlign: "right" }}>
                                                {/* Usunięto przycisk USUŃ, zostawiono tylko EDYTUJ */}
                                                <button onClick={() => startEdit(t)} style={{ cursor: "pointer", border: "1px solid #ccc", background: "white", padding: "5px 15px", borderRadius: "5px" }}>Edytuj</button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                                <div style={{ backgroundColor: editingId ? "#fff7ed" : "#f8f9fa", padding: "20px", borderRadius: "10px", border: "1px solid #eee" }}>
                                    <h4>{editingId ? "Edytuj" : "Dodaj"}</h4>
                                    <form onSubmit={handleThresholdSubmit} style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "15px" }}>
                                        <input type="text" placeholder="Typ" value={formData.thresholdType} onChange={e => setFormData({...formData, thresholdType: e.target.value})} required disabled={editingId !== null} style={{ padding: "10px", borderRadius: "5px", border: "1px solid #ccc" }} />
                                        <input type="number" step="0.1" placeholder="Warning" value={formData.valueWarning} onChange={e => setFormData({...formData, valueWarning: e.target.value})} required style={{ padding: "10px", borderRadius: "5px", border: "1px solid #ccc" }} />
                                        <input type="number" step="0.1" placeholder="Emergency" value={formData.valueEmergency} onChange={e => setFormData({...formData, valueEmergency: e.target.value})} required style={{ padding: "10px", borderRadius: "5px", border: "1px solid #ccc" }} />
                                        <div style={{ gridColumn: "1 / -1", textAlign: "right" }}>
                                            {editingId && <button type="button" onClick={cancelEdit} style={{ marginRight: "10px", padding: "10px" }}>Anuluj</button>}
                                            <button type="submit" className="btn" style={{ padding: "10px 25px" }}>{editingId ? "Zapisz" : "Dodaj"}</button>
                                        </div>
                                    </form>
                                </div>
                            </>
                        )}

                        {activeTab === "mapping" && (
                            <>
                                <p style={{ color: "#666", marginBottom: "20px" }}>Wybierz automatyczną reakcję.</p>
                                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                                    <thead>
                                    <tr style={{ borderBottom: "2px solid #ddd", textAlign: "left", color: "#666" }}>
                                        <th style={{ padding: "10px" }}>Typ Anomalii</th>
                                        <th style={{ padding: "10px" }}>Aktualna Reakcja</th>
                                        <th style={{ padding: "10px" }}>Zmień</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {thresholds.map(t => {
                                        const label = getReactionLabel(t.reactionId);
                                        const isSet = t.reactionId !== null;

                                        return (
                                            <tr key={t.id} style={{ borderBottom: "1px solid #eee" }}>
                                                <td style={{ padding: "15px", fontWeight: "bold" }}>{t.thresholdType}</td>

                                                <td style={{ padding: "15px" }}>
                                                    <span style={{
                                                        background: isSet ? (t.reactionId % 2 === 0 ? "#dbeafe" : "#fee2e2") : "#f3f4f6",
                                                        color: isSet ? (t.reactionId % 2 === 0 ? "#1e40af" : "#991b1b") : "#9ca3af",
                                                        padding: "5px 12px", borderRadius: "20px", fontSize: "0.9em", fontWeight: "bold"
                                                    }}>
                                                        {label}
                                                    </span>
                                                </td>

                                                <td style={{ padding: "15px" }}>
                                                    <select
                                                        defaultValue=""
                                                        onChange={(e) => handleAssignReaction(t, e.target.value)}
                                                        style={{ padding: "10px", borderRadius: "5px", border: "1px solid #ccc", width: "100%" }}
                                                    >
                                                        <option value="">-- Wybierz akcję --</option>
                                                        <option value="">Brak</option>
                                                        <option value="turnOn">Włącz (turnOn)</option>
                                                        <option value="turnOff">Wyłącz (turnOff)</option>
                                                    </select>
                                                </td>
                                            </tr>
                                        )})}
                                    </tbody>
                                </table>
                            </>
                        )}
                    </>
                ) : (
                    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%", color: "#999" }}>Wybierz grupę.</div>
                )}
            </div>
        </div>
    );
};

export default DeviceGroupsPage;