import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class MainServer {
    private static final int SERVER_PORT = 11122;
    private static final HashMap<String, ConnectedDevice> awaitingPlayers = new HashMap<>();

    private static Thread connectionsThread = new Thread(new Runnable() {
        @Override
        public void run() {
            awaitingPlayers.clear();
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                while (!connectionsThread.isInterrupted()) {
                    ConnectedDevice player = new ConnectedDevice(serverSocket.accept());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public static void playerConnected(ConnectedDevice player) {
        awaitingPlayers.put(player.getId(), player);
    }

    public static void tryToPair(ConnectedDevice player, String idToPair) {
        ConnectedDevice player2 = awaitingPlayers.get(idToPair);
        if (player2 == null) return;
        awaitingPlayers.remove(player);
        awaitingPlayers.remove(player2);
        player.setPair(player2);
        player2.setPair(player);
    }

    public static void playerDisconnected(ConnectedDevice player) {
        awaitingPlayers.remove(player.getId());
    }

    public static void main(String[] args) throws IOException {
        connectionsThread.start();
    }
}