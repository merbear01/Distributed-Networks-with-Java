import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server_socket {

    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients;

    public Server_socket(ServerSocket server) {
        this.serverSocket = server;
        this.clients = new HashMap<>();
    }

    public void Begin(){

        try{
            while (!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new connection has been made");
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch(IOException n){
        }
    }


    public static void main(String[] args) throws IOException{
        ServerSocket server = new ServerSocket(1234);
        Server_socket serves = new Server_socket(server);
        serves.Begin();
    }
}

// leave this at it is and create the client handler