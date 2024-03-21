package messenger.network;

/* Во всех методах передаем на вход экземпляр самого соединения, чтобы у той сущности,
    которая реализует интерфейс, был доступ к источнику события */
public interface TCPConnectionListener {
    // Соединение готово
    void onConnectionReady(TCPConnection tcpConnection);

    // Соединение приняло входящую строку
    void onReceiveString(TCPConnection tcpConnection, String value);

    // Соединение прервалось
    void onDisconnect(TCPConnection tcpConnection);

    // Что-то пошло не так (словили исключение)
    void onException(TCPConnection tcpConnection, Exception e);
}
