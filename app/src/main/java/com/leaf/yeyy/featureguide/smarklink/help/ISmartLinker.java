package com.leaf.yeyy.featureguide.smarklink.help;

import android.content.Context;

/**
 * Created by WIN10 on 2017/5/19.
 */

public interface ISmartLinker {
    public static final int V3 = 3;
    public static final int V5 = 5;

    public abstract void setOnSmartLinkListener(OnSmartLinkListener paramOnSmartLinkListener);

    public abstract void start(Context paramContext, String paramString, String... paramVarArgs)
            throws Exception;

    public abstract void stop();

    public abstract boolean isSmartLinking();
}
