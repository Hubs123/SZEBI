import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import ListCard from "../components/ListCard";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function RoomDevicesPage() {
  const { roomId } = useParams();
  const [devices, setDevices] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const d = await ControlApi.listRoomDevices(roomId);
        setDevices(d || []);
      } catch (e) {
        setError(e);
      }
    })();
  }, [roomId]);

  return (
    <Panel title="Lista urządzeń w pokoju">
      <ErrorBox error={error} />
      <div style={{ display: "grid", gap: "0.75rem" }}>
        {devices.map((d) => (
          <ListCard
            key={d.id}
            title={`${d.name}`}
            subtitle={`Typ: ${d.type}`}
            onClick={() => {}}
            right={<span style={{ opacity: 0.7 }}> </span>}
          />
        ))}
        {devices.length === 0 ? <div style={{ textAlign: "center", opacity: 0.8 }}>Brak urządzeń w pokoju.</div> : null}
      </div>

      <BackCancelBar cancelTo={`/sterowanie/pokoje/${roomId}`} />
    </Panel>
  );
}