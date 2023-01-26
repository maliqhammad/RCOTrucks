package com.rco.rcotrucks.interfaces;

public interface ListenFromActivity {
    void notifySyncComplete();
    void notifyBleConnection();
    void updateBottomDrivingStatus();
    void notifyShiftChanged();
    void onNetworkAvailable();
    void onNetworkLost();
}
