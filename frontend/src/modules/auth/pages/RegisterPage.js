import React, { useState } from 'react';
import { authApi } from '../../../services/api';
import { useNavigate, Link } from 'react-router-dom';
import '../../../App.css';

const RegisterPage = () => {
    const [userData, setUserData] = useState({
        username: '',
        password: '',
        firstName: '',
        lastName: ''
    });
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            await authApi.register(userData);
            alert("Konto zostało utworzone! Możesz się zalogować.");
            navigate('/login');
        } catch (err) {
            alert("Błąd rejestracji! Wybierz inną nazwę użytkownika.");
        }
    };

    return (
        <div className="module-container" style={{ justifyContent: 'center' }}>
            <div className="module-content-card" style={{ maxWidth: '450px', minHeight: 'auto' }}>
                <h2 className="module-header" style={{ marginBottom: '2rem' }}>Rejestracja</h2>
                <form onSubmit={handleRegister}>
                    <div className="form-group">
                        <label>Imię</label>
                        <input
                            className="form-control"
                            placeholder="Twoje imię"
                            onChange={e => setUserData({...userData, firstName: e.target.value})}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Nazwisko</label>
                        <input
                            className="form-control"
                            placeholder="Twoje nazwisko"
                            onChange={e => setUserData({...userData, lastName: e.target.value})}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Login (Username)</label>
                        <input
                            className="form-control"
                            placeholder="Wybierz login"
                            onChange={e => setUserData({...userData, username: e.target.value})}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Hasło</label>
                        <input
                            className="form-control"
                            type="password"
                            placeholder="Wybierz bezpieczne hasło"
                            onChange={e => setUserData({...userData, password: e.target.value})}
                            required
                        />
                    </div>
                    <button className="btn w-100 mb-3" type="submit">Stwórz konto</button>

                    <div style={{ textAlign: 'center', marginTop: '1.5rem', color: '#666' }}>
                        Masz już konto? <Link to="/login" style={{ color: '#667eea', fontWeight: 'bold', textDecoration: 'none' }}>Wróć do logowania</Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default RegisterPage;