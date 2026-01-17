import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function PlanActivatePage() {
  const nav = useNavigate();
  const { planId } = useParams();
  const [error, setError] = useState(null);

  async function activate() {
    setError(null);
    try {
      await ControlApi.activatePlan(planId);
      nav(`/sterowanie/plany/${planId}`);
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Aktywacja planu">
      <ErrorBox error={error} />
      <div style={{ textAlign: "center" }}>
        Aktywować plan ID: <b>{planId}</b>?
      </div>
      <div style={{ display: "flex", justifyContent: "center", marginTop: "1rem" }}>
        <button onClick={activate}>Aktywuj</button>
      </div>
      <BackCancelBar cancelTo={`/sterowanie/plany/${planId}`} />
    </Panel>
  );
}