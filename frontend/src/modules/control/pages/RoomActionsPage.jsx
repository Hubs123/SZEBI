import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import BackCancelBar from "../components/BackCancelBar";

export default function RoomActionsPage() {
  const nav = useNavigate();
  const { roomId } = useParams();

  return (
    <Panel title="Wybranie pokoju">
      <div style={{ textAlign: "center", marginBottom: "1rem", opacity: 0.85 }}>
        Pokój ID: {roomId}
      </div>

      <div style={{ display: "grid", gap: "1rem", maxWidth: 520, margin: "0 auto" }}>
        <button onClick={() => nav("grupowe")}>Grupowe sterowanie</button>
        <button onClick={() => nav("urzadzenia")}>Urządzenia w pokoju</button>
      </div>

      <BackCancelBar cancelTo="/sterowanie/pokoje" />
    </Panel>
  );
}