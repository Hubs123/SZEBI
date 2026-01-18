import React, { useEffect, useMemo, useState } from "react";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

function formatJson(v) {
  try {
    return JSON.stringify(v ?? null, null, 2);
  } catch {
    return String(v);
  }
}

function safeParseJson(s) {
  try {
    return { ok: true, value: JSON.parse(s) };
  } catch (e) {
    return { ok: false, error: e };
  }
}

function extractId(maybeJsonOrText) {
  const trimmed = (maybeJsonOrText || "").trim();
  if (!trimmed) return null;

  if (/^\d+$/.test(trimmed)) return parseInt(trimmed, 10);

  const p = safeParseJson(trimmed);
  if (!p.ok) return null;
  const obj = p.value;
  const id = obj?.id ?? obj?.deviceId ?? obj?.planId;
  if (typeof id === "number") return id;
  if (typeof id === "string" && /^\d+$/.test(id)) return parseInt(id, 10);
  return null;
}

function sanitizeCreateDevicePayload(obj) {
  if (!obj || typeof obj !== "object") return null;
  const { name, type, roomId } = obj;
  if (!name || !type) return null;
  return { name, type, roomId: roomId ?? null };
}

function sanitizeCreatePlanPayload(obj) {
  if (!obj || typeof obj !== "object") return null;
  const { name, rules } = obj;
  if (!name || !Array.isArray(rules)) return null;
  return { name, rules };
}

export default function AdministrationPage() {
  const [devices, setDevices] = useState(null);
  const [rooms, setRooms] = useState(null);
  const [plans, setPlans] = useState(null);

  const [error, setError] = useState(null);
  const [actionMsg, setActionMsg] = useState(null);

  const [deviceCreateInput, setDeviceCreateInput] = useState(
    JSON.stringify({ name: "Lampa", type: "noSimulation", roomId: null }, null, 2)
  );
  const [deviceDeleteInput, setDeviceDeleteInput] = useState("");

  const [planCreateInput, setPlanCreateInput] = useState(
    JSON.stringify(
      {
        name: "Nowy plan",
        rules: [
          {
            deviceId: 1,
            states: { power: 1.0 },
            timeWindow: null,
          },
        ],
      },
      null,
      2
    )
  );
  const [planDeleteInput, setPlanDeleteInput] = useState("");

  async function loadAll() {
    setError(null);
    setActionMsg(null);
    try {
      const [d, r, p] = await Promise.all([
        ControlApi.listDevices(),
        ControlApi.listRooms(),
        ControlApi.listAutomationPlans(),
      ]);
      setDevices(d);
      setRooms(r);
      setPlans(p);
    } catch (e) {
      setError(e);
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  const devicesJson = useMemo(() => formatJson(devices ?? []), [devices]);
  const roomsJson = useMemo(() => formatJson(rooms ?? []), [rooms]);
  const plansJson = useMemo(() => formatJson(plans ?? []), [plans]);

  async function onCreateDevice() {
    setError(null);
    setActionMsg(null);
    const parsed = safeParseJson(deviceCreateInput);
    if (!parsed.ok) {
      setError(new Error("Niepoprawny JSON dla createDevice."));
      return;
    }
    const payload = sanitizeCreateDevicePayload(parsed.value) || sanitizeCreateDevicePayload({
      name: parsed.value?.name,
      type: parsed.value?.type,
      roomId: parsed.value?.roomId,
    });
    if (!payload) {
      setError(new Error("createDevice wymaga pól: name, type (opcjonalnie roomId)."));
      return;
    }
    try {
      await ControlApi.createDevice(payload);
      setActionMsg("Utworzono urządzenie.");
      await loadAll();
    } catch (e) {
      setError(e);
    }
  }

  async function onDeleteDevice() {
    setError(null);
    setActionMsg(null);
    const id = extractId(deviceDeleteInput);
    if (!id) {
      setError(new Error("Podaj ID urządzenia (liczba) albo JSON z polem id."));
      return;
    }
    try {
      await ControlApi.deleteDevice(id);
      setActionMsg(`Usunięto urządzenie id=${id}.`);
      setDeviceDeleteInput("");
      await loadAll();
    } catch (e) {
      setError(e);
    }
  }

  async function onCreatePlan() {
    setError(null);
    setActionMsg(null);
    const parsed = safeParseJson(planCreateInput);
    if (!parsed.ok) {
      setError(new Error("Niepoprawny JSON dla createAutomationPlan."));
      return;
    }
    const obj = parsed.value;
    const payload = sanitizeCreatePlanPayload(obj) || sanitizeCreatePlanPayload({
      name: obj?.name,
      rules: obj?.rules,
    });
    if (!payload) {
      setError(new Error("createAutomationPlan wymaga pól: name, rules[]"));
      return;
    }
    try {
      // endpoint oczekuje {name, rules}
      await ControlApi.createAutomationPlan(payload);
      setActionMsg("Utworzono plan automatyzacji.");
      await loadAll();
    } catch (e) {
      setError(e);
    }
  }

  async function onDeletePlan() {
    setError(null);
    setActionMsg(null);
    const id = extractId(planDeleteInput);
    if (!id) {
      setError(new Error("Podaj ID planu (liczba) albo JSON z polem id."));
      return;
    }
    try {
      await ControlApi.deleteAutomationPlan(id);
      setActionMsg(`Usunięto plan id=${id}.`);
      setPlanDeleteInput("");
      await loadAll();
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Administracja">
      <ErrorBox error={error} />

      {actionMsg ? (
        <div style={{ border: "1px solid #2a7", padding: "0.75rem", marginBottom: "1rem" }}>
          {actionMsg}
        </div>
      ) : null}

      <div style={{ display: "flex", justifyContent: "center", marginBottom: "1rem" }}>
        <button className="btn" onClick={loadAll}>Odśwież</button>
      </div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(3, minmax(0, 1fr))",
          gap: "1rem",
          alignItems: "start",
        }}
      >
        {/* DEVICES */}
        <div style={{ border: "1px solid #ddd", borderRadius: 10, padding: "0.75rem" }}>
          <h3 style={{ marginTop: 0 }}>Devices</h3>
          <textarea
            readOnly
            value={devicesJson}
            style={{ width: "100%", height: 260, fontFamily: "monospace" }}
          />

          <div style={{ marginTop: "0.75rem" }}>
            <div style={{ fontWeight: 600, marginBottom: 6 }}>createDevice (JSON)</div>
            <textarea
              value={deviceCreateInput}
              onChange={(e) => setDeviceCreateInput(e.target.value)}
              style={{ width: "100%", height: 140, fontFamily: "monospace" }}
            />
            <div style={{ display: "flex", justifyContent: "center", marginTop: 8 }}>
              <button className="btn" onClick={onCreateDevice}>Create</button>
            </div>
          </div>

          <div style={{ marginTop: "0.75rem" }}>
            <div style={{ fontWeight: 600, marginBottom: 6 }}>deleteDevice (id lub JSON)</div>
            <input
              value={deviceDeleteInput}
              onChange={(e) => setDeviceDeleteInput(e.target.value)}
              className="form-control"
            />
            <div style={{ display: "flex", justifyContent: "center", marginTop: 8 }}>
              <button className="btn" onClick={onDeleteDevice}>Delete</button>
            </div>
          </div>
        </div>

        {/* ROOMS */}
        <div style={{ border: "1px solid #ddd", borderRadius: 10, padding: "0.75rem" }}>
          <h3 style={{ marginTop: 0 }}>Rooms</h3>
          <textarea
            readOnly
            value={roomsJson}
            style={{ width: "100%", height: 260, fontFamily: "monospace" }}
          />
        </div>

        {/* AUTOMATION PLANS */}
        <div style={{ border: "1px solid #ddd", borderRadius: 10, padding: "0.75rem" }}>
          <h3 style={{ marginTop: 0 }}>AutomationPlans</h3>
          <textarea
            readOnly
            value={plansJson}
            style={{ width: "100%", height: 260, fontFamily: "monospace" }}
          />

          <div style={{ marginTop: "0.75rem" }}>
            <div style={{ fontWeight: 600, marginBottom: 6 }}>createAutomationPlan (JSON)</div>
            <textarea
              value={planCreateInput}
              onChange={(e) => setPlanCreateInput(e.target.value)}
              style={{ width: "100%", height: 170, fontFamily: "monospace" }}
            />
            <div style={{ display: "flex", justifyContent: "center", marginTop: 8 }}>
              <button className="btn" onClick={onCreatePlan}>Create</button>
            </div>
          </div>

          <div style={{ marginTop: "0.75rem" }}>
            <div style={{ fontWeight: 600, marginBottom: 6 }}>deleteAutomationPlan (id lub JSON)</div>
            <input
              value={planDeleteInput}
              onChange={(e) => setPlanDeleteInput(e.target.value)}
              className="form-control"
            />
            <div style={{ display: "flex", justifyContent: "center", marginTop: 8 }}>
              <button className="btn" onClick={onDeletePlan}>Delete</button>
            </div>
          </div>
        </div>
      </div>

      <BackCancelBar cancelTo="/sterowanie" />
    </Panel>
  );
}
