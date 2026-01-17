import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function DeviceCreatePage() {
  const nav = useNavigate();
  const [name, setName] = useState("");
  const [type, setType] = useState("");
  const [types, setTypes] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [assignNow, setAssignNow] = useState(false);
  const [roomId, setRoomId] = useState("");
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const [t, r] = await Promise.all([ControlApi.listDeviceTypes(), ControlApi.listRooms()]);
        setTypes(t || []);
        setRooms(r || []);
      } catch (e) {
        setError(e);
      }
    })();
  }, []);

  const roomSelectDisabled = useMemo(() => !assignNow, [assignNow]);

  async function submit() {
    setError(null);
    try {
      const created = await ControlApi.createDevice({
        name,
        type,
        roomId: assignNow ? (roomId === "" ? null : Number(roomId)) : null,
      });
      // diagram: "Dodanie nowego urządzenia" -> ewentualnie "Przypisanie do pokoju" -> koniec
      nav(`/sterowanie/urzadzenia/${created.id}`);
    } catch (e) {
      setError(e);
    }
  }

  return (
    <Panel title="Dodanie nowego urządzenia">
      <ErrorBox error={error} />

      <div style={{ display: "grid", gap: "0.75rem", maxWidth: 520, margin: "0 auto" }}>
        <label>
          Nazwa:
          <input value={name} onChange={(e) => setName(e.target.value)} />
        </label>

        <label>
          Typ:
          <select value={type} onChange={(e) => setType(e.target.value)}>
            <option value="">-- wybierz --</option>
            {types.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </label>

        <label style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
          <input
            type="checkbox"
            checked={assignNow}
            onChange={(e) => setAssignNow(e.target.checked)}
          />
          Przypisz do pokoju
        </label>

        <label style={{ opacity: roomSelectDisabled ? 0.6 : 1 }}>
          Wybierz pokój:
          <select
            disabled={roomSelectDisabled}
            value={roomId}
            onChange={(e) => setRoomId(e.target.value)}
          >
            <option value="">-- brak --</option>
            {rooms.map((r) => (
              <option key={r.id} value={r.id}>{r.name} (ID: {r.id})</option>
            ))}
          </select>
        </label>

        <button onClick={submit} disabled={!name.trim() || !type}>
          Dodaj
        </button>

        {assignNow && rooms.length === 0 ? (
          <div style={{ border: "1px solid #ccc", padding: "0.75rem" }}>
            Brak pokoi w systemie.
          </div>
        ) : null}
      </div>

      <BackCancelBar cancelTo="/sterowanie/urzadzenia" />
    </Panel>
  );
}