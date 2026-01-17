import React from "react";

export default function ErrorBox({ error }) {
  if (!error) return null;
  return (
    <div style={{ border: "1px solid #d33", padding: "0.75rem", marginBottom: "1rem" }}>
      <b>Błąd:</b> {String(error.message || error)}
    </div>
  );
}