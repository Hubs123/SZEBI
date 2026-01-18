import React, { useState, useEffect } from 'react';
import { chatApi } from '../../../services/api';
import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom';
import '../../../App.css';

const ChatPage = () => {
    const [chats, setChats] = useState([]);
    const [searchPrefix, setSearchPrefix] = useState("");
    const [foundUsers, setFoundUsers] = useState([]);
    const [isAdmin, setIsAdmin] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    const loadChats = () => {
        chatApi.getChats()
            .then(res => {
                const chatList = res.data;
                setChats(chatList);
                if (location.pathname === '/komunikacja' && chatList.length > 0) {
                    navigate(`/komunikacja/${chatList[0].id}`, { preventScrollReset: true });
                }
            })
            .catch(console.error);
    };

    useEffect(() => {
        const userId = sessionStorage.getItem("userId");
        if (userId) {
            chatApi.getUserRole(userId)
                .then(res => setIsAdmin(res.data.role === 'ROLE_ADMIN'))
                .catch(() => setIsAdmin(false));
        }
        loadChats();
    }, [location.pathname]);

    const handleSearch = async (val) => {
        setSearchPrefix(val);
        if (val.length > 1) {
            try {
                const res = await chatApi.searchUsers(val);
                setFoundUsers(res.data);
            } catch (err) { console.error(err); }
        } else { setFoundUsers([]); }
    };

    const handleCreateChat = async () => {
        const chatName = prompt("Podaj nazwę nowego czatu:");
        if (chatName) {
            try {
                const res = await chatApi.createChat(chatName, []);
                const newChatId = res.data.id;

                if (newChatId) {
                    navigate(`/komunikacja/${newChatId}`);
                } else {
                    window.location.reload();
                }
            } catch (err) {
                console.error(err);
            }
        }
    };

    const isChatSelected = location.pathname !== '/komunikacja';

    return (
        <div className="module-container">
            <div className="module-header-container" style={{ width: '100%', maxWidth: '1000px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <nav className="nav-tabs" style={{ margin: 0, display: 'flex', alignItems: 'center' }}>
                    {chats.map(c => (
                        <NavLink
                            key={c.id}
                            to={`/komunikacja/${c.id}`}
                            className={({ isActive }) => isActive ? "active" : ""}
                            preventScrollReset={true}
                        >
                            {c.chatName}
                        </NavLink>
                    ))}
                    {isAdmin && (
                        <button
                            onClick={handleCreateChat}
                            style={{ background: 'rgba(255,255,255,0.2)', color: 'white', border: 'none', borderRadius: '15px', padding: '0.35rem 0.75rem', cursor: 'pointer' }}
                        >
                            + Nowy czat
                        </button>
                    )}
                </nav>

                <div style={{ position: 'relative' }}>
                    <input
                        className="form-control"
                        placeholder="Szukaj użytkowników..."
                        value={searchPrefix}
                        onChange={e => handleSearch(e.target.value)}
                        style={{ borderRadius: '20px', width: '220px', height: '48px', padding: '10px' }}
                    />
                    {foundUsers.length > 0 && (
                        <div style={{ position: 'absolute', backgroundColor: 'white', border: '1px solid #ddd', borderRadius: '8px', width: '100%', marginTop: '5px', zIndex: 1000, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
                            {foundUsers.map(u => (
                                <div key={u.id} style={{ padding: '10px', borderBottom: '1px solid #eee', fontSize: '0.8rem', color: '#333' }}>
                                    {u.firstName} {u.lastName} ({u.username})
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <div className="module-content-card">
                {isChatSelected ? (
                    <Outlet context={{ isAdmin }} />
                ) : (
                    <div style={{ textAlign: 'center', paddingTop: '5rem', color: '#666' }}>
                        <h3>Wybierz czat.</h3>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ChatPage;