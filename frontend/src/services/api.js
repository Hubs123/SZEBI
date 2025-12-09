import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const analysisApi = {
  runAnalysis: (sensorId, startTime, endTime) =>
    api.post('/analysis', {
      sensorId,
      startTime,
      endTime,
    }),
};

export const predictionApi = {
  runPrediction: (sensorId, modelId, modelType, historyDays) =>
    api.post('/prediction', {
      sensorId,
      modelId,
      modelType,
      historyDays,
    }),
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
};

export default api;

