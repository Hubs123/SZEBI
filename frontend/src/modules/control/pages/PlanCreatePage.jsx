import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function PlanCreatePage() {
  const nav = useNavigate();
  const [name, setName] = useState("");
  const [error, setError] = useState(null);

  async function create() {
    setError(null);
    try {
      await ControlApi.createPlan(name);
      nav("/sterowanie/plany");
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Utworzenie nowego planu">
      <ErrorBox error={error} />
      <div style={{ maxWidth: 520, margin: "0 auto", display: "grid", gap: "0.75rem" }}>
        <label>
          Nazwa planu:
          <input value={name} onChange={(e) => setName(e.target.value)} />
        </label>
        <button onClick={create} disabled={!name.trim()}>Utwórz</button>
      </div>
      <BackCancelBar cancelTo="/sterowanie/plany" />
    </Panel>
  );
}