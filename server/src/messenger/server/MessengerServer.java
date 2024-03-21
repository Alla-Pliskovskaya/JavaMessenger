package messenger.server;

import messenger.network.TCPConnection;
import messenger.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/* Сервер принимает входящие соединения,
держит несколько активных входящих соединений,
рассылает сообщения. Сервер принимает сообщение от клиента, рассылает его остальным клиентам */

public class MessengerServer implements TCPConnectionListener {
    // создаем экземпляр сервера, вызывается конструктор ниже, сервер начинает работать
    public static void main(String[] args) {
        new MessengerServer();
    }
    // список соединений
    private final ArrayList<TCPConnection> connections = new ArrayList<>();

    // простой сервер
    private MessengerServer() {
        System.out.println("Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) { // ловим исключения при подключении клиентов
                    System.out.println("TCP Connection exception: " + e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // что-то идет не так
        }
    }

    // соединение готово
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        // при сложении объекта со строкой у него неявно вызывается метод toString, который мы переопределили
        sendToAllConnections("Client connected. " + tcpConnection);
    }

    // приняли строку
    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    // соединение прервалось
    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections("Client disconnected. " + tcpConnection);
    }

    // словили исключение – печатаем в консоль
    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCP Connection: " + e);
    }

    // всем соединениям отправляем строку
    private void sendToAllConnections(String value) {
        System.out.println(value);
        for (TCPConnection connection : connections) {
            connection.sendString(value);
        }
    }
}
