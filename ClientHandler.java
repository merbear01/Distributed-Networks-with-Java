import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private static ClientHandler coordinator = null;
    private static boolean isFirstMember = true;

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;


    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // this is to send things
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // this is to read things

            if (isFirstMember) {
                // If a member is the first one in the group, the member must be informed about it at start-up.
                this.bufferedWriter.write("You are the first member in the group.");
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
                isFirstMember = false;

                // the first one will become the coordinator
                coordinator = this;
                System.out.println("Coordinator: " + clientUsername);
            } else {
                // Any new member will request details of existing members from the server
                // and will receive everyone's IDs, IP addresses and ports
                // including the current group coordinator
                this.bufferedWriter.write("Details of existing members:");
                this.bufferedWriter.newLine();
                for (ClientHandler clientHandler : clientHandlers) {
                    if (clientHandler != this) {
                        this.bufferedWriter.write(clientHandler.clientUsername + "'s" + " IP address: "+clientHandler.socket.getInetAddress().getHostAddress() + ", Port Number: " + clientHandler.socket.getPort());
                        this.bufferedWriter.newLine();
                    }
                }
                if (coordinator != null) {
                    this.bufferedWriter.write("Coordinator: " + coordinator.clientUsername);
                    this.bufferedWriter.newLine();
                }
                this.bufferedWriter.flush();
            }

            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Serving: " + clientUsername + " has entered the chat!!");
            notifyNewMember(this.toString());

        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }

    }
    public void sendPrivateMessage(String recipient, String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equals(recipient)) {
                try {
                    clientHandler.bufferedWriter.write("Private message from " + clientUsername + ": " + message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(socket, bufferedWriter, bufferedReader);
                }
                return;
            }
        }
        try {
            bufferedWriter.write(recipient + " is not a valid recipient");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }



    public void notifyNewMember(String newMemberUsername) {
        // Notify all members that a new member has joined the group
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write( clientUsername + " has joined the group");
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }
    }




    private synchronized void notifyQuit(ClientHandler quittingClient) {
        System.out.println(quittingClient.clientUsername + " has quit.");

        // remove the quitting client from the list of active clients
        clientHandlers.remove(quittingClient);

        // broadcast a message to all clients informing them of the quitting client
        broadcastMessage(quittingClient.clientUsername + " has quit the chat.");

        try {
            quittingClient.socket.close();
            quittingClient.bufferedWriter.close();
            quittingClient.bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient.equalsIgnoreCase("quit")) {
                    notifyQuit(this); // call notifyQuit method passing the reference to the quitting client
                    break;
                }
                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                System.err.println(clientUsername + " disconnected");
                closeEverything(socket, bufferedWriter, bufferedReader);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
        if (this == coordinator) {
            if (!clientHandlers.isEmpty()) {
                // if coordinator left, choose the next client as the new coordinator
                coordinator = clientHandlers.get(0);
                System.out.println("New coordinator: " + coordinator.clientUsername);
            } else {
                coordinator = null;
            }
        }
        broadcastMessage("Serving: " + clientUsername + " has left the chat");

    }

    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
