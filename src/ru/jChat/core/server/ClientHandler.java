package ru.jChat.core.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nik;

    public String getNik() {
        return nik;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/auth")) {
                            String[] data = msg.split("\\s");
                            String newNik = server.getAuthService().getNikByLoginAndPass(data[1], data[2]);
                            if (newNik != null) {
                                if (!server.isNikBusy(newNik)) {
                                    nik = newNik;
                                    sendMsg("/authok");
                                    server.subscribe(this);
                                    break;
                                }else {
                                    sendMsg("Учетная запись занята");
                                }
                            } else sendMsg("Неверный логин/пароль");
                        }
                    }

                    while (true) {
                        String msg = in.readUTF();
                        System.out.println(nik + ": " + msg);
                        if (msg.equals("/end")) break;
                        server.broadcastMsg(nik + ": " + msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    nik = null;
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
