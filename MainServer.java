import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    private static final int SERVER_PORT = 11122;
    private static final int SERVER_PORT_PING = 11123;

    private static Thread pairingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                while (true) {
                    Socket client1 = serverSocket.accept();
                    System.out.println("Client connected 1");
                    DataInputStream in1 = new DataInputStream(client1.getInputStream());
                    final DataOutputStream out1 = new DataOutputStream(client1.getOutputStream());

                    Socket client2 = serverSocket.accept();
                    System.out.println("Client connected 2");
                    DataInputStream in2 = new DataInputStream(client2.getInputStream());
                    final DataOutputStream out2 = new DataOutputStream(client2.getOutputStream());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    int x = in1.readInt();
                                    int y = in1.readInt();
                                    out2.writeInt(x);
                                    out2.writeInt(y);
                                    out2.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    client1.close();
                                } catch (IOException e1) {
                                }
                            }
                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    int x = in2.readInt();
                                    int y = in2.readInt();
                                    out1.writeInt(x);
                                    out1.writeInt(y);
                                    out1.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    client2.close();
                                } catch (IOException e1) {
                                }
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });
    private static Thread pingPairingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(SERVER_PORT_PING);
                while (true) {
                    Socket client1 = serverSocket.accept();
                    System.out.println("Client connected 1");
                    DataInputStream in1 = new DataInputStream(client1.getInputStream());
                    final DataOutputStream out1 = new DataOutputStream(client1.getOutputStream());

                    Socket client2 = serverSocket.accept();
                    System.out.println("Client connected 2");
                    DataInputStream in2 = new DataInputStream(client2.getInputStream());
                    final DataOutputStream out2 = new DataOutputStream(client2.getOutputStream());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    long x = in1.readLong();
                                    out2.writeLong(x);
                                    out2.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    client1.close();
                                } catch (IOException e1) {
                                }
                            }
                        }
                    }).start();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    long x = in2.readLong();
                                    out1.writeLong(x);
                                    out1.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    client2.close();
                                } catch (IOException e1) {
                                }
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public static void main(String[] args) throws IOException {
        pairingThread.start();
        pingPairingThread.start();
    }
}