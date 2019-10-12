package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:mainDB.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) throws SQLException {
        String sql = String.format("SELECT nickname FROM main where login = '%s' and password = '%s'",
                login, pass.hashCode());

        ResultSet rs = stmt.executeQuery(sql);

        if(rs.next()) {
            return rs.getString(1);
        }

        return null;
    }
//запись в базу данных строчки с новым пользователем
    public static void addNewUser(String nick, String login, String password) throws SQLException {
        String sql = String.format("INSERT INTO main (login, password, nickname) VALUES ('%s', '%s', '%s');",
                login, password.hashCode(), nick);

        stmt.execute(sql);
    }
//запись в базу черного листа
    public static void addToBlackList (String nick, String blackNick) throws SQLException{
        String sql1;
        String sql2;
        String sql3;
        int idUser=0;
        int idBlack=0;

        sql1 = String.format("SELECT id FROM main WHERE nickname = '%s';", nick);
        ResultSet rs1 = stmt.executeQuery(sql1);
        if(rs1.next()) {
            idUser = Integer.parseInt(rs1.getString(1));
        }
        sql2 = String.format("SELECT id FROM main WHERE nickname = '%s';", blackNick);
        ResultSet rs2 = stmt.executeQuery(sql2);
        if(rs2.next()) {
            idBlack = Integer.parseInt(rs2.getString(1));
        }
        sql3 = String.format("INSERT INTO BlackList (id_user, id_black) VALUES ('%s', '%s');",
                idUser, idBlack);
        stmt.execute(sql3);
    }

    //получение черного списка для клиента
    public static ArrayList<String> blackListFromDB(ClientsHandler user) throws SQLException {
        String nick = user.getNick();
        String sql = String.format("SELECT * FROM BlackList WHERE id_user = '%s';", nick);
        ArrayList<String> blackList = new ArrayList<>();
        ResultSet rs = stmt.executeQuery(sql);
        int i=0;

        while(rs.next()) {
            System.out.println(rs.getString(1));
            blackList.add(rs.getString(1));
            i++;
        }
        return blackList;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
