package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by shalaev nikolaj on 24.02.17.
 */
public class ClientHandler {
    Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isExit = false;
    private boolean isAuth = false;
    private String name;

    public ClientHandler(Server server, Socket socket){
        try {
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        auth();
                        while(!isExit) {
                            String msg = in.readUTF();
                            String[] partsMSG = msg.split(" ");
                            switch(partsMSG[2]){
                                case "/end":
                                    sendMSG(partsMSG[2]);
                                    isExit = true;
                                    break;
                                case "/relog":
                                    isAuth = false;
                                    out.writeUTF(partsMSG[2]);
                                    removeClient();
                                    auth();
                                    break;
                                case "/help":
                                    sendHelp();
                                    break;
                                default:
                                    server.broadcastMSG(msg);
                            }
                        }
                    } catch (IOException e){
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMSG(String s){
        if(s.equals("/end"))
            server.removeClient(this);
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void auth(){
        try {
            while (!isAuth) {
                String str = in.readUTF();
                String[] partsAuth = str.split(" ");
                if (str.startsWith("/auth")) {
                    if (!server.isConnected(partsAuth[1])) {
                        if (partsAuth.length == 3 && server.authService.setLogin(partsAuth[1], partsAuth[2])) {
                            name = partsAuth[1];
                            out.writeUTF("/authOK");
                            isAuth = true;
                        } else out.writeUTF("Неправильная пара логин/пароль");
                    } else out.writeUTF("Пользователь с таким именем уже подключен");
                } else if(str.startsWith("/reg")) {
                    if (partsAuth.length == 3 && !server.authService.findUser(partsAuth[1])){
                        server.authService.registration(partsAuth[1], partsAuth[2]);
                        out.writeUTF("/regOK");
                        name = partsAuth[1];
                        isAuth = true;
                    } else out.writeUTF("Пользователь с таким логином уже зарегистрирован");
                }
            }
            server.addClient(this);
            server.broadcastMSG("Подключился " + name);
        }catch (IOException e){

        }
    }

    String getName(){
        return name;
    }

    private void removeClient(){
        server.removeClient(this);
    }

    private void sendHelp(){
        String help = "/end - покинуть чат\n/relog - сменить пользователя";
        try {
            out.writeUTF(help);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
