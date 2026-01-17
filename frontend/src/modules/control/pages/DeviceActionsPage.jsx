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
        Urządzenie ID: {deviceId}
      </div>

      <div style={{ display: "grid", gap: "1rem", maxWidth: 520, margin: "0 auto" }}>
        <button onClick={() => nav(`parametry`)}>Ustawienie parametrów</button>
        <button onClick={() => nav(`przypisz`)}>Wybranie opcji przypisania do pokoju</button>
        <button onClick={() => nav(`/sterowanie/urzadzenia/${deviceId}?akcja=usun`)}>
          Usunięcie
        </button>
      </div>

      <BackCancelBar cancelTo="/sterowanie/urzadzenia" />
    </Panel>
  );
}