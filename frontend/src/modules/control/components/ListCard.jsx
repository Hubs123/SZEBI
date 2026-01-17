import React from "react";

export default function ListCard({ title, subtitle, onClick, right }) {
  return (
    <div
      onClick={onClick}
      style={{
        border: "1px solid #ccc",
        padding: "0.75rem 1rem",
        borderRadius: 8,
        cursor: "pointer",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
      }}
    >
      <div>
        <div style={{ fontWeight: 600 }}>{title}</div>
        {subtitle ? <div style={{ opacity: 0.8 }}>{subtitle}</div> : null}
      </div>
      {right ? <div>{right}</div> : <div style={{ opacity: 0.7 }}>→</div>}
    </div>
  );
}