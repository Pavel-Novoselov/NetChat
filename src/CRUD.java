import java.sql.*;
import java.time.LocalDateTime;

public class CRUD {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement pstmt;

    public static void main(String[] args) throws SQLException {
        try {
            connect();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        createT();
        dropT();
        readT();
        writeT();

        close();
    }

    public static void createT() throws SQLException {
        String sqlCr = "CREATE TABLE IF NOT EXISTS table0 (" +
                "    id     INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    groupe TEXT" +
                ");";
        stmt.execute(sqlCr);

    }

    public static void dropT() throws SQLException {
        String sqlDr = "DROP TABLE IF EXISTS table1;";
        stmt.execute(sqlDr);

    }

    public static void readT() throws SQLException {
        String sqlR = "SELECT students.name, st_doc.number FROM students JOIN st_doc, docs ON st_doc.id_doc = docs.id AND st_doc.id_student=students.id WHERE docs.title='pass';";
        ResultSet rs = stmt.executeQuery(sqlR);
        while (rs.next()) {
            System.out.println(rs.getString(1) + " " + rs.getInt("number"));
        }
    }

    public static void writeT() throws SQLException {
        String sqlW = "insert into st_doc (id_student, id_doc, date, number) Values (2010, 2, '10/10/10', 123459);";
        int i = stmt.executeUpdate(sqlW);
        System.out.println(i);

    }

    public static void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    public static void close() throws SQLException {
        connection.close();
    }
}


