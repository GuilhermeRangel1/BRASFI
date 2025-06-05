let stompClient = null;
let connected = false;


const messageInput = document.getElementById('message');
const sendButton = document.getElementById('send');
const chatMessagesArea = document.querySelector('.chat-messages-area');


function connect() {
    const socketUrl = "/brasfi-webapp-websocket"; 

    stompClient = new StompJs.Client({
        webSocketFactory: function () {
            return new SockJS(socketUrl);
        },
        reconnectDelay: 5000,
        debug: function (str) {
            console.log("[STOMP DEBUG] " + str);
        },
        onConnect: function (frame) {
            connected = true;
            console.log("Conectado: " + frame);

            stompClient.subscribe(`/topic/${comunidadeId}`, function (message) {
                const postSaida = JSON.parse(message.body);
                showPost(postSaida.authorName, postSaida.messageContent, postSaida.authorId);
            });

        },
        onStompError: function (frame) {
            console.error("Erro STOMP: ", frame.headers["message"]);
            console.error("Detalhes: ", frame.body);
        }
    });

    stompClient.activate();
}

function sendMessage() {
    const content = messageInput.value.trim();

    if (stompClient && connected && content && comunidadeId !== 0 && usuarioId !== 0) {
        const messagePayload = {
            'mensagem': content,
            'comunidadeId': comunidadeId,
            'usuarioId': usuarioId
        };
        stompClient.publish({
            destination: "/app/create-post", 
            body: JSON.stringify(messagePayload)
        });

        messageInput.value = ""; 
        console.log("Mensagem enviada.");
    } else {
        console.warn("Não foi possível enviar a mensagem. Verifique a conexão, conteúdo e IDs.");
        console.log({connected: connected, content: content, comunidadeId: comunidadeId, usuarioId: usuarioId});
    }
}

function showPost(authorName, messageContent, messageAuthorId) {
    const messageBubble = document.createElement('div');
    messageBubble.classList.add('message-bubble');

    if (messageAuthorId === usuarioId) {
        messageBubble.classList.add('my-message');
    } else {
        messageBubble.classList.add('other-message'); 
    }

    const userAvatar = document.createElement('div');
    userAvatar.classList.add('user-avatar');
    userAvatar.innerHTML = '<i class="fas fa-user-circle"></i>';

    const messageContentDiv = document.createElement('div');
    messageContentDiv.classList.add('message-content');

    const authorSpan = document.createElement('span');
    authorSpan.classList.add('message-author');
    authorSpan.textContent = authorName;

    const messageParagraph = document.createElement('p');
    messageParagraph.classList.add('message-text');
    messageParagraph.textContent = messageContent;

    messageContentDiv.appendChild(authorSpan);
    messageContentDiv.appendChild(messageParagraph);

    messageBubble.appendChild(userAvatar);
    messageBubble.appendChild(messageContentDiv);

    chatMessagesArea.appendChild(messageBubble);

    chatMessagesArea.scrollTop = chatMessagesArea.scrollHeight;
    console.log("Mensagem exibida: " + authorName + " (ID: " + messageAuthorId + ") - " + messageContent);
}

window.addEventListener("load", () => {
    if (comunidadeId !== 0) {
        connect();
    } else {
        console.warn("comunidadeId é 0 na inicialização. Conexão WebSocket não será estabelecida.");
        if (document.getElementById('message')) document.getElementById('message').disabled = true;
        if (document.getElementById('send')) document.getElementById('send').disabled = true;
    }

    if (document.getElementById("send")) {
        document.getElementById("send").addEventListener("click", function (e) {
            e.preventDefault(); 
            sendMessage();
        });
    }

    if (document.getElementById("message")) {
        document.getElementById("message").addEventListener('keypress', (event) => {
            if (event.key === 'Enter') {
                event.preventDefault(); 
                sendMessage();
            }
        });
    }
});