package server;

import java.sql.SQLException;

public class ServerStart {
    public static void main(String[] args) {
        try {
            new ServerChat();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
