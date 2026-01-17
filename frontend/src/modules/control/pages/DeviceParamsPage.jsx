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

  const disabledParams = ["temp", "smokeDetected"];

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
        setBlockedMsg("Brak możliwości zmiany parametrów");
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
          {current && Object.keys(current).length > 0 ? (
            <ul style={{ margin: 0, paddingLeft: "1.25rem" }}>
              {Object.entries(current).map(([k, v]) => (
                <li key={k}>
                  <b>{k}:</b> {v}
                </li>
              ))}
            </ul>
          ) : (
            <div>Brak aktualnych stanów</div>
          )}
        </div>

        <label>
          Nazwa parametru:
          <select
            value={key}
            onChange={(e) => setKey(e.target.value)}
            style={{ marginLeft: "0.5rem", padding: "0.4rem" }}
          >
            <option value="">-- wybierz parametr --</option>
            {current && Object.keys(current).map((k) => (
              <option
                key={k}
                value={k}
                disabled={disabledParams.includes(k)}
              >
                {k} {disabledParams.includes(k) ? "(nieedytowalne)" : ""}
              </option>
            ))}
          </select>
        </label>

        <label>
          Nowa wartość:
          <input value={val} onChange={(e) => setVal(e.target.value)} style={{ padding: "0.4rem", marginLeft: "0.5rem" }} />
        </label>

        <button class="btn" onClick={apply} disabled={!key.trim() || val === ""}>
          Ustaw
        </button>
      </div>

      <BackCancelBar cancelTo={`/sterowanie/urzadzenia/${deviceId}`} />
    </Panel>
  );
}