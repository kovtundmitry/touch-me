package tachos.ru.touchme;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    PrintWriter out;
    BufferedReader in;
    String ip = "185.26.120.93";
    int port = 11122;
    String name = "";
    private boolean isPaired = false;
    private IServer listener;
    Thread connThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Socket socket = new Socket(ip, port);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                listener.connectionSuccessful();
            } catch (IOException e) {
                listener.connectionFailed();
                e.printStackTrace();
                return;
            }
            try {
                auth(name);
                while (!connThread.isInterrupted()) {
                    if (isPaired) {
                        int x = Integer.valueOf(in.readLine());
                        int y = Integer.valueOf(in.readLine());
                        Log.d("Incoming coords: ", "" + x + " " + y);
                        commandXYReceived(x, y);
                        continue;
                    }
                    int command = in.read();
                    command = Character.getNumericValue(command);
                    Log.d("Incoming command: ", "" + command);
                    if (command == -1) break;
                    switch (command) {
                        case ServerCommands.COMMAND_QUEUED_NAME: {
                            commandQueuedDeviceNameReceived();
                            break;
                        }
                        case ServerCommands.COMMAND_PAIRED_SUCCESSFULLY: {
                            commandPairedSuccessfully();
                            break;
                        }
                        case ServerCommands.COMMAND_PAIRING_FAILED: {
                            commandPairingFailed();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                listener.connectionFailed();
                e.printStackTrace();
            }
        }
    });

    public ServerConnection(String name, IServer listener) {
        connect(name);
        this.listener = listener;
    }

    public void sendCoords(int x, int y) {
        out.println(x);
        out.println(y);
        out.flush();
    }

    public boolean isPaired() {
        return isPaired;
    }

    private void commandXYReceived(int x, int y) {
        listener.coordsReceived(x, y);
    }

    private void commandPairingFailed() {

    }

    private void commandPairedSuccessfully() {
        isPaired = true;
        listener.paired();
    }

    public void enterQueue() {
        if (out != null) {
            out.print(ServerCommands.COMMAND_ADD_TO_QUEUE);
            out.flush();
        }
    }

    public void getQueue() {
        if (out != null) {
            out.print(ServerCommands.COMMAND_REQUEST_QUEUE);
            out.flush();
        }
    }

    public void pairTo(String name) {
        out.print(ServerCommands.COMMAND_PAIR);
        out.println(name);
        out.flush();
    }

    private void commandQueuedDeviceNameReceived() throws IOException {
        String name = in.readLine();
        Log.d("queue", name);
        listener.clientReceived(name);
    }

    private void connect(String name) {
        this.name = name;
        connThread.start();
    }

    private void auth(String name) throws IOException {
        if (out != null) {
            out.print(ServerCommands.COMMAND_AUTH);
            out.println(name);
            out.flush();
        }
    }
}
