package cn.sprd.com.carmonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by SPREADTRUM\joe.yu on 8/3/15.
 */
// android:sharedUserId="android.uid.system"
public class CarMonitorSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener,Preference.OnPreferenceClickListener{

    private static final String TAG = "CarMonitorSettings";
    private static final String PREF_KEY_OPEN_REC_DIR = "openRecDir";
    private static final String PREF_KEY_CLEAR_ALL = "clearAll";
    private static final String REC_DIR = "/sdcard/rec/";
    private Preference mPrefOpenDir,mPrefClearAll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.carsettings);
        mPrefOpenDir = this.getPreferenceScreen().findPreference(PREF_KEY_OPEN_REC_DIR);
        if (mPrefOpenDir != null){
            mPrefOpenDir.setOnPreferenceClickListener(this);
        }
        mPrefClearAll = this.getPreferenceScreen().findPreference(PREF_KEY_CLEAR_ALL);
        if(mPrefClearAll != null){
            mPrefClearAll.setOnPreferenceClickListener(this);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(PREF_KEY_OPEN_REC_DIR.equals("key")){

        }

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mPrefOpenDir){
            Log.d(TAG, "open rec dir....");
            ComponentName cn = new ComponentName("cn.sprd.com.carmonitor","cn.sprd.com.carmonitor.MiniFileExplorer");
            Intent intent = new Intent();
            intent.setComponent( cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if (preference == mPrefClearAll){
            Log.d(TAG,"clear all");
            clearAll();
        }

        return false;
    }
    void clearAll() {
            Log.e(TAG, "clear All....");
            File recDir = new File(REC_DIR);
            File[] recs = recDir.listFiles();
            if (recs != null) {
                for (File file : recs) {
                    file.delete();
                }
            }
            Toast.makeText(this, R.string.delete_all_sucess, Toast.LENGTH_LONG).show();

    }

}
