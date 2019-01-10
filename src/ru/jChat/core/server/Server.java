package ru.jChat.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class Server {

    private Vector<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            clients = new Vector<>();
            authService = new AuthService();
            authService.connect();
            System.out.println("Server started...waiting for clients...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected" + socket.getInetAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Не удалось запустить сервис авторизации");
        }finally {
            authService.disconnect();
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public boolean isNikBusy(String nik){
        for(ClientHandler o: clients){
            if (nik.equals(o.getNik()))
                return true;
        }
        return false;
    }

    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }
}
