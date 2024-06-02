import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;


    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        }catch(IOException n){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage(){
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String messagetosend = scanner.nextLine();
                bufferedWriter.write(username + ": "+ messagetosend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch (IOException n){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    public void listentomessage(){
        new Thread(() -> {
            String mssagefromGc;

            while (socket.isConnected()){
                try{
                    mssagefromGc = bufferedReader.readLine();
                    System.out.println(mssagefromGc);
                }catch (IOException n){
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        }catch (IOException n){
            n.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        // Validate username
        String username;
        while (true) {
            System.out.println("Enter username for the gc (letters only): ");
            username = scanner.nextLine();
            if (username.matches("[a-zA-Z]+")) {
                break;
            } else {
                System.out.println("Invalid username. Username must contain only letters.");
            }
        }

        Socket socket = new Socket("Localhost", 1234);
        Client client = new Client(socket, username);
        client.listentomessage();
        client.sendMessage();
    }
}