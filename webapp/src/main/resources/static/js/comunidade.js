let stompClient = null;
let connected = false;

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
                showPost(postSaida.authorName, postSaida.messageContent);
            });

        },
        onStompError: function (frame) {
            console.error("Erro STOMP: ", frame.headers["message"]);
            console.error("Detalhes: ", frame.body);
        }
    });

    stompClient.activate();
}

function disconnect() {
    if (stompClient && connected) {
        stompClient.deactivate();
        connected = false;
        console.log("Desconectado");

    }
}

function sendMessage() {
    const content = document.getElementById("message").value;

    if (stompClient && connected && content && comunidadeId !== 0 && usuarioId !== 0) {
        stompClient.publish({
            destination: "/app/create-post",
            body: JSON.stringify({
                'mensagem': content,
                'comunidadeId': comunidadeId, 
                'usuarioId': usuarioId
            })
        });

        document.getElementById("message").value = "";
    } else {
        console.warn("Não foi possível enviar a mensagem. Verifique a conexão, conteúdo e IDs.");
    }
}

function showPost(authorName, messageContent) { 
    const chatBody = document.querySelector(".chat-messages-area");

    const newMessageBubble = document.createElement('div');
    newMessageBubble.classList.add('message-bubble');

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

    newMessageBubble.appendChild(userAvatar);
    newMessageBubble.appendChild(messageContentDiv);

    chatBody.appendChild(newMessageBubble);

    chatBody.scrollTop = chatBody.scrollHeight;
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