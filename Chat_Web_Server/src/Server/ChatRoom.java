package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ChatRoom {
    private Selector selector;
    private List<SocketChannel> clientsInTheRoom = new ArrayList<>();
    private List<SocketChannel> clientsToBeAdded = new ArrayList<>();
    private List<String[]> allMessages = new ArrayList<>();

    public ChatRoom(SocketChannel socketChannel) throws IOException {
        // This is a constructor call
        this.selector = Selector.open();

        // A Channel has to be in non-blocking mode before it can be registered with the selector
        socketChannel.configureBlocking(false);
        this.selector.selectNow(); // Ben's slides say that this is to make the selector happy
        socketChannel.register(selector, SelectionKey.OP_READ);

        clientsInTheRoom.add(socketChannel);

        // Create a new thread for the new room
        // In this project, one thread handles all for one room
        Thread thread = new Thread(() -> {
            try {
                serverRoom();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public synchronized void addClient(SocketChannel socketChannel) {
        clientsToBeAdded.add(socketChannel);
        System.out.println("Wake up");
        selector.wakeup();
    }

    private synchronized void addAllClients() throws IOException {
        for (SocketChannel socketChannel : clientsToBeAdded) {
            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);

            sendMessages(socketChannel, allMessages);

            clientsInTheRoom.add(socketChannel);
        }
        clientsToBeAdded.clear();
    }

    private void sendMessagesToAllClients(List<String[]> newMessages) throws IOException {
        for (SocketChannel socketChannel : clientsInTheRoom) {
            sendMessages(socketChannel, newMessages);
        }
    }

    private void sendMessages(SocketChannel socketChannel, List<String[]> messages) throws IOException {
        for (var message : messages) {
            SelectionKey key = socketChannel.keyFor(selector);
            key.cancel();

            socketChannel.configureBlocking(true);
            sendMessage(socketChannel.socket(), message[0], message[1]);

            socketChannel.configureBlocking(false);
            selector.selectNow();
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
    }

    private void serverRoom() throws IOException {
        while (true) {
            selector.select();

            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            ArrayList<String[]> newMessages = new ArrayList<>();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    iterator.remove();
                    key.cancel();
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    socketChannel.configureBlocking(true);

                    Socket socket = socketChannel.socket();
                    String[] tmpArr = readMessage(socket);
                    if(!tmpArr[0].equals("join")){
                        newMessages.add(new String[]{tmpArr[0], tmpArr[1]});
                        allMessages.add(new String[]{tmpArr[0], tmpArr[1]});

                        socketChannel.configureBlocking(false);
                        selector.selectNow();
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }else{
                        WebSocket.joinRoom(tmpArr, socketChannel);
                    }
                }
            }
            addAllClients();
            sendMessagesToAllClients(newMessages);
        }
    }

    private static String[] readMessage(Socket socket) throws IOException {
        String clientMessage = "";

        DataInputStream in = new DataInputStream(socket.getInputStream());

        byte firstByte = in.readByte();
        int finCode = (firstByte & 0xff) >> 7;
        int opCode = firstByte & 0xf;

        byte secondByte = in.readByte();
        int mask = (secondByte & 0xff) >> 7;
        int payloadLength = secondByte & 0x7f;

        if (payloadLength <= 125) {
            byte[] maskKey = new byte[4];
            in.read(maskKey);

            byte[] clientMessageBytes = new byte[payloadLength];
            in.read(clientMessageBytes);

            for (int i = 0; i < payloadLength; i++) {
                int realNumber = clientMessageBytes[i] ^ maskKey[i % 4];
                char c = (char) (realNumber & 0xFF);
                clientMessage += c;
            }
        }

        String firstPart = clientMessage.substring(0, clientMessage.indexOf(' '));
        String secondPart = clientMessage.substring(clientMessage.indexOf(' ') + 1);

        return new String[]{firstPart, secondPart};
    }

    private static byte[] encodeMessage(String message) {
        byte[] messageBytes = new byte[2 + message.length()];
        messageBytes[0] = (byte)(-127);
        messageBytes[1] = (byte) message.length();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            messageBytes[i + 2] = (byte) c;
        }
        return messageBytes;
    }

    private static String getJSONMessage(String username, String message) {
        return "{ \"user\" : \"" + username + "\", \"message\" : \"" + message + "\" }";
    }

    private static void sendMessage(Socket socket, String username, String message) throws IOException {
        byte[] messageBytes;
        messageBytes = encodeMessage(getJSONMessage(username, message));
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.write(messageBytes);
    }
}
