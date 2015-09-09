public interface IConnectedDevice {
    void onDisconnect(ConnectedDevice device);

    void addToQueue(ConnectedDevice device);

    void requestQueue(ConnectedDevice device);
}
