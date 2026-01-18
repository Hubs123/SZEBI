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
            sessionStorage.clear();
            localStorage.clear();

            const res = await authApi.login(credentials);
            const token = res.data.token;
            sessionStorage.setItem("token", token);
            const payload = JSON.parse(atob(token.split(".")[1]));
            sessionStorage.setItem("userId", payload.sub);

            const usernameToSave = res.data.username || credentials.username;
            sessionStorage.setItem("username", usernameToSave);

            window.location.href = '/';
        } catch (err) {
            alert("Login lub hasło jest niepoprawne.");
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

                    <div style={{ display: 'flex', justifyContent: 'center' }}>
                        <button className="btn mb-3" type="submit">Zaloguj się</button>
                    </div>


                    <div style={{ textAlign: 'center', marginTop: '1.5rem', color: '#666' }}>
                        <Link to="/register" style={{ color: '#667eea', fontWeight: 'bold', textDecoration: 'none' }}>Zarejestruj się</Link>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;