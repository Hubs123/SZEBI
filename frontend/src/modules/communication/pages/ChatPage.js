import React, { useState, useEffect } from 'react';
import { chatApi } from '../../../services/api';
import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom';
import '../../../App.css';

const ChatPage = () => {
    const [chats, setChats] = useState([]);
    const [searchPrefix, setSearchPrefix] = useState("");
    const [foundUsers, setFoundUsers] = useState([]);
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        chatApi.getChats()
            .then(res => {
                const chatList = res.data;
                setChats(chatList);
                if (location.pathname === '/komunikacja' && chatList.length > 0) {
                    navigate(`/komunikacja/${chatList[0].id}`);
                }
            })
            .catch(err => console.error(err));
    }, [location.pathname, navigate]);

    const handleSearch = async (val) => {
        setSearchPrefix(val);
        if (val.length > 1) {
            try {
                const res = await chatApi.searchUsers(val);
                setFoundUsers(res.data);
            } catch (err) {
                console.error(err);
            }
        } else {
            setFoundUsers([]);
        }
    };

    return (
        <div className="module-container">
            <div className="module-header-container" style={{ width: '100%', maxWidth: '1000px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <nav className="nav-tabs" style={{ margin: 0 }}>
                    {chats.map(c => (
                        <NavLink
                            key={c.id}
                            to={`/komunikacja/${c.id}`}
                            className={({ isActive }) => isActive ? "active" : ""}
                        >
                            {c.chatName}
                        </NavLink>
                    ))}
                    {chats.length === 0 && <span style={{ color: 'white', padding: '10px' }}>Brak konwersacji</span>}
                </nav>

                <div style={{ position: 'relative' }}>
                    <input
                        className="form-control"
                        placeholder="Szukaj użytkowników..."
                        value={searchPrefix}
                        onChange={e => handleSearch(e.target.value)}
                        style={{ borderRadius: '20px', width: '250px' }}
                    />
                    {foundUsers.length > 0 && (
                        <div style={{ position: 'absolute', backgroundColor: 'white', border: '1px solid #ddd', borderRadius: '8px', width: '100%', marginTop: '5px', zIndex: 1000, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
                            {foundUsers.map(u => (
                                <div key={u.id} style={{ padding: '10px', borderBottom: '1px solid #eee', fontSize: '0.9rem', color: '#333' }}>
                                    {u.firstName} {u.lastName} ({u.username})
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <div className="module-content-card">
                <Outlet />
            </div>
        </div>
    );
};

export default ChatPage;