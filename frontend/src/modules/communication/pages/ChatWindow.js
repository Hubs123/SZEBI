import React, { useState, useEffect, useRef } from 'react';
import { useParams, useOutletContext, useNavigate } from 'react-router-dom';
import { chatApi } from '../../../services/api';
import '../../../App.css';

const ChatWindow = () => {
    const { chatId } = useParams();
    const { isAdmin } = useOutletContext();
    const navigate = useNavigate();
    const [messages, setMessages] = useState([]);
    const [participants, setParticipants] = useState([]);
    const [text, setText] = useState("");
    const [file, setFile] = useState(null);
    const currentUserId = sessionStorage.getItem("userId");
    const messagesEndRef = useRef(null);

    const fetchData = () => {
        chatApi.getMessages(chatId).then(res => setMessages(res.data)).catch(console.error);
        chatApi.getAvailableUsers(chatId).then(res => setParticipants(res.data)).catch(console.error);
    };

    useEffect(() => {
        fetchData();
        const interval = setInterval(fetchData, 3000);
        return () => clearInterval(interval);
    }, [chatId]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }, [messages]);

    const handleSend = async () => {
        if (!text.trim() && !file) return;
        try {
            await chatApi.sendMessage(chatId, text, file);
            setText("");
            setFile(null);
            fetchData();
        } catch (err) { console.error(err); }
    };

    const handleAddUser = async () => {
        const username = prompt("Podaj login użytkownika do dodania:");
        if (username) {
            try {
                await chatApi.addUserToChat(chatId, username);
                fetchData();
            } catch (err) { console.error(err); }
        }
    };

    const handleRemoveUser = async (userId) => {
        if (window.confirm("Usunąć użytkownika z czatu?")) {
            try {
                await chatApi.removeUserFromChat(chatId, userId);
                fetchData();
            } catch (err) { console.error(err); }
        }
    };

    const handleDeleteChat = async () => {
        if (window.confirm("Czy na pewno chcesz usunąć cały czat?")) {
            try {
                await chatApi.deleteChat(chatId);
                // Po usunięciu czyścimy stan i wracamy do głównego widoku
                navigate('/komunikacja');
                window.location.reload();
            } catch (err) {
                console.error(err);
                // W razie błędu i tak odświeżamy, bo backend mógł już usunąć
                navigate('/komunikacja');
                window.location.reload();
            }
        }
    };

    return (
        <div className="d-flex flex-column h-100">
            <div className="d-flex justify-content-between align-items-center mb-3 pb-2 border-bottom">
                <div className="d-flex flex-wrap gap-2 align-items-center">
                    <span className="text-muted small">Uczestnicy:</span>
                    {participants.map(u => (
                        <div key={u.id} className="badge bg-light text-dark border d-flex align-items-center gap-2" style={{ fontWeight: '500', padding: '5px 10px' }}>
                            {u.firstName} {u.lastName}
                            {isAdmin && u.id.toString() !== currentUserId && (
                                <span onClick={() => handleRemoveUser(u.id)} style={{ cursor: 'pointer', color: '#ff4d4d', fontSize: '1.1rem' }}>&times;</span>
                            )}
                        </div>
                    ))}
                </div>
                {isAdmin && (
                    <div className="d-flex gap-2">
                        <button className="chat-action-btn" style={{ padding: '5px 10px', fontSize: '0.75rem', borderRadius: '5rem' }} onClick={handleAddUser}>+ Dodaj osobę</button>
                        <button className="chat-action-btn delete" style={{ padding: '5px 10px', fontSize: '0.75rem', background: '#ff4d4d', borderRadius: '5rem' }} onClick={handleDeleteChat}>Usuń czat</button>
                    </div>
                )}
            </div>

            <div className="messages-display" style={{ flex: '1', minHeight: '400px', background: '#f9f9f9', border: '2px solid #e0e0e0', borderRadius: '12px', padding: '1.5rem', overflowY: 'auto' }}>
                {messages.map(m => {
                    const isMine = m.sender?.id.toString() === currentUserId;
                    return (
                        <div key={m.id} className={`message-row ${isMine ? 'mine' : 'theirs'}`}>
                            <div className="message-bubble shadow-sm">
                                <small className="sender-name">{m.sender?.firstName} {m.sender?.lastName}</small>
                                <div>{m.content}</div>
                                {m.attachments && m.attachments.map(att => (
                                    <div key={att.id} style={{ marginTop: '8px' }}>
                                        <a href={chatApi.getFileUrl(att.id)} target="_blank" rel="noopener noreferrer" style={{ color: isMine ? 'white' : '#667eea', fontSize: '0.8rem', textDecoration: 'underline' }}>
                                            {att.fileName}
                                        </a>
                                    </div>
                                ))}
                            </div>
                        </div>
                    );
                })}
                <div ref={messagesEndRef} />
            </div>

            <div className="chat-input-section" style={{ paddingTop: '20px', marginTop: '10px' }}>
                {file && (
                    <div style={{ marginBottom: '10px', fontSize: '0.8rem', color: '#667eea', display: 'flex', alignItems: 'center', gap: '10px' }}>
                        Załączono: {file.name}
                        <span onClick={() => setFile(null)} style={{ cursor: 'pointer', color: '#ff4d4d', fontWeight: 'bold' }}>Usuń</span>
                    </div>
                )}
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <div className="form-group" style={{ flex: '1', margin: 0 }}>
                        <input className="form-control" placeholder="Wpisz wiadomość..." value={text} onChange={e => setText(e.target.value)} onKeyPress={e => e.key === 'Enter' && handleSend()} />
                    </div>
                    <input type="file" id="fileInput" style={{ display: 'none' }} onChange={e => setFile(e.target.files[0])} />
                    <button className="btn" type="button" onClick={() => document.getElementById('fileInput').click()} style={{ background: '#f0f0f0', color: '#333', boxShadow: 'none', minWidth: '80px' }}>Plik</button>
                    <button className="btn" onClick={handleSend} style={{ whiteSpace: 'nowrap', height: '48px', padding: '0 2rem' }}>Wyślij</button>
                </div>
            </div>
        </div>
    );
};

export default ChatWindow;