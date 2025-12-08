let selectedUsers = [];
let activeChatId = null;
//to do zmiany ale nie mam pomysl jak to zrobic, bo trzebabyloby zrobic modul logowania itd, a to chyba odgornie do apki
//musi byc
const userId = 1;

function addMessageToBox(senderName, content, attachments = []) {
    const msgBox = document.getElementById("messages");
    const div = document.createElement("div");
    div.textContent = `${senderName}: ${content}`;

    attachments.forEach(f => {
        const img = document.createElement("img");
        img.src = `/api/chat/files/${f.id}`;
        div.appendChild(img);
    });

    msgBox.appendChild(div);
    msgBox.scrollTop = msgBox.scrollHeight;
}

async function sendMessage(content = "", file = null) {
    if (!activeChatId) return;

    const formData = new FormData();
    formData.append("userId", userId);
    formData.append("content", content);

    if (file) formData.append("file", file);

    const res = await fetch(`/api/chat/${activeChatId}/send`, {
        method: "POST",
        body: formData
    });

    if (!res.ok) {
        const text = await res.text();
        return alert("Błąd podczas wysyłania wiadomości: " + text);
    }

    const msg = await res.json();
    addMessageToBox(`${msg.sender.firstName} ${msg.sender.lastName}`, msg.content, msg.attachments || []);
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

async function loadMessages(chatId) {
    const res = await fetch(`/api/chat/${chatId}/messages`);
    const messages = await res.json();
    const msgBox = document.getElementById("messages");
    msgBox.innerHTML = "";
    messages.forEach(msg => {
        addMessageToBox(`${msg.sender.firstName} ${msg.sender.lastName}`, msg.content, msg.attachments || []);
    });
}

async function loadChatHistory() {
    const res = await fetch(`/api/chat/all?userId=${userId}`);
    const chats = await res.json();
    const history = document.getElementById("chatHistory");
    history.innerHTML = "";

    chats.forEach(chat => {
        const div = document.createElement("div");
        const deleteButton = document.createElement("button");
        deleteButton.textContent = "❌";
        deleteButton.className = "deleteChatButton";
        deleteButton.onclick = async (e) => {
            e.stopPropagation();
            if (!confirm(`Usunąć czat "${chat.chatName}"?`)) return;
            const res = await fetch(`/api/chat/${chat.id}`, {
                method: "DELETE"
            });
            if (!res.ok) {
                return alert("Błąd przy usuwaniu czatu");
            }
            if (activeChatId === chat.id) activeChatId = null;
            await loadChatHistory();
        };

        div.textContent = chat.chatName;
        div.className = "chatItem";
        div.onclick = () => {
            activeChatId = chat.id;
            document.querySelectorAll(".chatItem").forEach(e => e.classList.remove("active"));
            div.classList.add("active");
            loadMessages(chat.id);
        };
        div.appendChild(deleteButton);
        history.appendChild(div);
    });

    if (chats.length > 0 && !activeChatId) {
        const firstChat = chats[0];
        activeChatId = firstChat.id;

        const firstChatDiv = history.querySelector(".chatItem");
        if (firstChatDiv) firstChatDiv.classList.add("active");

        loadMessages(activeChatId);
    }
}

function addChat() {
    document.getElementById("panel").style.display = "block";
}

function closeNewChatPanel() {
    document.getElementById('panel').style.display = 'none';
}


async function createChat() {
    const name = document.getElementById("nameChat").value.trim();
    if (!name) return alert("Chat name cannot be empty");

    const res = await fetch("/api/chat/create", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ chatName: name, creatorId: userId, participants: selectedUsers })
    });

    if (!res.ok) return alert(await res.text());
    selectedUsers = [];
    document.getElementById("panel").style.display = "none";
    document.getElementById("nameChat").value = "";
    await loadChatHistory();
}

// const searchInput = document.getElementById("searchUser");
// const resultsBox = document.getElementById("userResults");
//
// searchInput.oninput = async () => {
//     const prefix = searchInput.value.trim();
//     if (!prefix) { resultsBox.innerHTML = ""; return; }
//
//     const res = await fetch(`/api/chat/searchUsers?prefix=${prefix}`);
//     const users = await res.json();
//     resultsBox.innerHTML = "";
//     users.forEach(u => {
//         const div = document.createElement("div");
//         div.textContent = `${u.firstName} ${u.lastName}`;
//         div.onclick = () => {
//             if (!selectedUsers.includes(u.id)) selectedUsers.push(u.id);
//             searchInput.value = "";
//             resultsBox.innerHTML = "";
//         };
//         resultsBox.appendChild(div);
//     });
// };

function addUsers() {
    document.getElementById('addUserPanel').style.display = 'block';
}

function closeAddUserPanel() {
    document.getElementById('addUserPanel').style.display = 'none';
}

document.getElementById('confirmAddUserPanel').addEventListener('click', async () => {
    const username = document.getElementById('newUserNamePanel').value.trim();
    if (!username) return alert('Enter a username');

    const response = await fetch(`/api/chat/${activeChatId}/addUser`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: username })
    });

    if (response.ok) {
        alert('User added');
        document.getElementById('newUserNamePanel').value = '';
        closeAddUserPanel();
    } else {
        alert('Error adding user');
    }
});

function removeUsers() {
    const addContainer = document.getElementById('addUserContainer');
    if (addContainer) addContainer.style.display = 'none';

    const container = document.getElementById('userListContainer');
    container.style.display = 'block';

    usersList();
}

async function usersList() {
    const userList = document.getElementById('userList');

    const response = await fetch(`/api/chat/${activeChatId}/users`);
    const users = await response.json();

    userList.innerHTML = '';

    users.forEach(user => {
        const li = document.createElement('li');
        li.textContent = user.username + ' ';

        const removeBtn = document.createElement('button');
        removeBtn.textContent = '❌';
        removeBtn.onclick = () => deleteUserFromChat(user.id);

        li.appendChild(removeBtn);
        userList.appendChild(li);
    });
}

async function deleteUserFromChat(userId) {
    const response = await fetch(`/chat/${activeChatId}/users/${userId}`, {
        method: "DELETE"
    });

    if (response.ok) {
        alert('User removed');
        await usersList();
    } else {
        alert('Error removing user');
    }
}

function enableUserSearch(inputElement, resultsElement, onSelect) {

    inputElement.oninput = async () => {
        const prefix = inputElement.value.trim();
        if (!prefix) {
            resultsElement.innerHTML = "";
            return;
        }

        const res = await fetch(`/api/chat/searchUsers?prefix=${prefix}`);
        const users = await res.json();

        resultsElement.innerHTML = "";

        users.forEach(user => {
            const div = document.createElement("div");
            div.textContent = `${user.firstName} ${user.lastName}`;
            div.className = "autocomplete-item";

            div.onclick = () => {
                onSelect(user);
                resultsElement.innerHTML = "";
            };

            resultsElement.appendChild(div);
        });
    };
}

enableUserSearch(
    document.getElementById("searchUser"),
    document.getElementById("userResults"),
    (user) => {
        if (!selectedUsers.includes(user.id)) selectedUsers.push(user.id);
        document.getElementById("searchUser").value = "";
    }
);

enableUserSearch(
    document.getElementById("newUserNamePanel"),
    document.getElementById("addUserResults"),
    (user) => {
        document.getElementById("newUserNamePanel").value =
            `${user.firstName} ${user.lastName}`;

        // możesz od razu zapisać user.id, jeśli chcesz
        selectedAddUserId = user.id;
    }
);


loadChatHistory();