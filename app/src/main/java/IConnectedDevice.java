public interface IConnectedDevice {
    void onDisconnect(ConnectedDevice device);

    void addToQueue(ConnectedDevice device);

    void requestQueue(ConnectedDevice device);

    void requestPair(ConnectedDevice device, String pairName);


}
