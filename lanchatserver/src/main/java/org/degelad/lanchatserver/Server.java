package org.degelad.lanchatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

/**
 *
 * @author degelad
 */
public class Server {

    private Vector<ClientHandler> clients;

    public Server() throws SQLException {

        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
//добавляем пока что руками пользователей в таблицу бд
//            AuthService.addUser("login1", "pass1", "nick1");
//            AuthService.addUser("login2", "pass2", "nick2");
//            AuthService.addUser("login3", "pass3", "nick3");

            server = new ServerSocket(8189);
            System.out.println("Сервер запущен");
            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
//теперь не добавляем сразу клиента в список, а используем для этого отдельный метод subscribe и то после прохождения авторизации в классе ClientHandler
                new ClientHandler(this, socket);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {//Метод отправки личного сообщения от пользователя к пользователю
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nickTo)) {
                o.sendMsg("from " + from.getNick() + ":" + msg);
                from.sendMsg("to " + nickTo + ":" + msg);
                return;
            }
        }
        from.sendMsg(nickTo + " не найден в чате");
    }

    public void broadcastClientList() {                                         //отправка клиенту списка пользователей чата, клиент добавит у себя в правую часть чата
        StringBuilder sb = new StringBuilder();
        sb.append("/clientslist ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }
        String out = sb.toString();
        for (ClientHandler o : clients) {
            o.sendMsg(out);
        }
    }

    public boolean isNickBusy(String nick) {                                    //Метод проверки занят ли такой никнейм
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastMsg(ClientHandler from, String msg) {                                      //Метод отправки сообщений сервером
        for (ClientHandler o : clients) {
            if (!o.checkBlackList(from.getNick())) {                            //перед отправкой сообщений, проверяем пользователей на бан
                o.sendMsg(msg);
            }
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientList();
    }
}
