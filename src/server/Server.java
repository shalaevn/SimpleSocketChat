package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Created by shalaev nikolaj on 24.02.17.
 */
public class Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private final int PORT = 8189;
    private Vector<ClientHandler> clients = new Vector<>();

     public Server(){
         try {
             serverSocket = new ServerSocket(PORT);
             System.out.println("Сервер подключен. Ожидается подключение клиентов...");
             while(true){
                 socket = serverSocket.accept();
                 System.out.println("Подключился клиент");
                 new ClientHandler(this, socket);
             }
         } catch (IOException e){

         } finally {
             try {
                 socket.close();
                 serverSocket.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }

    void removeClient(ClientHandler client){
        clients.remove(client);
    }

    void addClient(ClientHandler client){
        clients.add(client);
    }

    void broadcastMSG(String s){
        for (ClientHandler client : clients){
            client.sendMSG(s);
        }
    }
}
