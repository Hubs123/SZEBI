import React, { useState, useEffect } from "react";
import { optimizationApi } from "../../../services/api";

const OptimizationPage = () => {
  // Zarządzanie widokiem: 'menu', 'generate', 'table'
  const [view, setView] = useState("menu");
  const [plans, setPlans] = useState([]);
  const [strategyType, setStrategyType] = useState("Costs_reduction");
  const userId = 1; // Pobierane z sesji użytkownika

  const loadPlans = async () => {
    try {
      const response = await optimizationApi.getPlans();
      setPlans(response.data || []);
    } catch (err) {
      console.error("Błąd pobierania planów:", err);
    }
  };

  // Automatyczne odświeżanie danych w widoku tabeli, aby śledzić zmiany statusu
  useEffect(() => {
    if (view === "table") {
      loadPlans();
      const interval = setInterval(loadPlans, 5000); //
      return () => clearInterval(interval);
    }
  }, [view]);

  const handleGenerate = async () => {
    try {
      // Administrator wybiera parametry i generuje plan
      await optimizationApi.generatePlan(userId, strategyType);
      alert("Nowy plan został wygenerowany.");
      setView("table"); // Przejdź do tabeli po wygenerowaniu
    } catch (err) {
      alert("Błąd generowania planu.");
    }
  };

  const handleToggleStatus = async plan => {
    try {
      // Zmiana statusu planu (Start/Stop)
      const action = plan.status === "Active" ? "stop" : "run";
      await optimizationApi[action === "run" ? "runPlan" : "stopPlan"](
        plan.id,
        userId,
      );
      loadPlans();
    } catch (err) {
      console.error("Błąd zmiany statusu:", err);
    }
  };

  return (
    <div className="panel">
      <div className="simulation-header">
        <h2>🧠 Moduł Optymalizacji Energii</h2>
        {view !== "menu" && (
          <button className="btn" onClick={() => setView("menu")}>
            ⬅ Powrót do menu
          </button>
        )}
      </div>

      {/* SEKCOJA 1: Menu Główne */}
      {view === "menu" && (
        <div className="dashboard-stats" style={{ marginTop: "2rem" }}>
          <div
            className="stat-card"
            style={{ cursor: "pointer" }}
            onClick={() => setView("generate")}
          >
            <h3>➕ Wygeneruj nowy plan</h3>
            <p>Wybierz parametry i stwórz nową strategię.</p>
          </div>
          <div
            className="stat-card"
            style={{ cursor: "pointer" }}
            onClick={() => setView("table")}
          >
            <h3>📋 Przeglądaj plany</h3>
            <p>Zobacz listę wszystkich planów i ich statusy.</p>
          </div>
          <div className="stat-card" style={{ opacity: 0.7 }}>
            <h3>⚡ Zmień status planu</h3>
            <p>Funkcja dostępna wewnątrz tabeli planów.</p>
          </div>
        </div>
      )}

      {/* SEKCJA 2: Formularz Generowania */}
      {view === "generate" && (
        <div
          className="results"
          style={{ padding: "2rem", textAlign: "center" }}
        >
          <h3>Wybierz typ strategii</h3>
          <select
            className="input-style"
            value={strategyType}
            onChange={e => setStrategyType(e.target.value)}
            style={{ margin: "1rem 0", width: "300px" }}
          >
            <option value="Costs_reduction">Redukcja Kosztów</option>
            <option value="Co2_reduction">Redukcja CO2</option>
            <option value="Load_reduction">Redukcja Obciążenia</option>
          </select>
          <br />
          <button className="btn" onClick={handleGenerate}>
            Potwierdź i Generuj
          </button>
        </div>
      )}

      {/* SEKCJA 3: Tabela Planów */}
      {view === "table" && (
        <div className="simulation-table-container">
          <table className="simulation-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nazwa / Strategia</th>
                <th>Oszczędność</th>
                <th>Status</th>
                <th>Akcja</th>
              </tr>
            </thead>
            <tbody>
              {plans.map(plan => (
                <tr key={plan.id}>
                  <td>#{plan.id}</td>
                  <td>{plan.name || plan.strategyType}</td>
                  <td className="value-cell">
                    {plan.costSavings.toFixed(2)} PLN
                  </td>
                  <td>
                    <span
                      className={`battery-level ${plan.status === "Active" ? "high" : "low"}`}
                    >
                      {plan.status}
                    </span>
                  </td>
                  <td>
                    <button
                      className={plan.status === "Active" ? "btn-stop" : "btn"}
                      onClick={() => handleToggleStatus(plan)}
                    >
                      {plan.status === "Active" ? "⏹ Zatrzymaj" : "▶ Uruchom"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {plans.length === 0 && (
            <div className="no-data-message">Brak dostępnych planów.</div>
          )}
        </div>
      )}
    </div>
  );
};

export default OptimizationPage;
