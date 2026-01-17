const API = "http://localhost:8080/api";

export const getAlerts = (role) =>
    fetch(`${API}/alerts?role=${role}`).then(res => res.json());

export const getDeviceGroups = () =>
    fetch(`${API}/admin/alerts/groups`).then(res => res.json());

export const getThresholds = (groupId) =>
    fetch(`${API}/admin/alerts/groups/${groupId}/thresholds`)
        .then(res => res.json());

export const getReactions = (groupId) =>
    fetch(`${API}/maintenance/alerts/groups/${groupId}/reactions`)
        .then(res => res.json());
