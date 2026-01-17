import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { chatApi } from '../../../services/api';
import '../../../App.css';

const ChatWindow = () => {
    const { chatId } = useParams();
    const [messages, setMessages] = useState([]);
    const [text, setText] = useState("");
    const [file, setFile] = useState(null);
    const currentUserId = localStorage.getItem("userId");
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        const fetchMsgs = () => {
            chatApi.getMessages(chatId).then(res => setMessages(res.data)).catch(console.error);
        };
        fetchMsgs();
        const interval = setInterval(fetchMsgs, 3000);
        return () => clearInterval(interval);
    }, [chatId]);

    useEffect(scrollToBottom, [messages]);

    const handleSend = async () => {
        if (!text.trim() && !file) return;
        try {
            await chatApi.sendMessage(chatId, text, file);
            setText("");
            setFile(null);
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div className="d-flex flex-column h-100">
            <div className="messages-display" style={{ flex: '1', minHeight: '400px', background: '#f9f9f9', border: '2px solid #e0e0e0', borderRadius: '12px', padding: '1.5rem', overflowY: 'auto' }}>
                {messages.length > 0 ? (
                    messages.map(m => {
                        const isMine = m.sender?.id.toString() === currentUserId;
                        return (
                            <div key={m.id} className={`message-row ${isMine ? 'mine' : 'theirs'}`}>
                                <div className="message-bubble shadow-sm">
                                    <small className="sender-name">
                                        {m.sender?.firstName} {m.sender?.lastName}
                                    </small>
                                    <div>{m.content}</div>
                                    {m.attachments && m.attachments.map(att => (
                                        <div key={att.id} style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px solid rgba(0,0,0,0.1)' }}>
                                            <a
                                                href={chatApi.getFileUrl(att.id)}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                style={{ color: isMine ? 'white' : '#667eea', fontSize: '0.8rem', textDecoration: 'underline' }}
                                            >
                                                Plik: {att.fileName}
                                            </a>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        );
                    })
                ) : (
                    <div style={{ textAlign: 'center', color: '#999', marginTop: '2rem' }}>Brak wiadomości</div>
                )}
                <div ref={messagesEndRef} />
            </div>

            <div className="chat-input-section" style={{ paddingTop: '5px', marginTop: '5px'}}>
                {file && (
                    <div style={{ marginBottom: '10px', fontSize: '0.8rem', color: '#667eea', display: 'flex', alignItems: 'center', gap: '10px' }}>
                        Załączono: {file.name}
                        <span onClick={() => setFile(null)} style={{ cursor: 'pointer', color: '#ff4d4d', fontWeight: 'bold' }}>Usuń</span>
                    </div>
                )}
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <div className="form-group" style={{ flex: '1', margin: 0 }}>
                        <input
                            className="form-control"
                            placeholder="Wpisz wiadomość..."
                            value={text}
                            onChange={e => setText(e.target.value)}
                            onKeyPress={e => e.key === 'Enter' && handleSend()}
                        />
                    </div>
                    <input
                        type="file"
                        id="fileInput"
                        style={{ display: 'none' }}
                        onChange={e => setFile(e.target.files[0])}
                    />
                    <button
                        className="btn"
                        type="button"
                        onClick={() => document.getElementById('fileInput').click()}
                        style={{ background: '#f0f0f0', color: '#333', boxShadow: 'none', minWidth: '80px' }}
                    >
                        Plik
                    </button>
                    <button
                        className="btn"
                        onClick={handleSend}
                        style={{ whiteSpace: 'nowrap', height: '48px', padding: '0 2rem' }}
                    >
                        Wyślij
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ChatWindow;