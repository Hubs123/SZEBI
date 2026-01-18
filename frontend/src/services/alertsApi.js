// services/alertsApi.js
const API_URL = "http://localhost:8080/api";

// --- Alerts (AlertController) ---
export const getAlerts = async (role) => {
    try {
        console.log(`[API] Wysyłanie zapytania: GET ${API_URL}/alerts?role=${role}`);
        const response = await fetch(`${API_URL}/alerts?role=${role}`);

        if (!response.ok) {
            throw new Error(`Błąd sieci: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        console.log("[API] Otrzymane alerty:", data); // <-- To pokaże Ci w konsoli co dokładnie przyszło
        return data;
    } catch (error) {
        console.error("[API] Błąd pobierania alertów:", error);
        return []; // Zwracamy pustą tablicę, żeby nie wywalić aplikacji
    }
};

// --- Admin / Config (AdminController) ---
export const getDeviceGroups = async () => {
    try {
        const response = await fetch(`${API_URL}/admin/alerts/groups`);
        if (!response.ok) throw new Error("Błąd pobierania grup");
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
};

export const getThresholds = async (groupId) => {
    try {
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds`);
        if (!response.ok) throw new Error("Błąd pobierania progów");
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
};

export const addThreshold = async (groupId, threshold) => {
    try {
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(threshold),
        });
        if (!response.ok) throw new Error("Błąd dodawania progu");
        return await response.json();
    } catch (error) {
        console.error(error);
        return false;
    }
};

export const updateThreshold = async (groupId, thresholdId, thresholdData) => {
    try {
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds/${thresholdId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(thresholdData),
        });
        if (!response.ok) throw new Error("Błąd edycji progu");
        return await response.json();
    } catch (error) {
        console.error(error);
        return false;
    }
};

export const deleteThreshold = async (groupId, thresholdId) => {
    try {
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds/${thresholdId}`, {
            method: "DELETE",
        });
        if (!response.ok) throw new Error("Błąd usuwania progu");
        return await response.json();
    } catch (error) {
        console.error(error);
        return false;
    }
};