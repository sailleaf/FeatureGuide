package com.leaf.yeyy.featureguide.smarklink.help;

import java.io.Serializable;

/**
 * Created by WIN10 on 2017/5/19.
 */
public class SmartLinkedModule implements Serializable {
    private static final long serialVersionUID = 833195854008521358L;
    private String mac;
    private String ip;
    private String mid;

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getModuleIP() {
        return this.ip;
    }

    public void setModuleIP(String moduleIP) {
        this.ip = moduleIP;
    }

    public String getMid() {
        return this.mid;
    }

    public void setMid(String string) {
        this.mid = string;
    }

    public String toString() {
        return "SmartLinkedModule [mac=" + this.mac + ", ip=" + this.ip + ", mid=" + this.mid + "]";
    }
}

