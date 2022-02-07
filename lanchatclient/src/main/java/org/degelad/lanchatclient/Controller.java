package org.degelad.lanchatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    ListView<String> clientList;

// 5 создаем сокет и входящий и исходящий потоки
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

//создаем адрес сервера
    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;

    boolean isAuthohorized;

    public void setAuthohorized(boolean isAuthohorized) {
        this.isAuthohorized = isAuthohorized;

        if (!isAuthohorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            clientList.setVisible(false);
            clientList.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientList.setVisible(true);
            clientList.setManaged(true);
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
                            break;
                        } else {
                            chatArea.appendText(str + "\n");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {                              //проверяем сообщения служебные ли они
                            if (str.equals("/serverClosed")) {                      //условие для отключения клиента
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
                }
            }).start();
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
            loginfield.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
