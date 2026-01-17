import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function PlanDeletePage() {
  const nav = useNavigate();
  const { planId } = useParams();
  const [error, setError] = useState(null);

  async function remove() {
    setError(null);
    try {
      await ControlApi.deletePlan(planId);
      nav("/sterowanie/plany");
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Usunięcie planu">
      <ErrorBox error={error} />
      <div style={{ textAlign: "center" }}>
        Czy na pewno usunąć plan ID: <b>{planId}</b>?
      </div>
      <div style={{ display: "flex", justifyContent: "center", marginTop: "1rem" }}>
        <button onClick={remove}>Usuń plan</button>
      </div>
      <BackCancelBar cancelTo={`/sterowanie/plany/${planId}`} />
    </Panel>
  );
}