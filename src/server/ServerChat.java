package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class ServerChat {
    private Vector<ClientsHandler> clients;

    public ServerChat() throws SQLException {
        clients=new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try{
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Server started");
            while (true){
                socket = server.accept();
                System.out.println("Client connected");
                new ClientsHandler(this, socket);
            }
        }catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void broadcastMsg(ClientsHandler from, String msg){
        for (ClientsHandler o: clients) {
            if (!from.checkBlackList(o.getNick()))
            o.sendMsg(msg);
        }
    }
//приватная переписка
    public void privatMsg(String nickname, String msg){
        for (ClientsHandler o: clients) {
            String nick = o.getNick();
            if(nick.equals(nickname)){
                o.sendMsg(msg);
            }
        }
    }
//проверка уникален ли НИК
    public boolean isNickUnic (String nickname){
        for (ClientsHandler o: clients) {
          //  String nick = o.getNick();
            if(o.getNick().equals(nickname)){
               return false;
            }
        }
        return true;
    }

    public void broadcastClientsList(){
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist");
        for (ClientsHandler o: clients) {
            sb.append(o.getNick()+" ");
        }
        String out = sb.toString();
        for (ClientsHandler o: clients) {
            o.sendMsg(out);
        }
    }

    public void subscribe(ClientsHandler client){
        clients.add(client);
       broadcastClientsList();
    }

    public void unsubscribe(ClientsHandler client){
        clients.remove(client);
        broadcastClientsList();
    }
}
