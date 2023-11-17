package com.example.dialservice;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private DatagramSocket UDPSocket;
    private InetAddress address;

    private int offset = 1;
    // private String ipaddress = "192.168.0.141";

    private String ipaddress = "192.168.100.1";

    // private String ipaddress = "192.168.0.141";
    private int port = 5010;

    public static String clientdata;

    public static boolean Datareceive = false;
    String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Ethernet preference
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        Network etherNetwork = null;
        for (Network network : connectivityManager.getAllNetworks()) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                etherNetwork = network;
                //Toast.makeText(getApplicationContext(), "Ethernet interface select", Toast.LENGTH_LONG).show();

            }
        }
        connectivityManager.bindProcessToNetwork(etherNetwork);

        Thread send = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Init(InetAddress.getByName(ipaddress));
                    while (true) {
                        try {
                            if (Datareceive) {
                                SendData(offset, clientdata, port, ipaddress);
                                Datareceive = false;
                                Log.d(TAG, "UDP Data Sent");
                                //Toast.makeText(getApplicationContext(),"Udp rx: "+ clientdata, Toast.LENGTH_LONG).show();

                            }
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        send.start();

        Button send1 = findViewById(R.id.btnsend);
        send1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent explicitIntent = new Intent();
                explicitIntent.putExtra("data", "zumi");
                //explicitIntent.setClassName("com.example.dialclient", "com.example.dialclient.IPCReceiver");
                explicitIntent.setClassName("com.bosch.cthmi.debug", "com.bosch.cthmi.receiver.IPCReceiver");
                sendBroadcast(explicitIntent);

                Toast.makeText(getApplicationContext(),"Sent",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Ethernet preference
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        Network etherNetwork = null;
        for (Network network : connectivityManager.getAllNetworks()) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                etherNetwork = network;
                Toast.makeText(getApplicationContext(), "Ethernet interface select", Toast.LENGTH_LONG).show();

            }
        }
        connectivityManager.bindProcessToNetwork(etherNetwork);

        new Thread() {
            public void run() {
                //while (true) {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        /*Receiving the data*/
                        public void run() {
                            ReceiveData(5010);
                        }
                    });
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //}
            }
        }.start();
    }

    public void Init(InetAddress address) {
        try {
            this.UDPSocket = new DatagramSocket();
            this.address = address;
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /*Data send functionalities*/
    public void SendData(final int nbRepet, final String Sdata, final int port, final String address) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, Sdata);

                    for (int i = 0; i < nbRepet; i++) {
                        byte[] data = Sdata.getBytes();
                        SendInstruction(data, port);
                        //Toast.makeText(getApplicationContext(),"Udp Tx: "+ data, Toast.LENGTH_LONG).show();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    //Send
    public void SendInstruction(final byte[] data, final int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    UDPSocket.send(packet);
                    DatagramPacket packetreponse = null;
                    // UDPSocket.receive(packetreponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }


    public void ReceiveData(final int portNum) {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void run() {
                try {
                    final int taille = 100;
                    final byte[] buffer = new byte[taille];
                    DatagramSocket socketReceive = new DatagramSocket(portNum);
                    socketReceive.setBroadcast(true);
                    socketReceive.setReuseAddress(true);
                    while (true) {
                        DatagramPacket data = new DatagramPacket(buffer, buffer.length);
                        socketReceive.receive(data);
                        String str = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            str = new String(data.getData(), data.getOffset(), data.getLength(), StandardCharsets.UTF_8 // or some other charset
                            );
                        }

                        Log.d(TAG, "RXMessage:  " + str);

                       // Toast.makeText(getApplicationContext(),"Udp rx: "+ str, Toast.LENGTH_LONG).show();

                        //send reply to client app
                        Intent explicitIntent = new Intent();
                        explicitIntent.putExtra("data", str);
                       // explicitIntent.setClassName("com.example.dialclient", "com.example.dialclient.IPCReceiver");
                       explicitIntent.setClassName("com.bosch.cthmi.debug", "com.bosch.cthmi.receiver.IPCReceiver");
                        sendBroadcast(explicitIntent);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}


