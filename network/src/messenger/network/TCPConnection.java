package messenger.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/* Класс основного соединения */
public class TCPConnection {
    private final Socket socket; // сокет, связанный с соединением
    private final Thread rxThread; // поток, слушающий входящие соединения (на каждом клиенте – свой поток)
    private final TCPConnectionListener eventListener; // слушатель событий
    private final BufferedReader in;
    private final BufferedWriter out;

    // Сокет создается внутри конструктора
    public TCPConnection(TCPConnectionListener eventListener, String ipAddress, int port) throws IOException {
        this(eventListener, new Socket(ipAddress, port)); // вызываем уже созданный ниже конструктор
    }

    // На вход принимает готовый объект сокета, с этим сокетом создает готовое соединение внутри
    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        // создаем анонимный класс
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this); // передали экземпляр обрамляющего класса
                    while (!rxThread.isInterrupted()) {
                        String str = in.readLine();
                        if (str == null)
                            return;
                        eventListener.onReceiveString(TCPConnection.this, str);
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    // Метод отправки строки
    public synchronized void sendString(String value) {
        try {
                out.write(value + "\r\n");
                out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect(); // раз строка не передалась, значит, с соединением что-то не то
        }
    }

    public synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    @Override
    public String toString() {
        return "TCP Connection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
