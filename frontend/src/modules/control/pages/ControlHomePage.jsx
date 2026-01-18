import React from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";
import { requireAdmin, requireResident } from "../../../services/roleGuards";

export default function ControlHomePage() {
  const nav = useNavigate();

  return (
    <Panel title="Moduł sterowania">
      <div style={{ display: "grid", gap: "1rem", maxWidth: 520, margin: "0 auto" }}>
        {requireResident() ? (
          <>
            <button className="btn" onClick={() => nav("urzadzenia")}>Urządzenia</button>
            <button className="btn" onClick={() => nav("pokoje")}>Pokoje</button>
            <button className="btn" onClick={() => nav("plany")}>Plany</button>
          </>
        ) : null}

        {requireAdmin() ? (
          <button className="btn" onClick={() => nav("administracja")}>Administracja</button>
        ) : null}

        {!requireResident() && !requireAdmin() ? (
          <div style={{ textAlign: "center", opacity: 0.8 }}>
            Brak uprawnień do modułu sterowania.
          </div>
        ) : null}
      </div>
    </Panel>
  );
}