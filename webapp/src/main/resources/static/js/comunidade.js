let stompClient = null;
let connected = false;

// Conecta ao WebSocket e se inscreve no tópico
function connect() {
    const socketUrl = "/brasfi-webapp-websocket";

    stompClient = new StompJs.Client({
        webSocketFactory: function () {
            return new SockJS("/brasfi-webapp-websocket");
        },
        reconnectDelay: 5000,
        debug: function (str) {
            console.log("[STOMP DEBUG] " + str);
        },
        onConnect: function (frame) {
            connected = true;
            console.log("Conectado: " + frame);

            stompClient.subscribe(`/topic/${comunidadeId}`, function (message) {
                const post = JSON.parse(message.body).content;
                showPost(post);
        });

            document.getElementById("connect").disabled = true;
            document.getElementById("disconnect").disabled = false;
        },
        onStompError: function (frame) {
            console.error("Erro STOMP: ", frame.headers["message"]);
            console.error("Detalhes: ", frame.body);
        }
    });

    stompClient.activate();
}

// Desconecta do WebSocket
function disconnect() {
    if (stompClient && connected) {
        stompClient.deactivate();
        connected = false;
        console.log("Desconectado");

        document.getElementById("connect").disabled = false;
        document.getElementById("disconnect").disabled = true;
    }
}

// Envia uma mensagem para o broker
function sendMessage() {
    //const author = document.getElementById("user").value;
    const content = document.getElementById("message").value;

    if (stompClient && connected && content) {
        stompClient.publish({
            destination: "/app/create-post",
            body: JSON.stringify({'autor': "", 'mensagem': $("#message").val(), 'comunidadeId': comunidadeId})
        });

        document.getElementById("message").value = ""; // Limpa campo
    }
}

// Exibe a mensagem na tela
function showPost(post) {
    /*const tableBody = document.getElementById("livechat");
    const row = document.createElement("tr");
    const cell = document.createElement("td");

    cell.textContent = post;
    row.appendChild(cell);
    tableBody.appendChild(row);*/


}

// Associa os botões
window.addEventListener("load", () => {
    connect();
    //document.getElementById("disconnect").addEventListener("click", disconnect);
    document.getElementById("send").addEventListener("click", function (e) {
        e.preventDefault();
        sendMessage();
    });
});