import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function DeviceDeletePage() {
  const nav = useNavigate();
  const { deviceId } = useParams();
  const [error, setError] = useState(null);

  async function remove() {
    setError(null);
    try {
      await ControlApi.deleteDevice(deviceId);
      nav("/sterowanie/urzadzenia");
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Usunięcie urządzenia">
      <ErrorBox error={error} />
      <div style={{ textAlign: "center", fontWeight: 600 }}>
        Czy na pewno usunąć urządzenie o ID {deviceId}?
      </div>

      <div style={{ display: "flex", gap: "0.75rem", justifyContent: "center", marginTop: "1rem" }}>
        <button class="btn" onClick={remove}>Usuń</button>
      </div>

      <BackCancelBar cancelTo={`/sterowanie/urzadzenia/${deviceId}`} />
    </Panel>
  );
}