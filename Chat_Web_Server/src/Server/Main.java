package Server;

public class Main {
    public static void main(String[] args){
        Server server = new Server(8080);
        new Thread(server).start();
    }
}
