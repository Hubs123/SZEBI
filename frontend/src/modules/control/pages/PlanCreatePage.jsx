import React, { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Panel from "../components/Panel";
import ErrorBox from "../components/ErrorBox";
import BackCancelBar from "../components/BackCancelBar";
import { ControlApi } from "../../../services/controlApi";

function emptyRule() {
  return {
    deviceId: "",
    states: [{ key: "", value: "" }],
  };
}

export default function PlanCreatePage() {
  const nav = useNavigate();
  const [name, setName] = useState("");
  const [rules, setRules] = useState([emptyRule()]);
  const [error, setError] = useState(null);

  function updateRule(idx, patch) {
    setRules((prev) => prev.map((r, i) => (i === idx ? { ...r, ...patch } : r)));
  }

  function updateState(ruleIdx, stateIdx, patch) {
    setRules((prev) =>
        prev.map((r, i) => {
          if (i !== ruleIdx) return r;
          const nextStates = r.states.map((s, j) => (j === stateIdx ? { ...s, ...patch } : s));
          return { ...r, states: nextStates };
        })
    );
  }

  function addRule() {
    setRules((prev) => [...prev, emptyRule()]);
  }

  function removeRule(idx) {
    setRules((prev) => prev.filter((_, i) => i !== idx));
  }

  function addState(ruleIdx) {
    setRules((prev) =>
        prev.map((r, i) => {
          if (i !== ruleIdx) return r;
          return { ...r, states: [...r.states, { key: "", value: "" }] };
        })
    );
  }

  function removeState(ruleIdx, stateIdx) {
    setRules((prev) =>
        prev.map((r, i) => {
          if (i !== ruleIdx) return r;
          const next = r.states.filter((_, j) => j !== stateIdx);
          return { ...r, states: next.length ? next : [{ key: "", value: "" }] };
        })
    );
  }

  const { canSubmit, payload } = useMemo(() => {
    const planName = name.trim();
    if (!planName) return { canSubmit: false, payload: null };
    if (!rules.length) return { canSubmit: false, payload: null };

    const outRules = [];
    for (let i = 0; i < rules.length; i++) {
      const r = rules[i];
      const deviceIdNum = Number.parseInt(String(r.deviceId).trim(), 10);
      if (!Number.isFinite(deviceIdNum)) {
        return { canSubmit: false, payload: null };
      }

      const statesObj = {};
      for (let j = 0; j < (r.states || []).length; j++) {
        const key = String(r.states[j]?.key ?? "").trim();
        const valueStr = String(r.states[j]?.value ?? "").trim();

        if (!key) continue;
        const val = Number.parseFloat(valueStr.replace(",", "."));
        if (!Number.isFinite(val)) {
          return { canSubmit: false, payload: null };
        }
        statesObj[key] = val;
      }

      if (Object.keys(statesObj).length === 0) {
        return { canSubmit: false, payload: null };
      }

      outRules.push({ deviceId: deviceIdNum, states: statesObj });
    }

    return {
      canSubmit: true,
      payload: { name: planName, rules: outRules },
    };
  }, [name, rules]);

  async function create() {
    setError(null);
    try {
      await ControlApi.createPlan(payload.name, payload.rules);
      nav("/sterowanie/plany");
    } catch (e) {
      setError(e);
    }
  }

  return (
      <Panel title="Utworzenie nowego planu">
        <ErrorBox error={error} />

        <div style={{ maxWidth: 720, margin: "0 auto", display: "grid", gap: "0.75rem" }}>
          <label style={{ display: "grid", gap: "0.25rem" }}>
            <span>Nazwa planu:</span>
            <input value={name} onChange={(e) => setName(e.target.value)} style={{ padding: "0.4rem" }} />
          </label>

          <div style={{ display: "grid", gap: "0.75rem" }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "0.5rem" }}>
              <h3 style={{ margin: 0 }}>Reguły automatyzacji</h3>
              <button type="button" className="btn" onClick={addRule}>
                + Dodaj regułę
              </button>
            </div>

            {rules.map((r, ruleIdx) => (
                <div
                    key={ruleIdx}
                    style={{
                      border: "1px solid #ddd",
                      borderRadius: 8,
                      padding: "0.75rem",
                      display: "grid",
                      gap: "0.75rem",
                    }}
                >
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "0.5rem" }}>
                    <strong>Reguła {ruleIdx + 1}</strong>
                    <button
                        type="button"
                        className="btn"
                        onClick={() => removeRule(ruleIdx)}
                        disabled={rules.length === 1}
                        title={rules.length === 1 ? "Musi istnieć przynajmniej jedna reguła" : "Usuń regułę"}
                    >
                      Usuń
                    </button>
                  </div>

                  <label style={{ display: "grid", gap: "0.25rem" }}>
                    <span>Id urządzenia:</span>
                    <input
                        value={r.deviceId}
                        onChange={(e) => updateRule(ruleIdx, { deviceId: e.target.value })}
                        style={{ padding: "0.4rem" }}
                        inputMode="numeric"
                    />
                  </label>

                  <div style={{ display: "grid", gap: "0.5rem" }}>
                    <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "0.5rem" }}>
                      <strong>Stany</strong>
                      <button type="button" className="btn" onClick={() => addState(ruleIdx)}>
                        + Dodaj stan
                      </button>
                    </div>

                    {r.states.map((s, stateIdx) => (
                        <div key={stateIdx} style={{ display: "grid", gridTemplateColumns: "1fr 160px auto", gap: "0.5rem" }}>
                          <input
                              value={s.key}
                              onChange={(e) => updateState(ruleIdx, stateIdx, { key: e.target.value })}
                              style={{ padding: "0.4rem" }}
                              placeholder="nazwa stanu"
                          />
                          <input
                              value={s.value}
                              onChange={(e) => updateState(ruleIdx, stateIdx, { value: e.target.value })}
                              style={{ padding: "0.4rem" }}
                              placeholder="wartość"
                              inputMode="decimal"
                          />
                          <button type="button" className="btn" onClick={() => removeState(ruleIdx, stateIdx)}>
                            Usuń
                          </button>
                        </div>
                    ))}
                  </div>
                </div>
            ))}
          </div>

          <button type="button" className="btn" onClick={create} disabled={!canSubmit}>
            Utwórz
          </button>
        </div>

        <BackCancelBar cancelTo="/sterowanie/plany" />
      </Panel>
  );
}