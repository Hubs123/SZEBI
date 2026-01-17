import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import ListCard from "../components/ListCard";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

export default function RoomsListPage() {
  const nav = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const r = await ControlApi.listRooms();
        setRooms(r || []);
      } catch (e) {
        setError(e);
      }
    })();
  }, []);

  return (
    <Panel title="Lista pokoi">
      <ErrorBox error={error} />

      <div style={{ display: "grid", gap: "0.75rem" }}>
        {rooms.map((r) => (
          <ListCard
            key={r.id}
            title={`${r.name} (ID: ${r.id})`}
            onClick={() => nav(`${r.id}`)}
          />
        ))}
        {rooms.length === 0 ? <div style={{ textAlign: "center", opacity: 0.8 }}>Brak pokoi.</div> : null}
      </div>

      <BackCancelBar cancelTo="/sterowanie" />
    </Panel>
  );
}