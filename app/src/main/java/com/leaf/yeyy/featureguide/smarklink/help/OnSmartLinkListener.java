package com.leaf.yeyy.featureguide.smarklink.help;

/**
 * Created by WIN10 on 2017/5/19.
 */

public interface OnSmartLinkListener {
    public abstract void onLinked(SmartLinkedModule paramSmartLinkedModule);

    public abstract void onCompleted();

    public abstract void onTimeOut();
}
