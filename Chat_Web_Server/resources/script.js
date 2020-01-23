let chatRoom = document.getElementById("roomName"),
    enterBtn = document.getElementById("enterBtn"),
    sendBtn = document.getElementById("sendBtn"),
    username = document.getElementById("username"),
    message = document.getElementById("text"),
    context = document.getElementById("context"),
    exitBtn = document.getElementById("exitBtn"),
    webSocket = new WebSocket("ws://localhost:8080/");

enterBtn.addEventListener("click", () => {
    webSocket.send("join " + chatRoom.value.toLowerCase());
    context.innerText = "";
});

sendBtn.addEventListener("click", () => {
    webSocket.send(username.value + " " + message.value);
});

exitBtn.addEventListener("click", () => {
    webSocket.close();
    webSocket = undefined;
    webSocket = new WebSocket("ws://localhost:8080/");
    context.innerText = "";
    webSocket.onmessage = setOnMessage;
    webSocket.onerror = (event) => {
        alert("ERROR: WebSocket crashed");
    }
});

webSocket.onmessage = setOnMessage;

webSocket.onerror = (event) => {
    alert("ERROR: WebSocket crashed");
}

function setOnMessage(event){
    let json = JSON.parse(event.data);
    let newField = document.createElement("div"),
        label = document.createElement("label"),
        p = document.createElement("p");
    newField.setAttribute("class", "field");
    label.setAttribute("class", "label");
    label.innerText = json.user;
    p.innerText = json.message;
    newField.appendChild(label);
    newField.appendChild(p);
    context.appendChild(newField);
    //console.log(event.data);
}
window.onclose = () => {
    webSocket.close();
}

webSocket.onopen = () => {
    console.log("Open websocket");
}