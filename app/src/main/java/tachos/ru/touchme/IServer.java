package tachos.ru.touchme;

public interface IServer {
    void clientReceived(String name);

    void connectionSuccessful();

    void connectionFailed();

    void disconnected();

    void paired();

    void coordsReceived(int x, int y);
}
