package com.leaf.yeyy.featureguide.smarklink.asist;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by WIN10 on 2017/5/19.
 */

public class SmartLinkManipulator {
    public static final int DEVICE_COUNT_ONE = 1;
    public static final int DEVICE_COUNT_MULTIPLE = -1;
    private static SmartLinkManipulator me = null;
    private final String RET_KEY = "smart_config";
    public boolean isConnecting = false;
    private String TAG = "HFdebug";
    private String ssid;
    private String pswd;
    private String broadCastIP;
    private String gateWay;
    private Set<String> successMacSet = new HashSet();
    private int HEADER_COUNT = 200;
    private int HEADER_PACKAGE_DELAY_TIME = 10;
    private int HEADER_CAPACITY = 76;
    private ConnectCallBack callback;
    private int CONTENT_COUNT = 5;
    private int CONTENT_PACKAGE_DELAY_TIME = 50;
    private int CONTENT_CHECKSUM_BEFORE_DELAY_TIME = 100;
    private int CONTENT_GROUP_DELAY_TIME = 500;
    private int port = 49999;
    private byte[] receiveByte = new byte['?'];
    private InetAddress inetAddressbroadcast;
    private DatagramSocket socket;
    private DatagramPacket packetToSendbroadcast;
    private DatagramPacket packetToSendgateway;
    private DatagramPacket dataPacket;
    private boolean isfinding = false;
    Runnable findThread = new Runnable() {
        public void run() {
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException localInterruptedException) {
            }
            for (int i = 0; (i < 20) && (SmartLinkManipulator.this.isConnecting); i++) {
                SmartLinkManipulator.this.sendFindCmd();
                if (SmartLinkManipulator.this.isConnecting) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException localInterruptedException1) {
                    }
                }
            }
            if (SmartLinkManipulator.this.isConnecting) {
                if (SmartLinkManipulator.this.successMacSet.size() <= 0) {
                    SmartLinkManipulator.this.callback.onConnectTimeOut();
                } else if (SmartLinkManipulator.this.successMacSet.size() > 0) {
                    SmartLinkManipulator.this.callback.onConnectOk();
                }
            }
            Log.e(SmartLinkManipulator.this.TAG, "stop find");
            SmartLinkManipulator.this.isfinding = false;
            SmartLinkManipulator.this.StopConnection();
        }
    };

    private SmartLinkManipulator() {
        this.isConnecting = false;
        this.isfinding = false;
    }

    public static SmartLinkManipulator getInstence() {
        if (me == null) {
            me = new SmartLinkManipulator();
        }
        return me;
    }

    public void setConnection(String ssid, String password, Context ctx)
            throws SocketException, UnknownHostException {
        Log.e(this.TAG, ssid + ":" + password);
        this.ssid = ssid;
        this.pswd = password;
        this.broadCastIP = getBroadcastAddress(ctx);
        this.socket = new DatagramSocket(this.port);
        this.socket.setBroadcast(true);
        this.inetAddressbroadcast = InetAddress.getByName(this.broadCastIP);
    }

    public void Startconnection(ConnectCallBack callback) {
        Log.e(this.TAG, "Startconnection");
        this.callback = callback;
        this.isConnecting = true;
        receive();
        this.successMacSet.clear();
        new Thread(new Runnable() {
            public void run() {
                while (SmartLinkManipulator.this.isConnecting) {
                    SmartLinkManipulator.this.connect();
                }
                Log.e(SmartLinkManipulator.this.TAG, "StopConnet");
                SmartLinkManipulator.this.StopConnection();
            }
        })

                .start();
        if (!this.isfinding) {
            this.isfinding = true;
            new Thread(this.findThread).start();
        }
    }

    public void StopConnection() {
        this.isConnecting = false;
        if (this.socket != null) {
            this.socket.close();
        }
    }

    public String getBroadcastAddress(Context ctx) {
        WifiManager cm = (WifiManager) ctx
                .getSystemService(Context.WIFI_SERVICE);
        DhcpInfo myDhcpInfo = cm.getDhcpInfo();
        if (myDhcpInfo == null) {
            return "255.255.255.255";
        }
        int broadcast = myDhcpInfo.ipAddress & myDhcpInfo.netmask |
                myDhcpInfo.netmask ^ 0xFFFFFFFF;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = ((byte) (broadcast >> k * 8 & 0xFF));
        }
        try {
            return InetAddress.getByAddress(quads).getHostAddress();
        } catch (Exception e) {
        }
        return "255.255.255.255";
    }

    private void connect() {
        Log.e(this.TAG, "connect");
        int count = 1;
        byte[] header = getBytes(this.HEADER_CAPACITY);
        while ((count <= this.HEADER_COUNT) && (this.isConnecting)) {
            send(header);
            try {
                Thread.sleep(this.HEADER_PACKAGE_DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        String pwd = this.pswd;
        int[] content = new int[pwd.length() + 2];

        content[0] = 89;
        int j = 1;
        for (int i = 0; i < pwd.length(); i++) {
            content[j] = (pwd.charAt(i) + 'L');
            j++;
        }
        content[(content.length - 1)] = 86;

        count = 1;
        while ((count <= this.CONTENT_COUNT) && (this.isConnecting)) {
            for (int i = 0; i < content.length; i++) {
                int _count = 1;
                if ((i == 0) || (i == content.length - 1)) {
                    _count = 3;
                }
                int t = 1;
                while ((t <= _count) && (this.isConnecting)) {
                    send(getBytes(content[i]));
                    if (i != content.length) {
                        try {
                            Thread.sleep(this.CONTENT_PACKAGE_DELAY_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    t++;
                }
                if (i != content.length) {
                    try {
                        Thread.sleep(this.CONTENT_PACKAGE_DELAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(this.CONTENT_CHECKSUM_BEFORE_DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int checkLength = pwd.length() + 256 + 76;

            int t = 1;
            while ((t <= 3) && (this.isConnecting)) {
                send(getBytes(checkLength));
                if (t < 3) {
                    try {
                        Thread.sleep(this.CONTENT_PACKAGE_DELAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                t++;
            }
            try {
                Thread.sleep(this.CONTENT_GROUP_DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        Log.e(this.TAG, "connect END");
    }

    private byte[] getBytes(int capacity) {
        byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++) {
            data[i] = 5;
        }
        return data;
    }

    public char byteToChar(byte[] b) {
        char c = (char) ((b[0] & 0xFF) << 8 | b[1] & 0xFF);
        return c;
    }

    public void sendFindCmd() {
        System.out.println("smartlinkfind");
        this.packetToSendbroadcast = new DatagramPacket(
                "smartlinkfind".getBytes(),
                "smartlinkfind".getBytes().length, this.inetAddressbroadcast,
                48899);
        try {
            this.socket.send(this.packetToSendbroadcast);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHFA11Cmd() {
        System.out.println("smartlinkfind");
        this.packetToSendbroadcast = new DatagramPacket(
                "HF-A11ASSISTHREAD".getBytes(),
                "HF-A11ASSISTHREAD".getBytes().length, this.inetAddressbroadcast,
                48899);
        try {
            this.socket.send(this.packetToSendbroadcast);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] data) {
        this.packetToSendbroadcast = new DatagramPacket(data, data.length,
                this.inetAddressbroadcast, this.port);
        try {
            this.socket.send(this.packetToSendbroadcast);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive() {
        Log.e(this.TAG, "start RECV");
        this.dataPacket = new DatagramPacket(this.receiveByte, this.receiveByte.length);
        new Thread() {
            public void run() {
                while (SmartLinkManipulator.this.isConnecting) {
                    try {
                        SmartLinkManipulator.this.socket.receive(SmartLinkManipulator.this.dataPacket);
                        int len = SmartLinkManipulator.this.dataPacket.getLength();
                        if (len > 0) {
                            String receiveStr = new String(SmartLinkManipulator.this.receiveByte, 0,
                                    len, "UTF-8");
                            if (receiveStr.contains("smart_config")) {
                                Log.e("RECV", "smart_config");
                                ModuleInfo mi = new ModuleInfo();
                                mi.setMac(receiveStr.replace("smart_config", "")
                                        .trim());
                                String ip = SmartLinkManipulator.this.dataPacket.getAddress().getHostAddress();
                                if ((ip.equalsIgnoreCase("0.0.0.0")) || (ip.contains(":"))) {
                                    return;
                                }
                                mi.setModuleIP(ip);
                                if (!SmartLinkManipulator.this.successMacSet.contains(mi.getMac())) {
                                    SmartLinkManipulator.this.successMacSet.add(mi.getMac());
                                    SmartLinkManipulator.this.callback.onConnect(mi);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(SmartLinkManipulator.this.TAG, "end RECV");
                SmartLinkManipulator.this.StopConnection();
            }
        }.start();
    }

    public static abstract interface ConnectCallBack {
        public abstract void onConnect(ModuleInfo paramModuleInfo);

        public abstract void onConnectTimeOut();

        public abstract void onConnectOk();
    }
}
