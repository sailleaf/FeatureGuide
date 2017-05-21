package com.leaf.yeyy.featureguide.smarklink.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leaf.yeyy.featureguide.smarklink.help.OnSmartLinkListener;
import com.leaf.yeyy.featureguide.smarklink.help.R1;
import com.leaf.yeyy.featureguide.smarklink.help.SmartLinkedModule;


public class SnifferSmartLinkerActivity extends Activity implements OnSmartLinkListener {
    private static final String TAG = "SnifferSmartLinkerActivity";
    protected EditText mSsidEditText;
    protected EditText mPasswordEditText;
    protected Button mStartButton;
    protected SnifferSmartLinker mSnifferSmartLinker;
    protected Handler mViewHandler = new Handler();
    protected ProgressDialog mWaitingDialog;
    private boolean mIsConncting = false;
    private BroadcastReceiver mWifiChangedReceiver;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        R1.initContext(getApplicationContext());
        this.mSnifferSmartLinker = SnifferSmartLinker.getInstence();

        this.mWaitingDialog = new ProgressDialog(this);
        this.mWaitingDialog.setMessage(getString(R1.string("hiflying_smartlinker_waiting")));
        this.mWaitingDialog.setButton(-2, "Waiting", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        this.mWaitingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                SnifferSmartLinkerActivity.this.mSnifferSmartLinker.setOnSmartLinkListener(null);
                SnifferSmartLinkerActivity.this.mSnifferSmartLinker.stop();
                SnifferSmartLinkerActivity.this.mIsConncting = false;
            }
        });
        setContentView(R1.layout("activity_hiflying_sniffer_smart_linker"));
        this.mSsidEditText = ((EditText) findViewById(R1.id("editText_hiflying_smartlinker_ssid")));
        this.mPasswordEditText = ((EditText) findViewById(R1.id("editText_hiflying_smartlinker_password")));
        this.mStartButton = ((Button) findViewById(R1.id("button_hiflying_smartlinker_start")));
        this.mSsidEditText.setText(getSSid());

        this.mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!SnifferSmartLinkerActivity.this.mIsConncting) {
                    try {
                        SnifferSmartLinkerActivity.this.mSnifferSmartLinker.setOnSmartLinkListener(SnifferSmartLinkerActivity.this);

                        SnifferSmartLinkerActivity.this.mSnifferSmartLinker.start(SnifferSmartLinkerActivity.this.getApplicationContext(), SnifferSmartLinkerActivity.this.mPasswordEditText.getText().toString().trim(), new String[]{
                                SnifferSmartLinkerActivity.this.mSsidEditText.getText().toString().trim()});
                        SnifferSmartLinkerActivity.this.mIsConncting = true;
                        SnifferSmartLinkerActivity.this.mWaitingDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.mWifiChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager) SnifferSmartLinkerActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(1);
                if ((networkInfo != null) && (networkInfo.isConnected())) {
                    SnifferSmartLinkerActivity.this.mSsidEditText.setText(SnifferSmartLinkerActivity.this.getSSid());
                    SnifferSmartLinkerActivity.this.mPasswordEditText.requestFocus();
                    SnifferSmartLinkerActivity.this.mStartButton.setEnabled(true);
                } else {
                    SnifferSmartLinkerActivity.this.mSsidEditText.setText(SnifferSmartLinkerActivity.this.getString(R1.string("hiflying_smartlinker_no_wifi_connectivity")));
                    SnifferSmartLinkerActivity.this.mSsidEditText.requestFocus();
                    SnifferSmartLinkerActivity.this.mStartButton.setEnabled(false);
                    if (SnifferSmartLinkerActivity.this.mWaitingDialog.isShowing()) {
                        SnifferSmartLinkerActivity.this.mWaitingDialog.dismiss();
                    }
                }
            }
        };
        registerReceiver(this.mWifiChangedReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mSnifferSmartLinker.setOnSmartLinkListener(null);
        try {
            unregisterReceiver(this.mWifiChangedReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLinked(final SmartLinkedModule module) {
        Log.w("SnifferSmartLinker", "onLinked");
        this.mViewHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(SnifferSmartLinkerActivity.this.getApplicationContext(), SnifferSmartLinkerActivity.this.getString(R1.string("hiflying_smartlinker_new_module_found"), new Object[]{module.getMac(), module.getModuleIP()}), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onCompleted() {
        Log.w("SnifferSmartLinker", "onCompleted");
        this.mViewHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(SnifferSmartLinkerActivity.this.getApplicationContext(), SnifferSmartLinkerActivity.this.getString(R1.string("hiflying_smartlinker_completed")), Toast.LENGTH_LONG).show();
                SnifferSmartLinkerActivity.this.mWaitingDialog.dismiss();
                SnifferSmartLinkerActivity.this.mIsConncting = false;
            }
        });
    }

    public void onTimeOut() {
        Log.w("SnifferSmartLinker", "onTimeOut");
        this.mViewHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(SnifferSmartLinkerActivity.this.getApplicationContext(), SnifferSmartLinkerActivity.this.getString(R1.string("hiflying_smartlinker_timeout")), Toast.LENGTH_LONG).show();
                SnifferSmartLinkerActivity.this.mWaitingDialog.dismiss();
                SnifferSmartLinkerActivity.this.mIsConncting = false;
            }
        });
    }

    private String getSSid() {
        WifiManager wm = (WifiManager) getApplication().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            WifiInfo wi = wm.getConnectionInfo();
            if (wi != null) {
                String ssid = wi.getSSID();
                if ((ssid.length() > 2) && (ssid.startsWith("\"")) && (ssid.endsWith("\""))) {
                    return ssid.substring(1, ssid.length() - 1);
                }
                return ssid;
            }
        }
        return "";
    }
}
