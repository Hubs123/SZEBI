import axios from "axios";

// Adres backendu (dostosuj port jeśli inny niż 8080)
const API_URL = "http://localhost:8080/api/optimization";

// --- PRZENIESIONA IMPLEMENTACJA getAuthHeader ---
// Funkcja pomocnicza pobierająca token z sessionStorage
const getAuthHeader = () => {
  const token = sessionStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// Pomocnicza funkcja generująca konfigurację axiosa z nagłówkami
const getConfig = () => ({
  headers: getAuthHeader(),
});

// --- EKSPORTOWANE FUNKCJE API ---

export const fetchPlans = async () => {
  const response = await axios.get(`${API_URL}/plans`, getConfig());
  return response.data;
};

export const generatePlan = async (userId, strategyType) => {
  const response = await axios.post(`${API_URL}/generate`, null, {
    params: { userId, strategyType },
    ...getConfig(),
  });
  return response.data;
};

export const runPlan = async planId => {
  const response = await axios.post(
    `${API_URL}/plans/${planId}/run`,
    null,
    getConfig(),
  );
  return response.data;
};

export const stopPlan = async planId => {
  const response = await axios.post(
    `${API_URL}/plans/${planId}/stop`,
    null,
    getConfig(),
  );
  return response.data;
};

export const deletePlan = async planId => {
  const response = await axios.delete(
    `${API_URL}/plans/${planId}`,
    getConfig(),
  );
  return response.data;
};

export const renamePlan = async (planId, newName) => {
  const response = await axios.patch(
    `${API_URL}/plans/${planId}/rename`,
    newName,
    {
      headers: {
        "Content-Type": "text/plain",
        ...getAuthHeader(),
      },
    },
  );
  return response.data;
};
