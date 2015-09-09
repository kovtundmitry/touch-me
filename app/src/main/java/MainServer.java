import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class MainServer {
    private static final int SERVER_PORT = 11122;
    private static final ArrayList<ConnectedDevice> connectedDevices = new ArrayList<>();
    private static final ArrayList<ConnectedDevice> queuedDevices = new ArrayList<>();
    private static final IConnectedDevice iConnectedDeviceListener = new IConnectedDevice() {
        @Override
        public void onDisconnect(ConnectedDevice device) {
            connectedDevices.remove(device);
            queuedDevices.remove(device);
            System.out.println("Player disconnected: " + device.getName());
        }

        @Override
        public void addToQueue(ConnectedDevice device) {
            queuedDevices.add(device);
        }

        @Override
        public void requestQueue(ConnectedDevice device) {
            for (ConnectedDevice queuedDevice : queuedDevices) {
                device.sendDeviceName(queuedDevice.getName());
            }
        }

        @Override
        public void requestPair(ConnectedDevice device, String pairName) {
            for (ConnectedDevice queuedDevice : queuedDevices) {
                if (pairName.equals(queuedDevice.getName())) {
                    queuedDevices.remove(queuedDevice);
                    queuedDevice.setPair(device);
                    device.setPair(queuedDevice);
                    break;
                }
            }
        }

    };
    private static Thread connectionsThread = new Thread(new Runnable() {
        @Override
        public void run() {
            connectedDevices.clear();
            queuedDevices.clear();
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                while (!connectionsThread.isInterrupted()) {
                    connectedDevices.add(new ConnectedDevice(serverSocket.accept(), iConnectedDeviceListener));
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

    public static void main(String[] args) throws IOException {
        connectionsThread.start();
    }
}