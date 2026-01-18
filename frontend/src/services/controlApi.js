const BASE = "http://localhost:8080";

async function http(method, path, body) {
  const token = sessionStorage.getItem("token");
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await res.text();
  const payload = text ? safeJson(text) : null;

  if (!res.ok) {
    const message = typeof payload === "string" ? payload : (payload?.message || text || "Błąd");
    const err = new Error(message);
    err.status = res.status;
    err.payload = payload;
    throw err;
  }
  return payload;
}

function safeJson(t) {
  try { return JSON.parse(t); } catch { return t; }
}

// ---- Devices ----
export const ControlApi = {
  listDevices: () => http("GET", "/api/control/devices"),
  createDevice: (data) => http("POST", "/api/control/devices", data),
  deleteDevice: (id) => http("DELETE", `/api/control/devices/${id}`),
  getDeviceStates: (id) => http("GET", `/api/control/devices/${id}/states`),
  setDeviceStates: (id, states) => http("PUT", `/api/control/devices/${id}/states`, { states }),
  assignDeviceRoom: (id, roomId) => http("PUT", `/api/control/devices/${id}/room`, { roomId }),
  listDeviceTypes: () => http("GET", "/api/control/devices/types"),

  // ---- Rooms ----
  listRooms: () => http("GET", "/api/control/rooms"),
  listRoomDevices: (roomId) => http("GET", `/api/control/rooms/${roomId}/devices`),
  groupCommand: (roomId, type, states) => http("POST", `/api/control/rooms/${roomId}/group-command`, { type, states }),

  // ---- Plans ----
  listPlans: () => http("GET", "/api/control/plans"),
  createPlan: (name, rules) => http("POST", "/api/control/plans", { name, rules }),
  getPlan: (planId) => http("GET", `/api/control/plans/${planId}`),
  deletePlan: (planId) => http("DELETE", `/api/control/plans/${planId}`),
  activatePlan: (planId) => http("POST", `/api/control/plans/${planId}/activate`),
  addRule: (planId, rule) => http("POST", `/api/control/plans/${planId}/rules`, rule),

  // ---- Admin aliases (surowe operacje) ----
  // Nazwy zgodne z wymaganiami z modułu Administracja.
  listAutomationPlans: () => http("GET", "/api/control/plans"),
  createAutomationPlan: (data) => http("POST", "/api/control/plans", data),
  deleteAutomationPlan: (id) => http("DELETE", `/api/control/plans/${id}`),
};