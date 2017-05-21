package com.leaf.yeyy.featureguide.smarklink.view;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.leaf.yeyy.featureguide.smarklink.help.ISmartLinker;
import com.leaf.yeyy.featureguide.smarklink.help.OnSmartLinkListener;
import com.leaf.yeyy.featureguide.smarklink.help.SmartLinkedModule;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class SnifferSmartLinker implements ISmartLinker {
    public static final int DEVICE_COUNT_ONE = 1;
    public static final int DEVICE_COUNT_MULTIPLE = -1;
    private static SnifferSmartLinker me = null;
    private final String RET_KEY = "smart_config";
    private String TAG = "HFdebug";
    private String ssid;
    private String pswd;
    private String broadCastIP;
    private String gateWay;
    private Set<String> successMacSet = new HashSet();
    private int HEADER_COUNT = 200;
    private int HEADER_PACKAGE_DELAY_TIME = 10;
    private int HEADER_CAPACITY = 76;
    private OnSmartLinkListener callback;
    private int CONTENT_COUNT = 5;
    private int CONTENT_PACKAGE_DELAY_TIME = 50;
    private int CONTENT_CHECKSUM_BEFORE_DELAY_TIME = 100;
    private int CONTENT_GROUP_DELAY_TIME = 500;
    private int port = 49999;
    private byte[] receiveByte = new byte['?'];
    private boolean isConnecting = false;
    private InetAddress inetAddressbroadcast;
    private DatagramSocket socket;
    private DatagramPacket packetToSendbroadcast;
    private DatagramPacket packetToSendgateway;
    private DatagramPacket dataPacket;
    private boolean isfinding = false;
    private Runnable findThread = new Runnable() {
        public void run() {
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException localInterruptedException) {
            }
            for (int i = 0; (i < 20) && (SnifferSmartLinker.this.isConnecting); i++) {
                SnifferSmartLinker.this.sendFindCmd();
                if (SnifferSmartLinker.this.isConnecting) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException localInterruptedException1) {
                    }
                }
            }
            if ((SnifferSmartLinker.this.isConnecting) && (SnifferSmartLinker.this.callback != null)) {
                if (SnifferSmartLinker.this.successMacSet.size() <= 0) {
                    SnifferSmartLinker.this.callback.onTimeOut();
                } else if (SnifferSmartLinker.this.successMacSet.size() > 0) {
                    SnifferSmartLinker.this.callback.onCompleted();
                }
            }
            Log.e(SnifferSmartLinker.this.TAG, "stop find");
            SnifferSmartLinker.this.isfinding = false;
            SnifferSmartLinker.this.stop();
        }
    };

    private SnifferSmartLinker() {
        this.isConnecting = false;
        this.isfinding = false;
    }

    public static SnifferSmartLinker getInstence() {
        return SnifferSmartLinkerInner.SNIFFER_SMART_LINKER;
    }

    private String getBroadcastAddress(Context ctx) {
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

    private char byteToChar(byte[] b) {
        char c = (char) ((b[0] & 0xFF) << 8 | b[1] & 0xFF);
        return c;
    }

    private void sendFindCmd() {
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

    private void sendHFA11Cmd() {
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

    private void send(byte[] data) {
        this.packetToSendbroadcast = new DatagramPacket(data, data.length,
                this.inetAddressbroadcast, this.port);
        try {
            this.socket.send(this.packetToSendbroadcast);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receive() {
        Log.e(this.TAG, "start RECV");
        this.dataPacket = new DatagramPacket(this.receiveByte, this.receiveByte.length);
        new Thread() {
            public void run() {
                while (SnifferSmartLinker.this.isConnecting) {
                    try {
                        SnifferSmartLinker.this.socket.receive(SnifferSmartLinker.this.dataPacket);
                        int len = SnifferSmartLinker.this.dataPacket.getLength();
                        if (len > 0) {
                            String receiveStr = new String(SnifferSmartLinker.this.receiveByte, 0,
                                    len, "UTF-8");
                            if (receiveStr.contains("smart_config")) {
                                Log.e("RECV", "smart_config");
                                SmartLinkedModule mi = new SmartLinkedModule();
                                mi.setMac(receiveStr.replace("smart_config", "")
                                        .trim());
                                String ip = SnifferSmartLinker.this.dataPacket.getAddress().getHostAddress();
                                if ((ip.equalsIgnoreCase("0.0.0.0")) || (ip.contains(":"))) {
                                    return;
                                }
                                mi.setModuleIP(ip);
                                if (!SnifferSmartLinker.this.successMacSet.contains(mi.getMac())) {
                                    SnifferSmartLinker.this.successMacSet.add(mi.getMac());
                                    if (SnifferSmartLinker.this.callback != null) {
                                        SnifferSmartLinker.this.callback.onLinked(mi);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(SnifferSmartLinker.this.TAG, "end RECV");
                SnifferSmartLinker.this.stop();
            }
        }.start();
    }

    public void setOnSmartLinkListener(OnSmartLinkListener listener) {
        this.callback = listener;
    }

    public void start(Context context, String password, String... ssid)
            throws Exception {
        Log.e(this.TAG, ssid + ":" + password);
        if ((ssid != null) && (ssid.length > 0)) {
            this.ssid = ssid[0];
        } else {
            this.ssid = null;
        }
        this.pswd = password;
        this.broadCastIP = getBroadcastAddress(context);
        this.socket = new DatagramSocket(this.port);
        this.socket.setBroadcast(true);
        this.inetAddressbroadcast = InetAddress.getByName(this.broadCastIP);

        Log.e(this.TAG, "start");
        this.isConnecting = true;
        receive();
        this.successMacSet.clear();
        new Thread(new Runnable() {
            public void run() {
                while (SnifferSmartLinker.this.isConnecting) {
                    SnifferSmartLinker.this.connect();
                }
                Log.e(SnifferSmartLinker.this.TAG, "StopConnet");
                SnifferSmartLinker.this.stop();
            }
        })

                .start();
        if (!this.isfinding) {
            this.isfinding = true;
            new Thread(this.findThread).start();
        }
    }

    public void stop() {
        this.isConnecting = false;
        if (this.socket != null) {
            this.socket.close();
        }
    }

    public boolean isSmartLinking() {
        return this.isConnecting;
    }

    private static class SnifferSmartLinkerInner {
        private static final SnifferSmartLinker SNIFFER_SMART_LINKER = new SnifferSmartLinker();
    }
}
