import React from "react";
import { useNavigate } from "react-router-dom";

export default function BackCancelBar({ backTo }) {
  const nav = useNavigate();
  return (
    <div style={{ display: "flex", gap: "0.75rem", justifyContent: "center", marginTop: "1.5rem" }}>
      <button className="btn-gray" onClick={() => (backTo ? nav(backTo) : nav(-1))}>Wstecz</button>
      <button className="btn-gray" onClick={() => nav("/sterowanie")}>Anuluj</button>
    </div>
  );
}