package ru.jChat.core.server;

import java.sql.*;

public class AuthService {
    private Connection connection;
    private Statement stmt;

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC"); //Загрузка драйвера подключения к БД
        connection = DriverManager.getConnection("jdbc:sqlite:MainDB.db");
        stmt = connection.createStatement();
    }

    public String getNikByLoginAndPass(String login, String pass){
        try {
            ResultSet rs = stmt.executeQuery("SELECT nik from users WHERE login = '" + login + "' AND password = '" + pass + "';"); //Запрос на чтение данных
            if (rs.next()){
                return rs.getString(1); //Вмепсто 1 можно указать "nik" - название столбца
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void disconnect(){
        try {
            stmt.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
