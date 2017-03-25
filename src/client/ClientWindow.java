package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shalaev nikolaj on 24.02.17.
 */
public class ClientWindow extends JFrame{
    private String login;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private JTextArea chatMessages;
    private JPanel inputPanel;
    private JTextField inputMessageField;
    private JButton sendButton;
    private JPanel authPanel;
    private JTextField loginField;
    private JPasswordField pass;
    private JButton loginButton;
    private boolean isExit = false;
    private boolean isAuth = true;
    private String name;

    public ClientWindow(){
        try {
            Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
            setTitle("CHAT WINDOW");
            int width = screenDimension.width;
            int height = screenDimension.height;
            setBounds(width/3, 0, (width * 26)/100, (height * 6)/10);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            chatMessages = new JTextArea();
            chatMessages.setLineWrap(true);
            chatMessages.setFocusable(false);
            chatMessages.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(chatMessages);
            scrollPane.setAutoscrolls(true);
            add(scrollPane, BorderLayout.CENTER);
            inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
            inputPanel.setMaximumSize(new Dimension(width, 20));
            inputMessageField = new JTextField();
            inputMessageField.setMaximumSize(new Dimension(width, 30));
            sendButton = new JButton("SEND");
            inputPanel.add(inputMessageField);
            inputPanel.add(sendButton);
            add(inputPanel, BorderLayout.SOUTH);
            authPanel = new JPanel(new GridLayout(1, 3));
            loginField = new JTextField();
            pass = new JPasswordField();
            loginButton = new JButton("LOGIN");
            authPanel.add(loginField);
            authPanel.add(pass);
            authPanel.add(loginButton);
            add(authPanel, BorderLayout.NORTH);
            setAuth(false);

            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            loginField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendLogin();
                }
            });

            pass.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendLogin();
                }
            });

            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendLogin();
                }
            });

            inputMessageField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage();
                }
            });

            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage();
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String broadcastMessage;
                    try {
                        auth();
                        while(!isExit) {
                            broadcastMessage = in.readUTF();
                            if(broadcastMessage.equals("/end")){
                                isExit = true;
                                chatMessages.append("Вы отключены от чата");
                            } else if(broadcastMessage.equals("/relog")){
                                setAuth(false);
                                auth();
                            } else chatMessages.append(broadcastMessage + "\n");
                            chatMessages.setCaretPosition(chatMessages.getDocument().getLength());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                inputMessageField.setText("/end");
                sendMessage();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        setVisible(true);
    }

    private void auth(){
        try {
            while (!isAuth) {
                String broadcastMessage = in.readUTF();
                if (broadcastMessage.equals("/authOK")) {
                    setAuth(true);
                    name = loginField.getText();
                } else chatMessages.append(broadcastMessage + "\n");
                chatMessages.setCaretPosition(chatMessages.getDocument().getLength());
            }
        }catch (IOException e){
        }
    }

    private void sendLogin(){
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + pass.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //дописать логику сохранения логина и пароля в файле серверной частью
    //доделать чат соответствуя последнему уроку
    public void setAuth(boolean auth){
        isAuth = auth;
        inputPanel.setVisible(isAuth);
        authPanel.setVisible(!isAuth);
    }

    private void sendMessage(){
        String message = inputMessageField.getText().trim();
        if(!message.equals("")){
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
            message = format.format(date) + " " + name + ": " + message;
            try {
                out.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inputMessageField.setText("");
        inputMessageField.grabFocus();
    }
}
