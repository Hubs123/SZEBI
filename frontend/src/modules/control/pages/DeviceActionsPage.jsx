import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import BackCancelBar from "../components/BackCancelBar";

export default function DeviceActionsPage() {
  const nav = useNavigate();
  const { deviceId } = useParams();

  return (
    <Panel title="Wybranie urządzenia">
      <div style={{ textAlign: "center", marginBottom: "1rem", opacity: 0.85 }}>
        ID urządzenia: {deviceId}
      </div>

      <div style={{ display: "grid", gap: "1rem", maxWidth: 520, margin: "0 auto" }}>
        <button class="btn-gray" onClick={() => nav(`parametry`)}>Ustaw parametry</button>
        <button class="btn-gray" onClick={() => nav(`przypisz`)}>Przypisz do pokoju</button>
        <button class="btn-gray" onClick={() => nav(`usun`)}>Usuń urządzenie</button>
      </div>

      <BackCancelBar cancelTo="/sterowanie/urzadzenia" />
    </Panel>
  );
}