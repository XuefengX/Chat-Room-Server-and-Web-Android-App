package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable{
    private int port = 8080;
    private ServerSocketChannel serverSocketChannel;
    private Thread runningThread;
    private static Map<String, ChatRoom> roomList;

    Server(int port){
        this.port = port;
        roomList = new HashMap<String, ChatRoom>();
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
        synchronized (this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(true){
            SocketChannel socketChannel = null;
            try{
                socketChannel = serverSocketChannel.accept();
            }catch (IOException e){
                System.out.println("ERROR when connect to the client: " + e.getMessage());
                e.printStackTrace();
            }
            if(socketChannel != null) new Thread(new HTTPRequest(socketChannel, roomList)).start();
        }
    }

    private void openServerSocket(){
        try{
            this.serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(true);
        }catch(IOException e) {
            System.out.println("ERROR when open the server socket " + e.getMessage());
            e.printStackTrace();
        }
    }
}
