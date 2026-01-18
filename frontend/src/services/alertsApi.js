// frontend/src/services/alertsApi.js

// Upewnij się, że port jest zgodny z Twoim backendem (zwykle 8080)
const API_URL = "http://localhost:8080/api";

export const getAlerts = async (role) => {
    try {
        const response = await fetch(`${API_URL}/alerts?role=${role}`);
        if (!response.ok) throw new Error("Błąd sieci");
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
};

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

export const getReactions = async (groupId) => {
    try {
        const response = await fetch(`${API_URL}/admin/alerts/groups/${groupId}/reactions`);
        if (!response.ok) throw new Error("Błąd pobierania reakcji");
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
        if (!response.ok) throw new Error("Błąd dodawania");
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
        if (!response.ok) throw new Error("Błąd edycji");
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
        if (!response.ok) throw new Error("Błąd usuwania");
        return await response.json();
    } catch (error) {
        console.error(error);
        return false;
    }
};