import React from "react";

export default function Panel({ title, children }) {
  return (
    <div className="panel" style={{ maxWidth: 900, margin: "0 auto", padding: "2rem" }}>
      <h2 style={{ textAlign: "center", marginBottom: "1.5rem" }}>{title}</h2>
      {children}
    </div>
  );
}