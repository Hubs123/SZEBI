import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import ListCard from "../components/ListCard";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function PlansListPage() {
  const nav = useNavigate();
  const [plans, setPlans] = useState([]);
  const [error, setError] = useState(null);

  async function load() {
    setError(null);
    try {
      const p = await ControlApi.listPlans();
      setPlans(p || []);
    } catch (e) {
      setError(e);
    }
  }

  useEffect(() => { load(); }, []);

  return (
    <Panel title="Lista Planów">
      <ErrorBox error={error} />

      <div style={{ display: "flex", justifyContent: "center", marginBottom: "1rem" }}>
        <button class="btn" onClick={() => nav("nowy")}>Dodaj nowy plan</button>
      </div>

      <div style={{ display: "grid", gap: "0.75rem" }}>
        {plans.map((p) => (
          <ListCard
            key={p.id}
            title={`${p.name}`}
            subtitle={`Reguły: ${p.rules ? p.rules.length : 0}`}
            onClick={() => nav(`${p.id}`)}
          />
        ))}
        {plans.length === 0 ? <div style={{ textAlign: "center", opacity: 0.8 }}>Brak planów.</div> : null}
      </div>

      <BackCancelBar cancelTo="/sterowanie" />
    </Panel>
  );
}