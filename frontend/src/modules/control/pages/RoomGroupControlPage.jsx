import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function RoomGroupControlPage() {
  const { roomId } = useParams();
  const [types, setTypes] = useState([]);
  const [type, setType] = useState("");
  const [key, setKey] = useState("");
  const [val, setVal] = useState("");
  const [error, setError] = useState(null);

  const [noDevicesMsg, setNoDevicesMsg] = useState("");
  const [lockedMsg, setLockedMsg] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const t = await ControlApi.listDeviceTypes();
        setTypes(t || []);
      } catch (e) {
        setError(e);
      }
    })();
  }, []);

  async function apply() {
    setError(null);
    setNoDevicesMsg("");
    setLockedMsg("");

    try {
      const v = Number(val);
      const res = await ControlApi.groupCommand(roomId, type, { [key]: v });

      if (res?.lockedDeviceIds?.length) {
        setLockedMsg("Brak możliwości zmiany parametrów");
      } else {
        setLockedMsg("");
      }
    } catch (e) {
      if (e.status === 404) {
        setNoDevicesMsg("Brak urządzeń w pokoju");
        return;
      }
      setError(e);
    }
  }

  return (
    <Panel title="Zastosowanie polecenia dla wszystkich urządzeń danego typu w pokoju">
      <ErrorBox error={error} />

      {noDevicesMsg ? (
        <div style={{ border: "1px solid #ccc", padding: "0.75rem", marginBottom: "1rem" }}>
          {noDevicesMsg}
        </div>
      ) : null}

      {lockedMsg ? (
        <div style={{ border: "1px solid #ccc", padding: "0.75rem", marginBottom: "1rem" }}>
          {lockedMsg}
        </div>
      ) : null}

      <div style={{ maxWidth: 520, margin: "0 auto", display: "grid", gap: "0.75rem" }}>
        <label>
          Typ urządzeń:
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">-- wybierz --</option>
            {types.map((t) => (
              <option key={t} value={t}>{t}</option>
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

        <button onClick={apply} disabled={!type || !key.trim() || val === ""}>
          Zastosuj
        </button>
      </div>

      <BackCancelBar cancelTo={`/sterowanie/pokoje/${roomId}`} />
    </Panel>
  );
}