import React, { useState } from 'react';
import { authApi } from '../../../services/api';
import { useNavigate, Link } from 'react-router-dom';
import '../../../App.css';

const LoginPage = () => {
    const [credentials, setCredentials] = useState({ username: '', password: '' });
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const res = await authApi.login(credentials);
            const token = res.data.token;
            localStorage.setItem("token", token);

            const payload = JSON.parse(atob(token.split(".")[1]));
            localStorage.setItem("userId", payload.sub);

            window.location.href = '/komunikacja';
        } catch (err) {
            alert("Błąd logowania! Sprawdź dane.");
        }
    };

    return (
        <div className="module-container" style={{ justifyContent: 'center' }}>
            <div className="module-content-card" style={{ maxWidth: '450px', minHeight: 'auto' }}>
                <h2 className="module-header" style={{ marginBottom: '2rem' }}>Panel Logowania</h2>
                <form onSubmit={handleLogin}>
                    <div className="form-group">
                        <label>Login użytkownika</label>
                        <input
                            className="form-control"
                            placeholder="Wprowadź login"
                            onChange={e => setCredentials({...credentials, username: e.target.value})}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label>Hasło</label>
                        <input
                            className="form-control"
                            type="password"
                            placeholder="Wprowadź hasło"
                            onChange={e => setCredentials({...credentials, password: e.target.value})}
                            required
                        />
                    </div>
                    <button className="btn w-100 mb-3" type="submit">Zaloguj się</button>

                    <div style={{ textAlign: 'center', marginTop: '1.5rem', color: '#666' }}>
                        <Link to="/register" style={{ color: '#667eea', fontWeight: 'bold', textDecoration: 'none' }}>Zarejestruj się</Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;