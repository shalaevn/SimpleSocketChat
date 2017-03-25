package server;

import java.sql.*;

/**
 * Created by Николай on 25.03.2017.
 */
public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement insert;
    private static PreparedStatement select;


    public static void connect(){
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:Users.db");
            insert = connection.prepareStatement("INSERT INTO User (Name, Pass) VALUES (?, ?);");
            select = connection.prepareStatement("SELECT Name FROM User WHERE Name = ? and Pass = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public  static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void registerNewUser(String name, String pass){
        try {
            insert.setString(1, name);
            insert.setString(2, pass);
            insert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean getAuth(String name, String pass){
        try {
            select.setString(1, name);
            select.setString(2, pass);
            ResultSet rs = select.executeQuery();
            if (rs.next()){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
