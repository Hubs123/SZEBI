const API_URL = "http://localhost:8080/api";

// Pomocnicza funkcja do pobierania nagłówków z tokenem
const getAuthHeaders = () => {
    const token = sessionStorage.getItem("token");
    return {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json"
    };
};

export const getAlerts = async (role) => {
    try {
        const response = await fetch(`${API_URL}/alerts?role=${role}`, {
            headers: { "Authorization": "Bearer " + sessionStorage.getItem("token") }
        });
        if (!response.ok) throw new Error("Błąd sieci");
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
};

export const getDeviceGroups = async () => {
    try {
        const response = await fetch(`${API_URL}/admin/alerts/groups`, {
            headers: { "Authorization": "Bearer " + sessionStorage.getItem("token") }
        });
        if (!response.ok) throw new Error("Błąd pobierania grup");
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
};

export const getThresholds = async (groupId) => {
    try {
        // NAPRAWIONE: Dodano nagłówek Authorization
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds`, {
            headers: { "Authorization": "Bearer " + sessionStorage.getItem("token") }
        });
        if (!response.ok) throw new Error("Błąd pobierania progów");
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
};

export const getReactions = async (groupId) => {
    try {
        // NAPRAWIONE: Dodano nagłówek Authorization
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/reactions`, {
            headers: { "Authorization": "Bearer " + sessionStorage.getItem("token") }
        });
        if (!response.ok) throw new Error("Błąd pobierania reakcji");
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
};

export const addThreshold = async (groupId, threshold) => {
    try {
        // NAPRAWIONE: Użycie getAuthHeaders() (Token + Content-Type)
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(threshold),
        });
        if (!response.ok) throw new Error("Błąd dodawania");
        return await response.json();
    } catch (error) {
        console.error(error);
        return false;
    }
};

export const updateThreshold = async (groupId, thresholdId, thresholdData) => {
    try {
        // NAPRAWIONE: Użycie getAuthHeaders()
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds/${thresholdId}`, {
            method: "PUT",
            headers: getAuthHeaders(),
            body: JSON.stringify(thresholdData),
        });
        if (!response.ok) throw new Error("Błąd edycji");
        return await response.json();
    } catch (error) {
        console.error(error);
        return false;
    }
};

export const deleteThreshold = async (groupId, thresholdId) => {
    try {
        // NAPRAWIONE: Dodano nagłówek Authorization
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/thresholds/${thresholdId}`, {
            method: "DELETE",
            headers: { "Authorization": "Bearer " + sessionStorage.getItem("token") }
        });
        if (!response.ok) throw new Error("Błąd usuwania");
        return await response.json();
    } catch (error) {
        console.error(error);
        return false;
    }
};