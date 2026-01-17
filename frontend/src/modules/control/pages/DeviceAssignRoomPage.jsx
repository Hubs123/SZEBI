import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function DeviceAssignRoomPage() {
  const nav = useNavigate();
  const { deviceId } = useParams();
  const [rooms, setRooms] = useState([]);
  const [roomId, setRoomId] = useState("");
  const [error, setError] = useState(null);
  const [noRoomsMsg, setNoRoomsMsg] = useState("");

  useEffect(() => {
    (async () => {
      setError(null);
      setNoRoomsMsg("");
      try {
        const r = await ControlApi.listRooms();
        setRooms(r || []);
        if (!r || r.length === 0) setNoRoomsMsg("Wyświetlenie komunikatu o braku pokoi");
      } catch (e) {
        setError(e);
      }
    })();
  }, []);

  async function assign() {
    setError(null);
    try {
      await ControlApi.assignDeviceRoom(deviceId, roomId === "" ? null : Number(roomId));
      nav(`/sterowanie/urzadzenia/${deviceId}`);
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Przypisanie do pokoju">
      <ErrorBox error={error} />

      {noRoomsMsg ? (
        <div style={{ border: "1px solid #ccc", padding: "0.75rem", marginBottom: "1rem" }}>
          {noRoomsMsg}
        </div>
      ) : (
        <div style={{ maxWidth: 520, margin: "0 auto", display: "grid", gap: "0.75rem" }}>
          <label>
            Pokój:
            <select value={roomId} onChange={(e) => setRoomId(e.target.value)}>
              <option value="">-- brak --</option>
              {rooms.map((r) => (
                <option key={r.id} value={r.id}>{r.name} (ID: {r.id})</option>
              ))}
            </select>
          </label>
          <button onClick={assign}>Przypisz</button>
        </div>
      )}

      <BackCancelBar cancelTo={`/sterowanie/urzadzenia/${deviceId}`} />
    </Panel>
  );
}