package Server;

import com.sun.xml.internal.ws.server.ServerRtException;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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
//запись черного листа пользователя в базу
    public static void addToBlackList (String nick, String blackNick) throws SQLException{
        String sql1;
        String sql2;
        String sql3;
        int idUser=0;
        int idBlack=0;
//получаем ID по нику юзера
        sql1 = String.format("SELECT id FROM main WHERE nickname = '%s';", nick);
        ResultSet rs1 = stmt.executeQuery(sql1);
        if(rs1.next()) {
            idUser = Integer.parseInt(rs1.getString(1));
        }
//получаем ID по нику блокированного юзера
        sql2 = String.format("SELECT id FROM main WHERE nickname = '%s';", blackNick);
        ResultSet rs2 = stmt.executeQuery(sql2);
        if(rs2.next()) {
            idBlack = Integer.parseInt(rs2.getString(1));
        }
        //добавляем запись в базу с ID юзера и блокированного
        sql3 = String.format("INSERT INTO BlackList (id_user, id_black) VALUES ('%s', '%s');",
                idUser, idBlack);
        stmt.execute(sql3);
    }

//получение своего черного списка при авторизации клиента
    public static ArrayList<String> blackListFromDB(ClientsHandler user) throws SQLException {
        ArrayList<String> blackList = new ArrayList<>();
        ArrayList<Integer> blackID = new ArrayList<>();
        String nick = user.getNick();
        //получаем userID по nick
        String sqlIdFromNick = String.format("SELECT id FROM main WHERE nickname = '%s';", nick);
        ResultSet rsUserId = stmt.executeQuery(sqlIdFromNick);
        int idUser=-1;
        if(rsUserId.next()) {
            idUser = rsUserId.getInt(1);
//            System.out.println("Start black list of "+nick + " "+idUser);
        } else System.out.println("Error userID");
        //получаем id тех кто в блэк листе для данного user'a
        if (idUser!=-1) {
            String sqlBlackIds = String.format("SELECT BlackList.id_black FROM main JOIN BlackList ON BlackList.id_user = main.id WHERE main.id='%d';", idUser);
            ResultSet rsBlackID = stmt.executeQuery(sqlBlackIds);
            while(rsBlackID.next()) {
                int idBlack = rsBlackID.getInt(1);
//                System.out.println("- "+idBlack);
                blackID.add(idBlack);
            }
        }
        //наконец получаем ники всех из блэк-листа по ID
        for (Integer id: blackID) {
            String sqlNickFromID = String.format("SELECT nickname FROM main WHERE id='%d';", id);
            ResultSet rsNickBlack = stmt.executeQuery(sqlNickFromID);
            while (rsNickBlack.next()){
                blackList.add(rsNickBlack.getString(1));
 //               System.out.println(rsNickBlack.getString(1));
            }
        }
        return blackList;
    }
//сохранение всех подряд сообщений в базу
    public static void saveMsg(LocalDateTime data, String nick, String msg) throws SQLException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = data.format(formatter);

        String sqlW="INSERT INTO history (date, fromNick, msg) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(sqlW);
        pstmt.setString(1, formattedDateTime);
        pstmt.setString(2, nick);
        pstmt.setString(3, msg);
        pstmt.executeUpdate();
    }
//получение истории сообщений из базы (передаем через TreeSet)
    public static TreeSet<String> getHistory() throws SQLException {
        TreeSet<String> history = new TreeSet<>();
        String str;
        String sqlS="SELECT date, fromNick, msg FROM history";
        ResultSet rs = stmt.executeQuery(sqlS);
        while(rs.next()){
            str=(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3));
            history.add(str);
        }
        return history;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
