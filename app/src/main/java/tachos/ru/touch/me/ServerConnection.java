package tachos.ru.touch.me;

import android.support.annotation.Nullable;
import android.util.Log;

import com.backendless.Backendless;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    private static ServerConnection instance = null;
    PrintWriter out;
    BufferedReader in;
    String ip = "185.26.120.93";
    int port = 11122;
    String id = "";
    String partnerId = "";
    Socket socket;
    private boolean isPaired = false;
    private IServer listener;
    Thread connThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                listener.connectionSuccessful();
            } catch (IOException e) {
                listener.connectionFailed();
                e.printStackTrace();
                return;
            }
            try {
                auth(id);
                pairTo(partnerId);
                while (!connThread.isInterrupted()) {
                    if (isPaired) {
                        int x = Integer.valueOf(in.readLine());
                        int y = Integer.valueOf(in.readLine());
                        if (y == -20 && x != -20 && x != -121)
                            x = y = Integer.valueOf(in.readLine());
                        Log.d("Incoming coords: ", "" + x + " " + y);
                        commandXYReceived(x, y);
                        continue;
                    }
                    int command = in.read();
                    command = Character.getNumericValue(command);
                    Log.d("Incoming command: ", "" + command);
                    if (command == -1) break;
                    switch (command) {
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

    public ServerConnection(IServer listener, String partnerId) {
        instance = this;
        this.partnerId = partnerId;
        this.listener = listener;
        connect(Backendless.UserService.CurrentUser().getUserId());
    }
@Nullable
    public static ServerConnection getInstance() {
        return instance;
    }

    public void sendCoords(int x, int y) {
        out.println(x);
        out.println(y);
        out.flush();
    }

    public void disconnect() {
        if (socket != null) try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance = null;
        connThread.interrupt();
        connThread = null;
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

    private void connect(String id) {
        this.id = id;
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
