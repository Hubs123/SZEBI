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

  const [error, setError] = useState(null);

  const selectedDevice = devices.find(d => String(d.id) === String(deviceId));
  const selectedType = selectedDevice?.type;

  const deviceStatesMap = {
    noSimulation: ["power"],
    thermometer: ["power", "temp"],
    smokeDetector: ["power", "smokeDetected"],
  };

  const disabledParams = ["temp", "smokeDetected"];

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
        timeWindow: null,
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
          <select value={deviceId} onChange={(e) => setDeviceId(e.target.value)} style={{ padding: "0.4rem", marginLeft: "0.5rem" }}>
            <option value="">-- wybierz --</option>
            {devices.map((d) => (
              <option key={d.id} value={d.id}>{d.name} (ID: {d.id})</option>
            ))}
          </select>
        </label>

        <label>
          Nazwa parametru:
          <select
              value={key}
              onChange={(e) => setKey(e.target.value)}
              disabled={!selectedType}
              style={{ padding: "0.4rem", marginLeft: "0.5rem" }}
            >
              <option value="">-- wybierz --</option>

              {selectedType &&
                deviceStatesMap[selectedType]?.map((param) => (
                  <option
                    key={param}
                    value={param}
                    disabled={disabledParams.includes(param)}
                  >
                    {param}
                    {disabledParams.includes(param) ? " (nieedytowalne)" : ""}
                  </option>
                ))}
            </select>
        </label>

        <label>
          Nowa wartość:
          <input value={val} onChange={(e) => setVal(e.target.value)} style={{ padding: "0.4rem", marginLeft: "0.5rem" }}/>
        </label>

        <button class="btn" onClick={add} disabled={!deviceId || !key.trim() || val === ""}>
          Dodaj
        </button>
      </div>

      <BackCancelBar cancelTo={`/sterowanie/plany/${planId}`} />
    </Panel>
  );
}