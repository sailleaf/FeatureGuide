package com.leaf.yeyy.featureguide;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leaf.yeyy.featureguide.asist.WeightAudio;
import com.leaf.yeyy.featureguide.smarklink.help.OnSmartLinkListener;
import com.leaf.yeyy.featureguide.smarklink.help.SmartLinkedModule;
import com.leaf.yeyy.featureguide.smarklink.view.SnifferSmartLinker;

public class MainActivity extends AppCompatActivity implements OnSmartLinkListener {
    private static final String TAG = "MainActivity";

    protected EditText mSsidEditText;
    protected EditText mPasswordEditText;
    protected Button mStartButton;
    protected SnifferSmartLinker mSnifferSmartLinker;
    protected Handler mViewHandler = new Handler();
    protected ProgressDialog mWaitingDialog;
    private boolean mIsConncting = false;
    private BroadcastReceiver mWifiChangedReceiver;
    private Button btnPlay;
    private EditText etWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etWeight = (EditText) findViewById(R.id.et_weight);
        btnPlay = (Button) findViewById(R.id.button);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sss = etWeight.getText().toString();
                Double f = Double.parseDouble(sss);
                //WeightSound.play(f);
                WeightAudio.play(f);
                Toast.makeText(getBaseContext(), "playing!", Toast.LENGTH_SHORT).show();
            }
        });

        mSnifferSmartLinker = SnifferSmartLinker.getInstence();

        mWaitingDialog = new ProgressDialog(this);
        mWaitingDialog.setMessage("hiflying_smartlinker_waiting");
        mWaitingDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        mWaitingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mSnifferSmartLinker.setOnSmartLinkListener(null);
                mSnifferSmartLinker.stop();
                mIsConncting = false;
            }
        });

        mSsidEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_ssid);
        mPasswordEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_password);
        mStartButton = (Button) findViewById(R.id.button_hiflying_smartlinker_start);
        mSsidEditText.setText(getSSid());

        mStartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mIsConncting) {
                    //璁剧疆瑕侀厤缃殑ssid 鍜宲swd
                    try {
                        mSnifferSmartLinker.setOnSmartLinkListener(MainActivity.this);
                        //寮�濮� smartLink
                        mSnifferSmartLinker.start(getApplicationContext(), mPasswordEditText.getText().toString().trim(),
                                mSsidEditText.getText().toString().trim());
                        mIsConncting = true;
                        mWaitingDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mWifiChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo != null && networkInfo.isConnected()) {
                    mSsidEditText.setText(getSSid());
                    mPasswordEditText.requestFocus();
                    mStartButton.setEnabled(true);
                } else {
                    mSsidEditText.setText("hiflying_smartlinker_no_wifi_connectivity");
                    mSsidEditText.requestFocus();
                    mStartButton.setEnabled(false);
                    if (mWaitingDialog.isShowing()) {
                        mWaitingDialog.dismiss();
                    }
                }
            }
        };
        registerReceiver(mWifiChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSnifferSmartLinker.setOnSmartLinkListener(null);
        try {
            unregisterReceiver(mWifiChangedReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLinked(final SmartLinkedModule module) {
        Log.w(TAG, "onLinked");
        mViewHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "hiflying_smartlinker_new_module_found" + module.getMac() + module.getModuleIP(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onCompleted() {
        Log.w(TAG, "onCompleted");
        mViewHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "hiflying_smartlinker_completed", Toast.LENGTH_SHORT).show();
                mWaitingDialog.dismiss();
                mIsConncting = false;
            }
        });
    }


    @Override
    public void onTimeOut() {
        Log.w(TAG, "onTimeOut");
        mViewHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "hiflying_smartlinker_timeout", Toast.LENGTH_SHORT).show();
                mWaitingDialog.dismiss();
                mIsConncting = false;
            }
        });
    }

    private String getSSid() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo wi = wm.getConnectionInfo();
            if (wi != null) {
                String ssid = wi.getSSID();
                if (ssid.length() > 2 && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    return ssid.substring(1, ssid.length() - 1);
                } else {
                    return ssid;
                }
            }
        }

        return "";
    }


    private String getVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "unknown";
    }

}
