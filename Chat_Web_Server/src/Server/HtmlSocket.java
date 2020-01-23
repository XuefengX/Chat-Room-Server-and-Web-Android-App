package Server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

public class HtmlSocket {
    private Socket socket;
    private Map<String, String> requestHeader;
    private final Map<String, String> CONTENT_TYPE = Map.ofEntries(
            Map.entry("html", "text/html"),
            Map.entry("css", "text/css"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("js", "text/javascript")
    );
    public HtmlSocket(Socket socket, Map<String, String> requestHeader){
        this.socket = socket;
        this.requestHeader = requestHeader;
    }

    public void serveHTML() throws IOException {
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        String html = requestHeader.get("GET").split(" ")[0];
        if (html.equals("/")) html = "/index.html";
        String[] router = html.split("\\.");
        String type = CONTENT_TYPE.get(router[router.length - 1]);
        if (type == null) return;
        if (type.equals("text/html") || type.equals("text/css") || type.equals("text/javascript")) {
            pw.println("HTTP/1.1 200 OK");
            pw.println("Date:" + new Date());
            pw.println("Content-Type: " + type);
            File file = new File("resources" + html);
            pw.println("Content-Length: " + file.length() + "\n");
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNext()) {
                pw.println(fileReader.nextLine());
            }
            pw.flush();
            socket.close();
        } else {
            pw.println("HTTP/1.1 404 NotFound\n");
        }
    }

}
