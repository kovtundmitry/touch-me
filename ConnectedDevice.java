import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedDevice {
    private Socket socket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;
    private String name = "";
    private IConnectedDevice listener;
    private Thread inputThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                System.out.println("Player streams opened");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                while (!inputThread.isInterrupted()) {
                    int command = Character.getNumericValue(inputStream.read());
                    System.out.println("Input command " + command + " from " + name);
                    if (command == -1) break;
                    switch (command) {
                        case ServerCommands.COMMAND_AUTH: {
                            commandAuth();
                            break;
                        }
                        case ServerCommands.COMMAND_ADD_TO_QUEUE: {
                            commandAddToQueue();
                            break;
                        }
                        case ServerCommands.COMMAND_REQUEST_QUEUE: {
                            commandRequestQueue();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                inputThread.interrupt();
                listener.onDisconnect(ConnectedDevice.this);
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    });

    public ConnectedDevice(Socket socket, IConnectedDevice listener) throws IOException {
        System.out.println("Player connecting");
        this.socket = socket;
        this.listener = listener;
        inputThread.start();
    }

    public String getName() {
        return name;
    }


    public void sendDeviceName(String name) {
        System.out.println("Sending queued device: " + name);
        outputStream.print(ServerCommands.COMMAND_QUEUED_NAME);
        outputStream.println(name);
        outputStream.flush();
    }


    private void commandAuth() throws IOException {
        name = inputStream.readLine();
        System.out.println("Played logged in: " + name);
    }

    private void commandAddToQueue() throws IOException {
        listener.addToQueue(this);
        System.out.println("Played queued: " + name);
    }

    private void commandRequestQueue() throws IOException {
        System.out.println("Queue requested: " + name);
        listener.requestQueue(this);
    }
}

