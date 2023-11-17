// aidlInterface.aidl
package com.example.dialservice;

// Declare any non-default types here with import statements

interface aidlInterface {
    String SendData();
    void ReceiveData(int offset,String command,int port,String ipaddress );
}