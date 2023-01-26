package com.rco.rcotrucks.businesslogic;

public interface IOnBleDataUpdate {
    void OnBleStatus(String data);
    void OnBleData(String data);
}