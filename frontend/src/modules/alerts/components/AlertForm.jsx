import React, { useState } from 'react';
import AlertService from '../../../services/AlertService';

const AlertForm = ({ onAlertAdded }) => {
    // Stan początkowy formularza - dostosuj pola do backendu
    const initialState = {
        message: '',
        type: 'INFO'
    };

    const [formData, setFormData] = useState(initialState);
    const [submitted, setSubmitted] = useState(false);

    const handleInputChange = (event) => {
        const { name, value } = event.target;
        setFormData({ ...formData, [name]: value });
    };

    const saveAlert = (e) => {
        e.preventDefault();

        // Obiekt wysyłany do Spring Boota
        const data = {
            message: formData.message,
            type: formData.type
            // Jeśli backend wymaga daty, możesz ją dodać tutaj lub pozwolić backendowi ustawić @PrePersist
        };

        AlertService.createAlert(data)
            .then(response => {
                setSubmitted(true);
                setFormData(initialState); // Reset formularza
                if (onAlertAdded) onAlertAdded(); // Odśwież listę w komponencie nadrzędnym

                // Ukryj komunikat sukcesu po 3 sekundach
                setTimeout(() => setSubmitted(false), 3000);
            })
            .catch(e => {
                console.error("Błąd zapisu alertu", e);
                alert("Nie udało się zapisać alertu. Sprawdź konsolę.");
            });
    };

    return (
        <form onSubmit={saveAlert}>
            {submitted && (
                <div className="alert alert-success" role="alert">
                    Alert dodany pomyślnie!
                </div>
            )}

            <div className="form-group mb-3">
                <label htmlFor="message">Treść alertu</label>
                <input
                    type="text"
                    className="form-control"
                    id="message"
                    required
                    value={formData.message}
                    onChange={handleInputChange}
                    name="message"
                    placeholder="Wpisz treść..."
                />
            </div>

            <div className="form-group mb-3">
                <label htmlFor="type">Typ alertu</label>
                <select
                    className="form-control"
                    id="type"
                    name="type"
                    value={formData.type}
                    onChange={handleInputChange}
                >
                    <option value="INFO">Informacja (INFO)</option>
                    <option value="WARNING">Ostrzeżenie (WARNING)</option>
                    <option value="ERROR">Błąd (ERROR)</option>
                </select>
            </div>

            <button type="submit" className="btn btn-primary">
                Zapisz Alert
            </button>
        </form>
    );
};

export default AlertForm;