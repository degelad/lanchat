package org.degelad.lanchatserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;

/**
 *
 * @author degelad
 */
public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:lanchatdb.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
//метод добавления пользователей в таблицу бд

    public static void addUser(String login, String pass, String nick) {

        try {
            String query = "insert into userTable (login, passwd, nickname) values (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setInt(2, pass.hashCode());
            ps.setString(3, nick);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
//метод проверки наличия пользователя в базе по нику и хеш пароля.

    public static String getNickLoginAndPass(String login, String pass) {

        try {
            ResultSet rs = stmt.executeQuery("select nickname, passwd from userTable where login = '" + login + "'");
            int myHash = pass.hashCode();
            if (rs.next()) {
                String nick = rs.getString(1);
                int dbhash = rs.getInt(2);
                if (myHash == dbhash) {
                    return nick;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void getRenameNick(String oldnick, String newnick) {

        try {
                stmt.executeQuery("update userTable set nickname='" + newnick + "' where nickname = '" + oldnick + "'");
        } catch (SQLException e) {
            e.printStackTrace();
    }
}

//метод проверки наличия пользователя в базе по логину.
    public static String getLogin(String login) {

        try {
            ResultSet rs = stmt.executeQuery("select login from userTable where login = '" + login + "'");
            if (rs.next()) {
                String dblogin = rs.getString(1);
                if (login.equalsIgnoreCase(dblogin)) {
                    return dblogin;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
//метод проверки наличия пользователя в базе по ник-нейм.

    public static boolean getNick(String nick) {

        try {
            ResultSet rs = stmt.executeQuery("select nickname from userTable where nickname = '" + nick + "'");
            if (rs.next()) {
                String dbnick = rs.getString(1);
                if (nick.equalsIgnoreCase(dbnick)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
//метод проверки пароля пользователя при регистрации, пока что только проверяет есть ли пароль или пустой

    public static boolean getPass(String pass) {

        if (pass != null) {
            return true;
        }
        return false;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
