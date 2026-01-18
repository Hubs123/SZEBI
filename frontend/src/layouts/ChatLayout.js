import React, { useState, useEffect } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { chatApi } from '../../../services/api';

const ChatLayout = () => {
    const [chats, setChats] = useState([]);

    useEffect(() => {
        chatApi.getChats().then(res => setChats(res.data)).catch(console.error);
    }, []);

    return (
        <div className="container-fluid">
            <div className="row">
                <div className="col-md-3 bg-light border-end" style={{ minHeight: '85vh' }}>
                    <div className="p-3">
                        <h5 className="text-muted text-uppercase small fw-bold">Twoje Konwersacje</h5>
                    </div>
                    <ul className="nav nav-pills flex-column mb-auto p-2">
                        {chats.map(chat => (
                            <li className="nav-item" key={chat.id}>
                                <NavLink
                                    to={`/komunikacja/${chat.id}`}
                                    className={({ isActive }) => `nav-link mb-1 ${isActive ? 'active' : 'link-dark'}`}
                                >
                                    <i className="bi bi-chat-left-text me-2"></i>
                                    {chat.chatName}
                                </NavLink>
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="col-md-9 p-0">
                    <Outlet />
                </div>
            </div>
        </div>
    );
};

export default ChatLayout;