import React, { useEffect, useState } from "react";
// Upewnij się, że masz updateThreshold w imporcie!
import { getDeviceGroups, getThresholds, addThreshold, deleteThreshold, updateThreshold } from "../../../services/alertsApi";

const DeviceGroupsPage = () => {
    // ... (stany bez zmian: groups, selectedGroup, thresholds, editingId, formData) ...
    const [groups, setGroups] = useState([]);
    const [selectedGroup, setSelectedGroup] = useState(null);
    const [thresholds, setThresholds] = useState([]);
    const [editingId, setEditingId] = useState(null);
    const [formData, setFormData] = useState({ thresholdType: "", valueWarning: "", valueEmergency: "" });

    // ... (useEffecty bez zmian) ...
    useEffect(() => {
        getDeviceGroups().then((data) => {
            setGroups(data);
            if (data.length > 0) setSelectedGroup(data[0]);
        });
    }, []);

    useEffect(() => {
        if (selectedGroup) {
            fetchThresholds(selectedGroup.id);
            cancelEdit();
        }
    }, [selectedGroup]);

    const fetchThresholds = (groupId) => getThresholds(groupId).then(setThresholds);
    const handleGroupClick = (group) => setSelectedGroup(group);

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

    // --- TUTAJ ZMIANA: UŻYWAMY UPDATE ZAMIAST DELETE+ADD ---
    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedGroup) return;

        const payload = {
            thresholdType: formData.thresholdType,
            valueWarning: parseFloat(formData.valueWarning),
            valueEmergency: parseFloat(formData.valueEmergency)
        };

        let success = false;

        if (editingId) {
            // CZYSTA EDYCJA (Backend modifyThreshold)
            console.log("Aktualizacja ID:", editingId);
            success = await updateThreshold(selectedGroup.id, editingId, payload);
        } else {
            // DODAWANIE
            console.log("Dodawanie nowego...");
            success = await addThreshold(selectedGroup.id, payload);
        }

        if (success) {
            fetchThresholds(selectedGroup.id);
            cancelEdit();
        } else {
            alert("Operacja nieudana. Sprawdź backend.");
        }
    };

    const handleDelete = async (thresholdId) => {
        if (!window.confirm("Usunąć?")) return;
        const success = await deleteThreshold(selectedGroup.id, thresholdId);
        if (success) fetchThresholds(selectedGroup.id);
    };

    return (
        <div style={{ display: "flex", gap: "2rem", minHeight: "500px" }}>
            {/* LEWA KOLUMNA (bez zmian) */}
            <div className="panel" style={{ flex: "1", padding: "1.5rem" }}>
                <h3 style={{ borderBottom: "1px solid #eee", paddingBottom: "10px" }}>Grupy Urządzeń</h3>
                <ul style={{ listStyle: "none", padding: 0 }}>
                    {groups.map(group => (
                        <li
                            key={group.id}
                            onClick={() => handleGroupClick(group)}
                            style={{
                                padding: "12px 15px", margin: "8px 0", cursor: "pointer", borderRadius: "8px",
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

            {/* PRAWA KOLUMNA */}
            <div className="panel" style={{ flex: "2", padding: "1.5rem" }}>
                {selectedGroup ? (
                    <>
                        <h3 style={{ marginBottom: "20px", color: "#333" }}>
                            Konfiguracja: <span style={{color: "#667eea"}}>{selectedGroup.groupName}</span>
                        </h3>

                        <table style={{ width: "100%", borderCollapse: "collapse", marginBottom: "30px" }}>
                            <thead>
                            <tr style={{ borderBottom: "2px solid #ddd", textAlign: "left", color: "#666" }}>
                                <th style={{ padding: "10px" }}>Typ</th>
                                <th style={{ padding: "10px", color: "#d97706" }}>Warning</th>
                                <th style={{ padding: "10px", color: "#dc2626" }}>Emergency</th>
                                <th style={{ padding: "10px", textAlign: "right" }}>Akcje</th>
                            </tr>
                            </thead>
                            <tbody>
                            {thresholds.map(t => (
                                <tr key={t.id} style={{ borderBottom: "1px solid #eee", backgroundColor: editingId === t.id ? "#fff7ed" : "transparent" }}>
                                    <td style={{ padding: "10px", fontWeight: "bold" }}>{t.thresholdType}</td>
                                    <td style={{ padding: "10px" }}>{t.valueWarning}</td>
                                    <td style={{ padding: "10px" }}>{t.valueEmergency}</td>
                                    <td style={{ padding: "10px", textAlign: "right" }}>
                                        <button onClick={() => startEdit(t)} style={{ marginRight: "10px", cursor: "pointer", border: "1px solid #ccc", background: "white", padding: "5px 10px", borderRadius: "5px" }}>Edytuj</button>
                                        <button onClick={() => handleDelete(t.id)} style={{ color: "red", border: "none", background: "none", cursor: "pointer", fontWeight: "bold" }}>X</button>
                                    </td>
                                </tr>
                            ))}
                            {thresholds.length === 0 && <tr><td colSpan="4" style={{padding:"20px", textAlign:"center", color:"#888"}}>Brak ustawionych limitów.</td></tr>}
                            </tbody>
                        </table>

                        <div style={{ backgroundColor: editingId ? "#fff7ed" : "#f8f9fa", padding: "20px", borderRadius: "10px", border: editingId ? "1px solid #fdba74" : "1px solid #e9ecef" }}>
                            <h4 style={{ marginTop: 0 }}>{editingId ? "Edytuj progi" : "Dodaj progi"}</h4>
                            <form onSubmit={handleSubmit} style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "15px" }}>
                                <div>
                                    <label style={{ display: "block", fontSize: "0.85em", fontWeight: "bold", marginBottom: "5px" }}>Typ</label>
                                    <input type="text" placeholder="np. TEMP" value={formData.thresholdType} onChange={e => setFormData({...formData, thresholdType: e.target.value})} required disabled={editingId !== null} style={{ width: "100%", padding: "8px", borderRadius: "5px", border: "1px solid #ccc", background: editingId ? "#eee" : "white" }} />
                                </div>
                                <div>
                                    <label style={{ display: "block", fontSize: "0.85em", fontWeight: "bold", marginBottom: "5px", color: "#d97706" }}>Warning</label>
                                    <input type="number" step="0.1" value={formData.valueWarning} onChange={e => setFormData({...formData, valueWarning: e.target.value})} required style={{ width: "100%", padding: "8px", borderRadius: "5px", border: "1px solid #ccc" }} />
                                </div>
                                <div>
                                    <label style={{ display: "block", fontSize: "0.85em", fontWeight: "bold", marginBottom: "5px", color: "#dc2626" }}>Emergency</label>
                                    <input type="number" step="0.1" value={formData.valueEmergency} onChange={e => setFormData({...formData, valueEmergency: e.target.value})} required style={{ width: "100%", padding: "8px", borderRadius: "5px", border: "1px solid #ccc" }} />
                                </div>
                                <div style={{ gridColumn: "1 / -1", display: "flex", justifyContent: "flex-end", gap: "10px" }}>
                                    {editingId && <button type="button" onClick={cancelEdit} style={{ padding: "10px 20px", border: "1px solid #ccc", background: "white", borderRadius: "8px", cursor: "pointer" }}>Anuluj</button>}
                                    <button type="submit" className="btn" style={{ padding: "10px 25px" }}>{editingId ? "Zapisz" : "Dodaj"}</button>
                                </div>
                            </form>
                        </div>
                    </>
                ) : (
                    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%", color: "#999" }}>Wybierz grupę.</div>
                )}
            </div>
        </div>
    );
};

export default DeviceGroupsPage;