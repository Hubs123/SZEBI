let selectedUsers = [];
let activeChatId = null;
const token = localStorage.getItem("token");
const userId = localStorage.getItem("userId");

if (!token || !userId) window.location.href = "login.html";

function authFetch(url, options = {}) {
    options.headers = {
        ...(options.headers || {}),
        "Authorization": "Bearer " + token
    };
    return fetch(url, options);
}

async function checkUsersRole(userId) {
    const res = await authFetch(`/api/chat/${userId}/role`);
    const result = await res.json();
    return result['role'];
}

function addMessageToBox(senderName, content, attachments = []) {
    const msgBox = document.getElementById("messages");

    const div = document.createElement("div");
    div.className = "message p-1 mb-1 border rounded";

    const textNode = document.createElement("div");
    textNode.textContent = `${senderName}: ${content}`;
    div.appendChild(textNode);

    attachments.forEach(f => {

        if (f.fileType && f.fileType.startsWith("image/")) {
            const img = document.createElement("img");
            img.src = `/api/chat/files/${f.id}`;
            img.style.maxWidth = "200px";
            img.style.display = "block";
            img.style.marginTop = "5px";
            img.className = "rounded shadow-sm";
            div.appendChild(img);
        }
        else {
            const a = document.createElement("a");
            a.href = `/api/chat/files/${f.id}`;
            a.textContent = f.fileName || "download file";
            a.target = "_blank";
            a.className = "d-block mt-1";
            div.appendChild(a);
        }
    });

    msgBox.appendChild(div);
    msgBox.scrollTop = msgBox.scrollHeight;
}

async function sendMessage(content = "", file = null) {
    if (!activeChatId) return alert("Select a chat first!");
    const formData = new FormData();
    formData.append("userId", userId);
    formData.append("content", content);
    if (file) formData.append("file", file);

    try {
        const res = await authFetch(`/api/chat/${activeChatId}/send`, {
            method: "POST",
            body: formData
        });
        if (!res.ok) {
            const text = await res.text();
            return alert("Error sending message: " + text);
        }

        const msg = await res.json();
        const senderName = msg.sender ? `${msg.sender.firstName || ""} ${msg.sender.lastName || ""}`.trim() : "Unknown";
        const attachments = msg.attachments || [];
        addMessageToBox(senderName, msg.content || "", attachments);
    } catch (err) {
        console.error("sendMessage error:", err);
        alert("Failed to send message");
    }
}

function sendMsg() {
    const input = document.getElementById("msgInput");
    if (!input.value.trim()) return;
    sendMessage(input.value.trim());
    input.value = "";
}

function sendFile() {
    const fileInput = document.getElementById("fileInput");
    fileInput.click();
    fileInput.onchange = () => {
        const file = fileInput.files[0];
        if (!file) return;
        sendMessage("", file);
    };
}

function enableUserSearch(inputElement, resultsElement, onSelect) {
    inputElement.oninput = async () => {
        const prefix = inputElement.value.trim();
        if (!prefix) { resultsElement.innerHTML = ""; return; }

        const res = await authFetch(`/api/chat/searchUsers?prefix=${prefix}`);
        const users = await res.json();
        resultsElement.innerHTML = "";

        users.forEach(user => {
            const div = document.createElement("div");
            div.textContent = `${user.firstName} ${user.lastName} (${user.username})`;
            div.className = "autocomplete-item";
            div.onclick = () => { onSelect(user); resultsElement.innerHTML = ""; };
            resultsElement.appendChild(div);
        });
    };
}

async function addUsers() {
    if (!activeChatId) return alert("Select a chat first");

    const role = await checkUsersRole(userId);
    if (role !== "ROLE_ADMIN") return alert("You don't have permission");

    const addUserModal = new bootstrap.Modal(document.getElementById('addUserPanel'));
    addUserModal.show();

    const res = await authFetch(`/api/chat/${activeChatId}/availableUsers`);
    const users = await res.json();

    const userList = document.getElementById('userList');
    userList.innerHTML = '';

    users.forEach(u => {
        const li = document.createElement('li');
        li.className = 'd-flex justify-content-between align-items-center mb-1';
        li.textContent = `${u.firstName} ${u.lastName} (${u.username})`;

        const btn = document.createElement('button');
        btn.textContent = "Add";
        btn.className = "btn btn-sm btn-success ms-auto";

        btn.onclick = async () => {
            const addRes = await authFetch(`/api/chat/${activeChatId}/users`, {
                method: 'POST',
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ userId: u.id })
            });
            if (!addRes.ok) return alert(await addRes.text());

            alert("User added successfully!");
            loadChatUsers();
        };

        li.appendChild(btn);
        userList.appendChild(li);
    });
}

const confirmAddBtn = document.getElementById("confirmAddUserPanel");

if (confirmAddBtn) {
    confirmAddBtn.onclick = async () => {
        const username = document.getElementById("newUserNamePanel").value.trim();
        if (!username) return alert("Enter username");

        const res = await authFetch("/api/chat/addUser", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username })
        });

        if (!res.ok) {
            alert(await res.text());
            return;
        }

        const modalEl = document.getElementById("addUserPanel");
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();

        document.getElementById("newUserNamePanel").value = "";
        document.getElementById("addUserResults").innerHTML = "";

        alert("User added successfully");
    };
}

function closeAddUserPanel() {
    const addUserModalEl = document.getElementById('addUserPanel');
    const modalInstance = bootstrap.Modal.getInstance(addUserModalEl);
    if (modalInstance) modalInstance.hide();
}

function closeNewChatPanel() {
    const newChatModalEl = document.getElementById('panel');
    const modalInstance = bootstrap.Modal.getInstance(newChatModalEl);
    if (modalInstance) modalInstance.hide();
}

async function addChat() {
    if (await checkUsersRole(userId) === "ROLE_ADMIN") {
        const newChatModal = new bootstrap.Modal(document.getElementById('panel'));
        newChatModal.show();
    } else alert("You don't have permission");
}

async function createChat() {
    const chatName = document.getElementById("nameChat").value.trim();
    if (!chatName) return alert("Enter chat name");

    const participants = [document.getElementById("searchUser").value.trim()].filter(u => u);

    const res = await authFetch("/api/chat/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ chatName, participants })
    });

    if (!res.ok) return alert(await res.text());
    const chat = await res.json();
    alert("Chat created: " + chat.chatName);
    closeNewChatPanel();
}

async function removeUsers() {
    if (!activeChatId) {
        return alert("Select a chat first");
    }

    const role = await checkUsersRole(userId);
    if (role !== "ROLE_ADMIN") return alert("You don't have permission");

    const userListContainer = document.getElementById("userListContainer");
    userListContainer.style.display = "block";

    const res = await authFetch(`/api/chat/${activeChatId}/users`);
    if (!res.ok) return alert("Cannot load chat users");

    const users = await res.json();
    const userList = document.getElementById("userList");
    userList.innerHTML = '';

    users.forEach(u => {
        const li = document.createElement("li");
        li.className = "d-flex justify-content-between align-items-center mb-1";

        li.innerHTML = `
            <span>${u.firstName} ${u.lastName} (${u.username})</span>
            <button class="btn btn-sm btn-danger">Remove</button>
        `;

        li.querySelector("button").onclick = async () => {
            if (!confirm("Remove this user from chat?")) return;

            const removeRes = await authFetch(`/api/chat/${activeChatId}/users/${u.id}`, {
                method: "DELETE"
            });

            if (!removeRes.ok) return alert(await removeRes.text());

            alert("User removed successfully!");
            removeUsers();
        };

        userList.appendChild(li);
    });
}

enableUserSearch(
    document.getElementById("newUserNamePanel"),
    document.getElementById("addUserResults"),
    (user) => { document.getElementById("newUserNamePanel").value = user.username; }
);

async function loadChats() {
    const currentUserRole = await checkUsersRole(userId);

    const res = await authFetch("/api/chat/all");
    const chats = await res.json();
    const container = document.getElementById("chatHistory");
    container.innerHTML = "";

    chats.forEach(chat => {
        const div = document.createElement("div");
        div.className = "chatItem p-2 border rounded mb-1 d-flex justify-content-between align-items-center";

        const nameSpan = document.createElement("span");
        nameSpan.textContent = chat.chatName;
        div.appendChild(nameSpan);

        div.onclick = () => {
            activeChatId = chat.id;
            document.getElementById("messages").innerHTML = "";
            loadMessages(chat.id);
        };

        if (currentUserRole === "ROLE_ADMIN") {
            const delBtn = document.createElement("button");
            delBtn.textContent = "❌";
            delBtn.className = "deleteChatButton btn btn-sm btn-outline-danger";
            delBtn.onclick = (e) => {
                e.stopPropagation();
                deleteChat(chat.id);
            };
            div.appendChild(delBtn);
        }

        container.appendChild(div);
    });
}

async function loadMessages(chatId) {
    const res = await authFetch(`/api/chat/${chatId}/messages`);
    const messages = await res.json();
    const msgBox = document.getElementById("messages");
    msgBox.innerHTML = "";

    messages.forEach(msg => {
        const senderName = `${msg.sender.firstName} ${msg.sender.lastName}`;
        addMessageToBox(senderName, msg.content, msg.attachments || []);
    });
}

async function loadChatUsers() {
    if (!activeChatId) return;

    const res = await authFetch(`/api/chat/${activeChatId}/users`);
    if (!res.ok) return;

    const users = await res.json();
    const ul = document.getElementById("userList");
    ul.innerHTML = ""; // czyścimy listę przed odświeżeniem

    users.forEach(u => {
        const li = document.createElement("li");
        li.className = "d-flex justify-content-between align-items-center mb-1";

        li.innerHTML = `
            <span>${u.firstName} ${u.lastName} (${u.username})</span>
            <button class="btn btn-sm btn-danger">Remove</button>
        `;
        li.querySelector("button").onclick = () => removeUserFromChat(u.id);
        ul.appendChild(li);
    });
}

async function removeUserFromChat(userIdToRemove) {
    if (!confirm("Remove this user from chat?")) return;

    const res = await authFetch(`/api/chat/${activeChatId}/users/${userIdToRemove}`, {
        method: "DELETE"
    });

    if (!res.ok) {
        alert(await res.text());
        return;
    }

    const ul = document.getElementById("userList");
    const liToRemove = Array.from(ul.children).find(li => {
        return li.dataset.userId == userIdToRemove;
    });
    if (liToRemove) ul.removeChild(liToRemove);
}

function renderChats(chats, currentUserRole) {
    const chatHistory = document.getElementById('chatHistory');
    chatHistory.innerHTML = '';

    chats.forEach(chat => {
        const chatDiv = document.createElement('div');
        chatDiv.className = 'chatItem';
        chatDiv.textContent = chat.chatName;

        if (chat.id === activeChatId) chatDiv.classList.add('active');

        chatDiv.onclick = () => selectChat(chat.id);

        if (currentUserRole === 'ROLE_ADMIN') {
            const delBtn = document.createElement('button');
            delBtn.textContent = '❌';
            delBtn.className = 'deleteChatButton';
            delBtn.onclick = (e) => {
                e.stopPropagation(); // don't select chat
                deleteChat(chat.id);
            };
            chatDiv.appendChild(delBtn);
        }

        chatHistory.appendChild(chatDiv);
    });
}

function deleteChat(chatId) {
    if (!confirm("Are you sure you want to delete this chat?")) return;

    authFetch(`/api/chat/${chatId}`, { method: "DELETE" })
        .then(res => {
            if (!res.ok) return res.text().then(t => { throw new Error(t); });

            const container = document.getElementById("chatHistory");
            const chatDiv = Array.from(container.children)
                .find(div => div.onclick && div.textContent.includes(chatId));
            if (chatDiv) container.removeChild(chatDiv);

            if (activeChatId === chatId) {
                activeChatId = null;
                document.getElementById("messages").innerHTML = "";
            }

            loadChats();
        })
        .catch(err => alert("Deleted Chat"));
}

function startPolling() {
    setInterval(() => {
        if (activeChatId) {
            loadMessages(activeChatId);
            loadChatUsers();
        }
        loadChats();
    }, 3000);
}


loadChats();
startPolling();