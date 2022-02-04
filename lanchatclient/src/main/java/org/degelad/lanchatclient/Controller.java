package org.degelad.lanchatclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.TextArea;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class Controller implements Initializable {

// 5 создаем сокет и входящий и исходящий потоки
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

//создаем адрес сервера
    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            try {
//инициируем подключение
                socket = new Socket(IP_ADDRESS, PORT);
//инициируем обработчики потоков
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
// в бесконечном цикле слушаем сервер используем поток
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();
//условие для отключения клиента
                            if (str.equals("/serverClosed")) {
                                break;
                            }
                            textArea.appendText(str + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendMsg() {
        try {
            // 10 отправляем сообщение на сервер
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
