package com.leaf.yeyy.featureguide.asist;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WIN10 on 2017/5/17.
 */

public class WeightSound {
    private static String TAG = WeightSound.class.getSimpleName();
    private static int baiWei;
    private static int shiWei;
    private static int geWei;
    private static int zhengshu;
    private static int xiaoshu;
    private static int xiaoshu1;
    private static int xiaoshu2;

    private static String dot = "\\.";


    private WeightSound() {
    }

    public static void play(float fWeight) {
        DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足1位,会以0补足.
        String p = decimalFormat.format(fWeight);//format 返回的是字符串
        Log.d(TAG, "" + p);
        Log.d(TAG, "" + p.split("\\.").length);

        if (!splitDigit(p.split(dot))) {
            Log.e(TAG, "error!!!!");
            return;
        }
        if (shiWei != 0 && shiWei != 1) { // 20~99的情况
            if (geWei != 0) {
                if (xiaoshu != 0) {
                    sound2XD0();
                } else {
                    sound2X();
                }
            } else {
                if (xiaoshu != 0) {
                    sound20D0();
                } else {
                    sound20();
                }
            }
        } else if (shiWei == 1) { //10~19的情况
            if (geWei != 0) {
                if (xiaoshu != 0) {
                    sound1XD0();
                } else {
                    sound1X();
                }
            } else {
                if (xiaoshu != 0) {
                    sound10D0();
                } else {
                    sound10();
                }
            }
        } else { // 0~9的情况
            if (geWei != 0) {
                if (xiaoshu != 0) {
                    soundXD0();
                } else {
                    soundX();
                }
            } else {
                if (xiaoshu != 0) {
                    sound0D0();
                } else {
                    sound0();
                }
            }
        }

    }

    /**
     * 1~9带个位带小数位
     */
    private static void soundXD0() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/00" + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/point.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + xiaoshu + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 1~9带个位不带小数位
     */
    private static void soundX() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/00" + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 0~9不带个位带小数位
     */
    private static void sound0D0() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/00" + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/point.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + xiaoshu + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 0~9不带个位不带小数位
     */
    private static void sound0() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/00" + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }

    }

    /**
     * 10不带个位带小数位
     */
    private static void sound10D0() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + "0.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/point.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + xiaoshu + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 10不带个位不带小数位
     */
    private static void sound10() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }


    /**
     * 11~19带个位带小数位
     */
    private static void sound1XD0() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/point.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + xiaoshu + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 11~19带个位不带小数位
     */
    private static void sound1X() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 20~99不带个位带小数位
     */
    private static void sound20D0() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + "0.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/point.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + xiaoshu + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 20~99不带个位不带小数位
     */
    private static void sound20() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + "0.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 20~99带个位和小数位
     */
    private static void sound2XD0() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + "0.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/point.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + xiaoshu + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    /**
     * 20~99带个位不带小数位
     */
    private static void sound2X() {
        AudioPlayer audioPlayer = new AudioPlayer(null);
        List<String> urlList = new ArrayList<>();
        urlList.add("http://auth.api.cfcmu.cn/music/0" + shiWei + "0.m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/00" + geWei + ".m4a");
        urlList.add("http://auth.api.cfcmu.cn/music/kg.m4a");
        try {
            audioPlayer.playUrlList(urlList);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    public static boolean splitDigit(String[] strDigit) {
        if (strDigit.length != 2) {
            Log.e(TAG, "wrong one");
            return false;
        }
        if (strDigit[0].length() > 2) {
            Log.e(TAG, "wrong two");
            return false;
        }
        if (strDigit[0].length() == 0) {
            Log.e(TAG, "wrong three");
            return false;
        }

        try {
            zhengshu = Integer.parseInt(strDigit[0]);
            xiaoshu = Integer.parseInt(strDigit[1]);
            shiWei = zhengshu / 10;
            geWei = zhengshu % 10;
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }
}
