import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import BackCancelBar from "../components/BackCancelBar";

export default function PlanActionsPage() {
  const nav = useNavigate();
  const { planId } = useParams();

  return (
    <Panel title="Wybranie planu">
      <div style={{ textAlign: "center", marginBottom: "1rem", opacity: 0.85 }}>
        Plan ID: {planId}
      </div>

      <div style={{ display: "grid", gap: "1rem", maxWidth: 520, margin: "0 auto" }}>
        <button onClick={() => nav("aktywuj")}>Aktywacja planu</button>
        <button onClick={() => nav("reguly/dodaj")}>Dodanie reguły</button>
        <button onClick={() => nav("usun")}>Usunięcie</button>
      </div>

      <BackCancelBar cancelTo="/sterowanie/plany" />
    </Panel>
  );
}