package org.degelad.lanchatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author degelad
 */
public class ClientHandler {

    private DataOutputStream out;
    private DataInputStream in;
    String nick;
    List<String> blackList;                                                     //лист в который помещаем пользователя в бан

    public ClientHandler(Server server, Socket socket) {                        //конструктор для создания экземпляра класса, то есть подключения очередного клиента
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.blackList = new ArrayList<>();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {                                          //цикл отвечающий за авторизация клиента
                            String str = in.readUTF();
                            if (str.startsWith("/auth")) {                      //Если от клиента поступит /auth значит он хочет авторизоваться
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickLoginAndPass(tokens[1], tokens[2]);
                                if (newNick != null) {                          //Проверяем не пустой ли ник и не в сети ли он, если авторизация успешна, отправляем сообщение пользователю /authok и подписывем его в список клиентов
                                    if (!server.isNickBusy(newNick)) {
                                        sendMsg("/authok");
                                        nick = newNick;
                                        server.subscribe(ClientHandler.this);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись уже используется");
                                    }
                                } else {
                                    sendMsg("Неверный логин/пароль");
                                }
                            }

                            try {

                                if (str.startsWith("/regus")) {                      //Если от клиента поступит /regus значит он хочет зарегестрироваться
                                    String[] tokens = str.split(" ");
                                    String newLogin = AuthService.getLogin(tokens[1]);
                                    Boolean newNick = AuthService.getNick(tokens[2]);
                                    Boolean newPass = AuthService.getPass(tokens[3]);
                                    if (newLogin != null) {
                                        sendMsg("Такой логин уже используется");
                                        break;

                                    } else if (newNick) {
                                        sendMsg("Такой ник-нейм уже используется");
                                        break;
                                    } else if (newPass != true) {
                                        sendMsg("Введите пароль");
                                        break;
                                    } else {
                                        AuthService.addUser(tokens[1], tokens[3], tokens[2]);
                                        sendMsg("Вы зарегестрированы, пройдите авторизацию");
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendMsg("Заполните все поля");
                            }
                        }

                        while (true) {                                          //цикл ждет служебных сообщений от пользователя
                            String str = in.readUTF();
                            if (str.startsWith("/")) {                          //проверяем сообщения служебные ли они
                                if (str.startsWith("/end")) {                   //отсоединение клиента от сервера, например для перелогина /end
                                    out.writeUTF("/severclosed");
                                    break;
                                }
                                if (str.startsWith("/w")) {                     //отправка сообщений в личку указанному пользователю командой /w [nickname] [message]
                                    String[] tokens = str.split(" ", 3);        //сплитим пробелами поступившее сообщение на три части, 1 это /w, вторая ник, третья само сообщение
                                    server.sendPersonalMsg(ClientHandler.this, tokens[1], tokens[2]);//отправляем сообщение 
                                }
                                if (str.startsWith("/blacklist")) {                     //добавление в черный список указанного пользователя /blacklist [nickname] переделать с использованием не ArrayList а базы данных
                                    String[] tokens = str.split(" ");
                                    blackList.add(tokens[1]);
                                    sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                                }

                                if (str.startsWith("/renick")) {                     //смена ник-нейм /renick [new nickname]
                                    String[] tokens = str.split(" ");
                                    String oldnick = ClientHandler.this.getNick();
                                    if (!AuthService.getNick(tokens[1])) {
                                    AuthService.getRenameNick(oldnick, tokens[1]);
                                        server.broadcastMsg(ClientHandler.this, oldnick + " сменил ник-нейм на " + tokens[1]);
                                        sendMsg("После смены ник-нейма, необходимо пройти авторизацию");
                                        out.writeUTF("/severclosed");
                                        break;
                                    } else {
                                        sendMsg("Такой ник-нейм уже занят");
                                    }
                                }
                            } else {
                                server.broadcastMsg(ClientHandler.this, nick + " " + str);
                            }
                            System.out.println("Client " + str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        server.unsubscribe(ClientHandler.this);                 //удалениe клиента из списка рассылки broadcastMsg (из Vector<ClientHandler> clients)
                    }

                }

            }
            ).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean checkBlackList(String nick) {                                //метод проверяет содержится ли данный никнейм в blackList
        return blackList.contains(nick);
    }

    public String getNick() {                                                   //геттер отдачи никнейма
        return nick;
    }

    public void sendMsg(String msg) {                                           //метод записи сообщения в исходящий поток

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
