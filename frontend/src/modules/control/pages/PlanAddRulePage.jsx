import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function PlanAddRulePage() {
  const nav = useNavigate();
  const { planId } = useParams();
  const [devices, setDevices] = useState([]);

  const [deviceId, setDeviceId] = useState("");
  const [key, setKey] = useState("");
  const [val, setVal] = useState("");
  const [timeWindow, setTimeWindow] = useState(""); // opcjonalne

  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const d = await ControlApi.listDevices();
        setDevices(d || []);
      } catch (e) {
        setError(e);
      }
    })();
  }, []);

  async function add() {
    setError(null);
    try {
      await ControlApi.addRule(planId, {
        deviceId: Number(deviceId),
        states: { [key]: Number(val) },
        timeWindow: timeWindow.trim() ? timeWindow.trim() : null,
      });
      nav(`/sterowanie/plany/${planId}`);
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Dodanie reguły">
      <ErrorBox error={error} />
      <div style={{ maxWidth: 520, margin: "0 auto", display: "grid", gap: "0.75rem" }}>
        <label>
          Urządzenie:
          <select value={deviceId} onChange={(e) => setDeviceId(e.target.value)}>
            <option value="">-- wybierz --</option>
            {devices.map((d) => (
              <option key={d.id} value={d.id}>{d.name} (ID: {d.id})</option>
            ))}
          </select>
        </label>

        <label>
          Klucz parametru:
          <input value={key} onChange={(e) => setKey(e.target.value)} />
        </label>

        <label>
          Wartość (float):
          <input value={val} onChange={(e) => setVal(e.target.value)} />
        </label>

        <label>
          Okno czasowe (opcjonalnie, np. 8:00-10:00):
          <input value={timeWindow} onChange={(e) => setTimeWindow(e.target.value)} />
        </label>

        <button onClick={add} disabled={!deviceId || !key.trim() || val === ""}>
          Dodaj regułę
        </button>
      </div>

      <BackCancelBar cancelTo={`/sterowanie/plany/${planId}`} />
    </Panel>
  );
}