import React, { useState, useEffect } from "react";
import * as optimizationApi from "../../../services/optimizationApi";
import { getUserRole } from "../../../services/roleGuards";
import "../../../App.css";
import "./OptimizationPage.css";

const OptimizationPage = () => {
  const [view, setView] = useState("menu");
  const [plans, setPlans] = useState([]);
  const [strategyType, setStrategyType] = useState("Costs_reduction");
  const [expandedPlanId, setExpandedPlanId] = useState(null);

  const userRole = getUserRole();
  const isAdmin = userRole === "ROLE_ADMIN";
  const userId = 1;

  const loadPlans = async () => {
    try {
      const data = await optimizationApi.fetchPlans();
      setPlans(data || []);
    } catch (err) {
      console.error("Błąd pobierania planów:", err);
    }
  };

  useEffect(() => {
    if (view === "table") {
      loadPlans();
      const interval = setInterval(loadPlans, 3000);
      return () => clearInterval(interval);
    }
  }, [view]);


  const handleGenerate = async () => {
    if (!isAdmin) return alert("Brak uprawnień. Tylko administrator.");
    try {
      await optimizationApi.generatePlan(userId, strategyType);
      alert("Plan wygenerowany.");
      setView("table");
    } catch (err) {
      alert("łąd generowania.");
    }
  };

  const handleToggleStatus = async plan => {
    try {
      if (plan.status === "Active") {
        await optimizationApi.stopPlan(plan.id);
      } else {
        await optimizationApi.runPlan(plan.id);
      }
      loadPlans();
    } catch (err) {
      alert("Błąd zmiany statusu.");
    }
  };

  const handleDelete = async id => {
    if (!isAdmin) return;
    if (window.confirm("Czy na pewno trwale usunąć ten plan?")) {
      try {
        await optimizationApi.deletePlan(id);
        loadPlans();
      } catch (e) {
        alert("Błąd usuwania planu.");
      }
    }
  };

  const handleRename = async (id, currentName) => {
    const newName = prompt("Wpisz nową nazwę planu:", currentName || "");
    if (newName && newName !== currentName) {
      try {
        await optimizationApi.renamePlan(id, newName);
        loadPlans();
      } catch (e) {
        alert("Nie udało się zmienić nazwy.");
      }
    }
  };

  const toggleRules = id => {
    setExpandedPlanId(expandedPlanId === id ? null : id);
  };

  return (
    <div className="module-container">
      <div className="panel" style={{ width: "100%", maxWidth: "1100px" }}>
        <div
          className="module-header"
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <div>
            <h2 style={{ margin: 0 }}>Optymalizacja Energii</h2>
          </div>
          {view !== "menu" && (
            <button className="btn btn-gray" onClick={() => setView("menu")}>
              Menu
            </button>
          )}
        </div>

        {view === "menu" && (
          <div className="stats-grid">
            <div
              className={`stat-card ${!isAdmin ? "disabled-card" : ""}`}
              style={{
                cursor: isAdmin ? "pointer" : "not-allowed",
                position: "relative",
              }}
              onClick={() => isAdmin && setView("generate")}
            >
              <h3>Wygeneruj Plan</h3>
              <p>Stwórz nową strategię optymalizacji.</p>
              {!isAdmin && <div className="locked-overlay">🔒 Tylko Admin</div>}
            </div>

            <div
              className="stat-card"
              style={{
                cursor: "pointer",
                background: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
              }}
              onClick={() => setView("table")}
            >
              <h3>Przeglądaj Plany</h3>
              <p>Lista aktywnych i archiwalnych planów.</p>
            </div>
          </div>
        )}

        {view === "generate" && isAdmin && (
          <div className="generate-container">
            <h3>Konfiguracja strategii</h3>
            <div
              className="form-group"
              style={{ maxWidth: "400px", margin: "2rem auto" }}
            >
              <label>Typ optymalizacji:</label>
              <select
                value={strategyType}
                onChange={e => setStrategyType(e.target.value)}
              >
                <option value="Costs_reduction">
                  Redukcja Kosztów (PLN)
                </option>
                <option value="Load_reduction">
                  Redukcja Obciążenia (Peak Shaving)
                </option>
              </select>
            </div>

            <div className="actions-row">
              <button className="btn btn-gray" onClick={() => setView("menu")}>
                Anuluj
              </button>
              <button className="btn" onClick={handleGenerate}>
                Generuj
              </button>
            </div>
          </div>
        )}

        {view === "table" && (
          <div className="simulation-table-container">
            <table className="simulation-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nazwa / Strategia</th>
                  <th>Oszczędność</th>
                  <th>Status</th>
                  <th>Akcje</th>
                </tr>
              </thead>
              <tbody>
                {plans.length > 0 ? (
                  plans.map(plan => (
                    <React.Fragment key={plan.id}>
                      <tr
                        className={
                          expandedPlanId === plan.id
                            ? "expanded-row-parent"
                            : ""
                        }
                      >
                        <td>
                          <strong>#{plan.id}</strong>
                        </td>
                        <td>
                          {plan.name || (
                            <span
                              style={{ fontStyle: "italic", color: "#999" }}
                            >
                              Bez nazwy
                            </span>
                          )}
                          <div style={{ fontSize: "0.75rem", color: "#666" }}>
                            {plan.strategyType}
                          </div>
                        </td>
                        <td className="value-cell">
                          {plan.costSavings?.toFixed(2)} PLN
                        </td>
                        <td>
                          <span
                            className={`status-pill ${plan.status.toLowerCase()}`}
                          >
                            {plan.status === "Active"
                              ? "W toku"
                              : plan.status === "Stopped"
                                ? "Stop"
                                : "Szkic"}
                          </span>
                        </td>
                        <td className="actions-cell">
                          {/* Info (Rules) */}
                          <button
                            className="action-btn info"
                            onClick={() => toggleRules(plan.id)}
                            title="Pokaż/Ukryj Reguły"
                          >
                            {expandedPlanId === plan.id ? "▲" : "▼"}
                          </button>

                          {/* Edycja Nazwy */}
                          <button
                            className="action-btn edit"
                            onClick={() => handleRename(plan.id, plan.name)}
                            title="Zmień nazwę"
                          >
                            ✎
                          </button>

                          {/* Start/Stop */}
                          <button
                            className={`action-btn ${plan.status === "Active" ? "stop" : "start"}`}
                            onClick={() => handleToggleStatus(plan)}
                            title={
                              plan.status === "Active" ? "Zatrzymaj" : "Uruchom"
                            }
                          >
                            {plan.status === "Active" ? "⏹" : "▶"}
                          </button>

                          {/* Usuwanie (Tylko Admin) */}
                          {isAdmin && (
                            <button
                              className="action-btn delete"
                              onClick={() => handleDelete(plan.id)}
                              title="Usuń plan"
                            >
                              🗑
                            </button>
                          )}
                        </td>
                      </tr>

                      {/* --- ROZWINIĘTE REGUŁY --- */}
                      {expandedPlanId === plan.id && (
                        <tr className="rules-row">
                          <td colSpan="5">
                            <div className="rules-container">
                              <h4>Harmonogram Automatyzacji</h4>
                              {plan.rules && plan.rules.length > 0 ? (
                                <div className="rules-grid">
                                  {plan.rules.map((rule, idx) => (
                                    <div key={idx} className="rule-badge">
                                      <span className="rule-time">
                                        {rule.timeWindow}
                                      </span>
                                      <span className="rule-dev">
                                        Urządzenie #{rule.deviceId}
                                      </span>
                                      <div className="rule-states">
                                        {Object.entries(rule.states).map(
                                          ([k, v]) => (
                                            <span key={k}>
                                              {k}: <strong>{v}</strong>
                                            </span>
                                          ),
                                        )}
                                      </div>
                                    </div>
                                  ))}
                                </div>
                              ) : (
                                <p
                                  style={{ color: "#999", fontStyle: "italic" }}
                                >
                                  Brak wygenerowanych reguł.
                                </p>
                              )}
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  ))
                ) : (
                  <tr>
                    <td
                      colSpan="5"
                      style={{
                        textAlign: "center",
                        padding: "2rem",
                        color: "#888",
                      }}
                    >
                      Brak planów.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default OptimizationPage;
