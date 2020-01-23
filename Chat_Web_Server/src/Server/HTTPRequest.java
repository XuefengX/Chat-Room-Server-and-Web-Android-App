package Server;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.*;

class HTTPRequest implements Runnable{
    private SocketChannel socketChannel;
    private Socket socket;
    private static Map<String, ChatRoom> roomList;

    HTTPRequest(SocketChannel socketChannel, Map<String, ChatRoom> roomList){
        this.socketChannel = socketChannel;
        this.socket = socketChannel.socket();
        this.roomList = roomList;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try{
            receiveRequest();
        }catch (IOException e){
            System.out.println("ERROR when get input stream: " + e.getMessage());
        }
    }

    private void receiveRequest() throws IOException {
        Scanner sc = new Scanner(socket.getInputStream());
        List<String> inputs = new ArrayList<>();
        do{
            if(sc.hasNextLine())
                inputs.add(sc.nextLine());
        } while (!inputs.isEmpty() && !(inputs.get(inputs.size() - 1).equals("")));
        Map<String, String> requestHeader = new HashMap<>();
        for(String s : inputs) {
            System.out.println(s);
        }
        for(String s : inputs){
            if(!s.equals("")) {
                String[] splitString = s.split(" ", 2);
                requestHeader.put(splitString[0], splitString[1]);
            }
        }
        new HTTPResponse(socketChannel, requestHeader, roomList).sendResponse();
    }
}
