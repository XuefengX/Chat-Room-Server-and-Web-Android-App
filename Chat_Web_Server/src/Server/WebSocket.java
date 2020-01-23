package Server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class WebSocket {
    private SocketChannel socketChannel;
    private Socket socket;
    private Map<String, String> requestHeader;
    private static Map<String, ChatRoom> roomList;

    private static String[] currentData;
    public WebSocket(SocketChannel socketChannel, Map<String, String> requestHeader, Map<String, ChatRoom> roomList){
        this.socketChannel = socketChannel;
        this.socket = socketChannel.socket();
        this.requestHeader = requestHeader;
        this.roomList = roomList;
    }

    public boolean connect() throws IOException {
        System.out.println("Connecting...");
        final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        final String HEADER_TEMPLATE = "HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: " +
                "Upgrade\r\nSec-WebSocket-Accept: %s\r\n\r\n";

        OutputStream outputStream = socket.getOutputStream();
        if(requestHeader.containsKey("Sec-WebSocket-Key:")){
            String key = requestHeader.get("Sec-WebSocket-Key:");
            System.out.println("Get Sec-WebSocket-Key: " + key);
            String secAccept = String.format(HEADER_TEMPLATE, webSocketAcceptKey(key + GUID));
            outputStream.write(secAccept.getBytes());
            System.out.println(secAccept);
            outputStream.flush();
            return true;
        }
        return false;
    }

    public void receiveMessage() throws IOException {
        //debug();
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            String input = decode(dataInputStream);
            String[] data = input.split("\\s+");
            this.currentData = data;
            System.out.println("Received Request: " + data[0] + " " + data[1]);
            if(data[0].equals("join")){
                joinRoom(data, socketChannel);
            }
    }

    public static void joinRoom(String[] data, SocketChannel socketChannel) throws IOException {
        String chatRoomName = data[1];
        System.out.println("Join chat room: " + chatRoomName);
        System.out.println(roomList.keySet());
        if (roomList.containsKey(chatRoomName)) {
            System.out.println("Contains: " + chatRoomName);
            ChatRoom chatRoom = roomList.get(chatRoomName);
            chatRoom.addClient(socketChannel);
            System.out.println("Joined chat room: " + chatRoomName);
        } else {
            ChatRoom chatRoom = new ChatRoom(socketChannel);
            roomList.put(chatRoomName, chatRoom);
            System.out.println("New Chat Room: " + chatRoomName);
        }
    }


    private static String decode(DataInputStream dataInputStream) throws IOException {
        byte[] header = new byte[2];
        header[0] = dataInputStream.readByte();
        header[1] = dataInputStream.readByte();
        byte fin = 0, opcode = 0, mask = 0;
        long payloadLen = 0;
        fin = (byte) (header[0] >> 7 & 0x1);
        System.out.println("Fin: " + fin);
        opcode = (byte) (header[0] & 0xF);
        System.out.println("Opcode: " + opcode);
        mask = (byte) (header[1] >> 7 & 0x1);
        System.out.println("Mask: " + mask);
        payloadLen = header[1] & 0x7F;
        System.out.println("PayLoadLength: " + payloadLen);
        byte[] extraLen;
        if (payloadLen == 126) {
            extraLen = new byte[2];
            dataInputStream.read(extraLen, 0, 2);
            for (int i = 0; i < extraLen.length; i++) {
                payloadLen = (payloadLen << 8) | (extraLen[i] & 0xff);
                System.out.println("PayLoadLength: " + payloadLen);
            }
        } else if (payloadLen == 127) {
            extraLen = new byte[6];
            dataInputStream.read(extraLen, 0, 6);
            for (int i = 0; i < extraLen.length; i++) {
                payloadLen = (payloadLen << 8) | (extraLen[i] & 0xff);
                System.out.println("PayLoadLength: " + payloadLen);
            }
        }
        byte[] maskKey = new byte[4];
        dataInputStream.readFully(maskKey, 0, 4);
        byte[] data = new byte[(int) payloadLen];
        dataInputStream.readFully(data);
        for (int i = 0; i < payloadLen; i++) {
            data[i] = (byte) (data[i] ^ maskKey[i & 0x3]);
        }
        return new String(data);
    }

    public static String webSocketAcceptKey(String s){
        MessageDigest md = null;
        try{
             md = MessageDigest.getInstance("SHA-1");

        }catch (NoSuchAlgorithmException e){
            System.out.println("Parse key error");
        }
        return Base64.getEncoder().encodeToString(md.digest(s.getBytes()));
    }
    private void debug() throws IOException {
        String message = "Hello";
        byte[] response = new byte[2 + message.length()];
        response[0] = (byte) 0x81;
        response[1] = (byte) message.length();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            response[i + 2] = (byte) c;
        }
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(response);
        outputStream.flush();
    }
    public static void sendResponse(String data, Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        byte[] json = data.getBytes();
        byte[] response = new byte[2 + json.length];
        response[0] = (byte) 0x81;
        response[1] = (byte) json.length;
        for (int i = 0; i < json.length; i++)
            response[i + 2] = json[i];
        // System.out.println("Sending response: " + response[0] + " " + response[1] + " " + new String(response));
        outputStream.write(response);
        outputStream.flush();
    }

    public static String[] getInput(){
        return currentData;
    }
    public static String toJSON(String[] data) {
        return "{ \"user\" : \"" + data[0] + "\", \"message\" : \"" + data[1] + "\" }";
    }
}

