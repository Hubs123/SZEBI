import React from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";

export default function ControlHomePage() {
  const nav = useNavigate();

  return (
    <Panel title="Moduł sterowania">
      <div style={{ display: "grid", gap: "1rem", maxWidth: 520, margin: "0 auto" }}>
        <button onClick={() => nav("urzadzenia")}>Urządzenia</button>
        <button onClick={() => nav("pokoje")}>Pokoje</button>
        <button onClick={() => nav("plany")}>Plany</button>
      </div>
    </Panel>
  );
}