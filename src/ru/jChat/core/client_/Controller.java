package ru.jChat.core.client_;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextArea textArea;
    @FXML
    TextField msgField;
    @FXML
    HBox authPanel;
    @FXML
    HBox msgPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passField;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    final String SERVER_IP = "localhost"; //127.0.0.1
    final int SERVER_PORT = 8189;

    private boolean authorized;

    public void setAuthorized(Boolean authorized) {
        this.authorized = authorized;
        if (authorized) {
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            authPanel.setVisible(false);
            authPanel.setManaged(false);
        } else {
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            authPanel.setVisible(true);
            authPanel.setManaged(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
    }

    public void sendAuthMsg() { //Сообщение серверу при авторизации
        if (socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            showAlert("Не удалось подключиться к серверу. Проверьте соединение");
        }
    }

    public void connect(){
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread thread = new Thread(() -> {
                try {
                    while (true) {  //Цикл авторизации, ожидание инфы от сервера
                        String s = null;
                        s = in.readUTF();
                        if (s.equals("/authok")){
                            textArea.clear();
                            setAuthorized(true);
                            break;
                        }
                        textArea.appendText(s + "\n");
                    }
                    while (true) {
                        String s = null;
                        s = in.readUTF();
                        textArea.appendText(s + "\n");
                    }
                } catch (IOException e) {
                    showAlert("Сервер перестал отвечать. Обратитесь к администратору.");
                } finally {
                    setAuthorized(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            showAlert("Не удалось подключиться к серверу. Проверьте соединение");
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            showAlert("Не удалось подключиться к серверу. Проверьте соединение");
        }
    }

    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Возникли проблемы");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
