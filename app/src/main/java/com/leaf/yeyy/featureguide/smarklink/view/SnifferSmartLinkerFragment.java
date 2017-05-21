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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leaf.yeyy.featureguide.smarklink.help.OnSmartLinkListener;
import com.leaf.yeyy.featureguide.smarklink.help.R1;
import com.leaf.yeyy.featureguide.smarklink.help.SmartLinkedModule;


public class SnifferSmartLinkerFragment extends Fragment implements OnSmartLinkListener {
    private static final String TAG = "SnifferSmartLinkerFragment";
    protected EditText mSsidEditText;
    protected EditText mPasswordEditText;
    protected Button mStartButton;
    protected SnifferSmartLinker mSnifferSmartLinker;
    protected Handler mViewHandler = new Handler();
    protected ProgressDialog mWaitingDialog;
    private boolean mIsConncting = false;
    private BroadcastReceiver mWifiChangedReceiver;
    private Context mAppContext;

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mAppContext = activity.getApplicationContext();
        R1.initContext(this.mAppContext);

        this.mSnifferSmartLinker = SnifferSmartLinker.getInstence();
        this.mSnifferSmartLinker.setOnSmartLinkListener(this);

        this.mWaitingDialog = new ProgressDialog(activity);
        this.mWaitingDialog.setMessage(getString(R1.string("hiflying_smartlinker_waiting")));
        this.mWaitingDialog.setButton(-2, "Waiting", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        this.mWaitingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                SnifferSmartLinkerFragment.this.mSnifferSmartLinker.setOnSmartLinkListener(null);
                SnifferSmartLinkerFragment.this.mSnifferSmartLinker.stop();
                SnifferSmartLinkerFragment.this.mIsConncting = false;
            }
        });
    }

    public void onDetach() {
        super.onDetach();
        this.mSnifferSmartLinker.setOnSmartLinkListener(null);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R1.layout("activity_hiflying_sniffer_smart_linker"), container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mSsidEditText = ((EditText) view.findViewById(R1.id("editText_hiflying_smartlinker_ssid")));
        this.mPasswordEditText = ((EditText) view.findViewById(R1.id("editText_hiflying_smartlinker_password")));
        this.mStartButton = ((Button) view.findViewById(R1.id("button_hiflying_smartlinker_start")));
        this.mSsidEditText.setText(getSSid());

        this.mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!SnifferSmartLinkerFragment.this.mIsConncting) {
                    try {
                        SnifferSmartLinkerFragment.this.mSnifferSmartLinker.setOnSmartLinkListener(SnifferSmartLinkerFragment.this);

                        SnifferSmartLinkerFragment.this.mSnifferSmartLinker.start(SnifferSmartLinkerFragment.this.mAppContext, SnifferSmartLinkerFragment.this.mPasswordEditText.getText().toString().trim(), new String[]{
                                SnifferSmartLinkerFragment.this.mSsidEditText.getText().toString().trim()});
                        SnifferSmartLinkerFragment.this.mIsConncting = true;
                        SnifferSmartLinkerFragment.this.mWaitingDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.mWifiChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager) SnifferSmartLinkerFragment.this.mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(1);
                if ((networkInfo != null) && (networkInfo.isConnected())) {
                    SnifferSmartLinkerFragment.this.mSsidEditText.setText(SnifferSmartLinkerFragment.this.getSSid());
                    SnifferSmartLinkerFragment.this.mPasswordEditText.requestFocus();
                    SnifferSmartLinkerFragment.this.mStartButton.setEnabled(true);
                } else {
                    SnifferSmartLinkerFragment.this.mSsidEditText.setText(SnifferSmartLinkerFragment.this.getString(R1.string("hiflying_smartlinker_no_wifi_connectivity")));
                    SnifferSmartLinkerFragment.this.mSsidEditText.requestFocus();
                    SnifferSmartLinkerFragment.this.mStartButton.setEnabled(false);
                    if (SnifferSmartLinkerFragment.this.mWaitingDialog.isShowing()) {
                        SnifferSmartLinkerFragment.this.mWaitingDialog.dismiss();
                    }
                }
            }
        };
        this.mAppContext.registerReceiver(this.mWifiChangedReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    public void onDestroyView() {
        super.onDestroyView();
        try {
            this.mAppContext.unregisterReceiver(this.mWifiChangedReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLinked(final SmartLinkedModule module) {
        Log.w("SnifferSmartLinker", "onLinked");
        this.mViewHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(SnifferSmartLinkerFragment.this.mAppContext, SnifferSmartLinkerFragment.this.getString(R1.string("hiflying_smartlinker_new_module_found"), new Object[]{module.getMac(), module.getModuleIP()}), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onCompleted() {
        Log.w("SnifferSmartLinker", "onCompleted");
        this.mViewHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(SnifferSmartLinkerFragment.this.mAppContext, SnifferSmartLinkerFragment.this.getString(R1.string("hiflying_smartlinker_completed")), Toast.LENGTH_LONG).show();
                SnifferSmartLinkerFragment.this.mWaitingDialog.dismiss();
                SnifferSmartLinkerFragment.this.mIsConncting = false;
            }
        });
    }

    public void onTimeOut() {
        Log.w("SnifferSmartLinker", "onTimeOut");
        this.mViewHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(SnifferSmartLinkerFragment.this.mAppContext, SnifferSmartLinkerFragment.this.getString(R1.string("hiflying_smartlinker_timeout")), Toast.LENGTH_LONG).show();
                SnifferSmartLinkerFragment.this.mWaitingDialog.dismiss();
                SnifferSmartLinkerFragment.this.mIsConncting = false;
            }
        });
    }

    private String getSSid() {
        WifiManager wm = (WifiManager) this.mAppContext.getSystemService(Context.WIFI_SERVICE);
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
