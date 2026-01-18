import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    const token = sessionStorage.getItem("token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const analysisApi = {
    runAnalysis: (sensorId, startTime, endTime) =>
        api.post('/analysis', { sensorId, startTime, endTime }),
};

export const predictionApi = {
    runPrediction: (sensorId, modelId, modelType, historyDays) =>
        api.post('/prediction', { sensorId, modelId, modelType, historyDays }),
};

export const reportApi = {
    getReport: (reportId) => api.get(`/reports/${reportId}`),
};

export const dataApi = {
    getMeasurements: (sensorId, start, end) => {
        const params = { sensorId };
        if (start) params.start = start;
        if (end) params.end = end;
        return api.get('/data/measurements', { params });
    },
    getSimulationResults: () => api.get('/data/simulation/results'),
    runSimulation: (date) => {
        const params = date ? { date } : {};
        return api.post('/data/simulation/run', null, { params });
    },
};

export const chatApi = {
    getChats: () => api.get('/chat/all'),
    getMessages: (chatId) => api.get(`/chat/${chatId}/messages`),
    sendMessage: (chatId, content, file) => {
        const formData = new FormData();
        if (content) formData.append('content', content);
        if (file) formData.append('file', file);
        return api.post(`/chat/${chatId}/send`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    getAvailableUsers: (chatId) => api.get(`/chat/${chatId}/availableUsers`),
    searchUsers: (prefix) => api.get('/chat/searchUsers', { params: { prefix } }),
    createChat: (chatName, participants) => api.post('/chat/create', { chatName, participants }),
    deleteChat: (chatId) => api.delete(`/chat/${chatId}`),
    addUserToChat: (chatId, username) => api.post(`/chat/${chatId}/addUser`, { username }),
    removeUserFromChat: (chatId, userId) => api.delete(`/chat/${chatId}/users/${userId}`),
    getUserRole: (userId) => api.get(`/chat/${userId}/role`),
    getFileUrl: (fileId) => `${API_BASE_URL}/chat/files/${fileId}`,
};

export const authApi = {
    login: (credentials) => api.post('/szebi/login', credentials),
    register: (userData) => api.post('/szebi/register', userData),
};

export default api;