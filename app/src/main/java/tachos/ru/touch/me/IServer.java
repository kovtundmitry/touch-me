package tachos.ru.touch.me;

public interface IServer {

    void connectionSuccessful();

    void connectionFailed();

    void disconnected();

    void paired();

    void coordsReceived(int x, int y);
}
