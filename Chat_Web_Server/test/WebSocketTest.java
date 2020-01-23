package Server.test;

import Server.WebSocket;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebSocketTest {

    @Test
    void connect() {
    }

    @Test
    void encode() {
        final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        assertEquals("s3pPLMBiTxaQ9kYGzzhZRbK+xOo=", WebSocket.webSocketAcceptKey("dGhlIHNhbXBsZSBub25jZQ==" + GUID));
        assertEquals("sygiKclbz8wuA/zwlt2i0USixnE=", WebSocket.webSocketAcceptKey("WEoPKDDcPN8tlwZH2KwbNg==" + GUID));
    }
}