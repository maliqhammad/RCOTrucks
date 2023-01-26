package com.rco.rcotrucks.utils;

import android.net.ConnectivityManager;
        import android.net.NetworkCapabilities;
        import android.net.NetworkRequest;

public class ConnectionMonitor extends ConnectivityManager.NetworkCallback {

    public interface Network {
        void onNetworkLost();
        void onNetworkAvailable();
    }

    private final Network network;
    private final NetworkRequest networkRequest;
    private ConnectivityManager connectivityManager;
    private Boolean networkAvailable = false;

    public ConnectionMonitor(Network network, ConnectivityManager connectivityManager) {
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();



        this.network = network;
        this.connectivityManager = connectivityManager;
    }

    public boolean isConnected(){
        return networkAvailable;
    }

    public void registerNetworkCallback() {
        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    public void unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public void onAvailable(android.net.Network network) {
        this.network.onNetworkAvailable();

        networkAvailable = true;
    }

    @Override
    public void onLost(android.net.Network network) {
        this.network.onNetworkLost();
        networkAvailable = false;
    }

//    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//    NetworkInfo netInfo = cm.getActiveNetworkInfo();
//    //should check null because in airplane mode it will be null
//    NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
//    int downSpeed = nc.getLinkDownstreamBandwidthKbps();
//    int upSpeed = nc.getLinkUpstreamBandwidthKbps();

}