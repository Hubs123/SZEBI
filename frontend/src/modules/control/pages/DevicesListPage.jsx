import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import ListCard from "../components/ListCard";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function DevicesListPage() {
  const nav = useNavigate();
  const [devices, setDevices] = useState([]);
  const [error, setError] = useState(null);

  async function load() {
    setError(null);
    try {
      const d = await ControlApi.listDevices();
      setDevices(d || []);
    } catch (e) {
      setError(e);
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <Panel title="Lista urządzeń">
      <ErrorBox error={error} />

      <div style={{ display: "flex", justifyContent: "center", marginBottom: "1rem" }}>
        <button onClick={() => nav("nowe")}>Dodaj nowe urządzenie</button>
      </div>

      <div style={{ display: "grid", gap: "0.75rem" }}>
        {devices.map((d) => (
          <ListCard
            key={d.id}
            title={`${d.name} (ID: ${d.id})`}
            subtitle={`Typ: ${d.type}${d.roomId != null ? ` | Pokój: ${d.roomId}` : ""}`}
            onClick={() => nav(`../urzadzenia/${d.id}`)}
          />
        ))}
        {devices.length === 0 ? <div style={{ textAlign: "center", opacity: 0.8 }}>Brak urządzeń w systemie.</div> : null}
      </div>

      <BackCancelBar cancelTo="/sterowanie" />
    </Panel>
  );
}