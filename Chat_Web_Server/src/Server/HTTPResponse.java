package Server;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class HTTPResponse {
    private SocketChannel socketChannel;
    private Socket socket;
    private Map<String, String> requestHeader;
    private static Map<String, ChatRoom> roomList;

    public HTTPResponse(SocketChannel socketChannel, Map<String, String> requestHeader, Map<String, ChatRoom> roomList) {
        this.socketChannel = socketChannel;
        this.socket = socketChannel.socket();
        this.requestHeader = requestHeader;
        this.roomList = roomList;
    }

    void sendResponse() throws IOException {
        if (requestHeader.containsKey("GET")) {
            if (requestHeader.containsKey("Upgrade:")) {
                if (requestHeader.get("Upgrade:").equals("websocket")) openWebSocket();
            } else {
                serveWebPage();
            }
        }
    }

    private void serveWebPage() throws IOException {
        new HtmlSocket(socket, requestHeader).serveHTML();
    }

    private void openWebSocket() throws IOException {
        WebSocket webSocket = new WebSocket(socketChannel, requestHeader, roomList);
        if(webSocket.connect()){
            System.out.println("Web Socket Connected");
            webSocket.receiveMessage();
        }
    }

}