import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function DeviceParamsPage() {
  const nav = useNavigate();
  const { deviceId } = useParams();
  const [current, setCurrent] = useState(null);
  const [key, setKey] = useState("");
  const [val, setVal] = useState("");
  const [error, setError] = useState(null);
  const [blockedMsg, setBlockedMsg] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const st = await ControlApi.getDeviceStates(deviceId);
        setCurrent(st);
      } catch (e) {
        setError(e);
      }
    })();
  }, [deviceId]);

  async function apply() {
    setError(null);
    setBlockedMsg("");
    try {
      const v = Number(val);
      await ControlApi.setDeviceStates(deviceId, { [key]: v });
      const st = await ControlApi.getDeviceStates(deviceId);
      setCurrent(st);
      setKey("");
      setVal("");
    } catch (e) {
      if (e.status === 409) {
        setBlockedMsg("Brak możliwości zmiany parametrów - parametry ustawione przez procedurę alarmową.");
        return;
      }
      setError(e);
    }
  }

  return (
    <Panel title="Zmiana parametrów">
      <ErrorBox error={error} />

      {blockedMsg ? (
        <div style={{ border: "1px solid #ccc", padding: "0.75rem", marginBottom: "1rem" }}>
          {blockedMsg}
        </div>
      ) : null}

      <div style={{ maxWidth: 640, margin: "0 auto", display: "grid", gap: "0.75rem" }}>
        <div style={{ border: "1px solid #ccc", padding: "0.75rem" }}>
          <div style={{ fontWeight: 600, marginBottom: "0.5rem" }}>Aktualne stany:</div>
          <pre style={{ margin: 0 }}>{JSON.stringify(current, null, 2)}</pre>
        </div>

        <label>
          Klucz parametru:
          <input value={key} onChange={(e) => setKey(e.target.value)} />
        </label>

        <label>
          Wartość (float):
          <input value={val} onChange={(e) => setVal(e.target.value)} />
        </label>

        <button onClick={apply} disabled={!key.trim() || val === ""}>
          Ustaw
        </button>
      </div>

      <BackCancelBar cancelTo={`/sterowanie/urzadzenia/${deviceId}`} />
    </Panel>
  );
}