package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;


public class ClientsHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ServerChat serv;
    private String nick;

    public ClientsHandler(ServerChat serv, Socket socket) {
        try {
            this.socket = socket;
            this.serv = serv;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/reg")){
                                String[] regTokens = str.split(" ");
                                AuthService.addNewUser(regTokens[1], regTokens[2], regTokens[3]);
                                sendMsg("/regok");
                            }
                            if (str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String currentNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                if (currentNick == null)
                                    sendMsg("неверный логин/пароль");
                                else if (!serv.isNickUnic(currentNick))
                                    sendMsg("пользователь с такимл логином уже существует");
                                    else {
                                    sendMsg("/authok");
                                    nick = currentNick;
                                    serv.subscribe(ClientsHandler.this);
                                    System.out.println("Auth OK ");
                                    break;
                                }
                            }
                        }
                        while (true){
                            String str = in.readUTF();
                            if (str.equalsIgnoreCase("/end")) {
                                sendMsg("/clientClose");
                                break;
                            }
                            if (str.startsWith("/w")){
                                String[] tokens = str.split(" ");
                                serv.privatMsg(tokens[1], "Privat message from "+ClientsHandler.this.getNick()+": "+tokens[2]);
                            } else
                                serv.broadcastMsg(nick + ": " + str);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (IOException e){
                        e.printStackTrace();
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        serv.unsubscribe(ClientsHandler.this);
                    }
                }
            }).start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg){
        try{
            out.writeUTF(msg);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}