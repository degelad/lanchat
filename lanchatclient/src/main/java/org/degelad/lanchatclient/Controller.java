package org.degelad.lanchatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class Controller {

    @FXML
    TextArea chatArea;

    @FXML
    TextField msgField;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginfield;

    @FXML
    PasswordField passwordField;

    @FXML
    HBox upperPanelReg;

    @FXML
    TextField loginfieldreg;

    @FXML
    TextField nicknamefieldreg;

    @FXML
    PasswordField passwordFieldreg;

    @FXML
    ListView<String> clientList;

// 5 создаем сокет и входящий и исходящий потоки
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    FileOutputStream outlog;
//создаем адрес сервера
    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;
    private String login;

    boolean isAuthohorized;
    boolean isHistory;

    public void setAuthohorized(boolean isAuthohorized) {
        this.isAuthohorized = isAuthohorized;
        createHistory();
        if (!isAuthohorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            upperPanelReg.setVisible(true);
            upperPanelReg.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            upperPanelReg.setVisible(false);
            upperPanelReg.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
            History.stop();
        }
    }

    public void connect() {
        try {
//инициируем подключение
            socket = new Socket(IP_ADDRESS, PORT);
//инициируем обработчики потоков
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
// в бесконечном цикле слушаем сервер используем поток
            setAuthohorized(false);
            new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/authok")) {
                            setAuthohorized(true);
                            chatArea.appendText(History.getLast100LinesOfHistory(login));
                            History.start(login);
                            break;
                        } else {
                            chatArea.appendText(str + "\n");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {                              //проверяем сообщения служебные ли они
                            if (str.equals("/serverClosed")) {
                                break;
                            }
                            if (str.startsWith("/clientslist ")) {               //Условие отправлять ли полученный список пользователей в правую часть чата
                                String[] tokens = str.split(" ");

                                Platform.runLater(new Runnable() {              //Отдельным потоком добавление списка пользователей в клиент лист правой части чата

                                    @Override
                                    public void run() {
                                        clientList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientList.getItems().add(tokens[i]);
                                        }
                                    }
                                });
                            }
                        } else {
                            chatArea.appendText(str + "\n");
                            History.writeLine(str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthohorized(false);
                    chatArea.clear();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createHistory() {
        File paperHistory = new File("history");
        try {
            paperHistory.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            //отправляем сообщение на сервер
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//метод авторизации

    public void tryToAuth() {
        connect();
        try {
            out.writeUTF("/auth " + loginfield.getText() + " " + passwordField.getText());
            login = loginfield.getText().trim();
            loginfield.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//метод регистрации пользователя

    public void tryToReg() {
        connect();
        try {
            out.writeUTF("/regus " + loginfieldreg.getText() + " " + nicknamefieldreg.getText() + " " + passwordFieldreg.getText());
            loginfieldreg.clear();
            nicknamefieldreg.clear();
            passwordFieldreg.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
