/**
 * Created by SPREADTRUM\joe.yu on 7/28/15.
 */
package cn.sprd.com.carmonitor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;


public class PhoneStatusReceiver extends BroadcastReceiver {

    private String POWER_PLUG_IN = Intent.ACTION_POWER_CONNECTED;
    private String POWER_DISCONNECT = Intent.ACTION_POWER_DISCONNECTED;
    private String BATTERY_CHANGE = Intent.ACTION_BATTERY_CHANGED;
    private String BATTERY_LOW = Intent.ACTION_BATTERY_LOW;
    private String TAG = "PhoneStatusReceiver";
    private Context mContext = null;
    private boolean mPlugged = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(mContext == null){
            mContext = context;
        }
        String action = intent.getAction();
        if (POWER_PLUG_IN.equals(action)) {
            Log.e(TAG, "Power Plug in");
            Toast.makeText(mContext,"plug in!!",Toast.LENGTH_LONG).show();
             startActivitySafe();
        }else if(POWER_DISCONNECT.equals(action)){
            Log.e(TAG, "Power Plug out");
            Toast.makeText(mContext,"plug out!!",Toast.LENGTH_LONG).show();
            handleNoPowerFinish();
        }else  if(BATTERY_CHANGE.equals(action)){
            Log.e(TAG,"Battery changed");
            Toast.makeText(mContext,"BatteryChanged!!",Toast.LENGTH_LONG).show();
        }else if(BATTERY_LOW.equals(action)){
            Log.e(TAG,"Power low");
        }else if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
            Log.e(TAG,"Boot complete");
            startActivitySafe();
        }
    }
    private void handleNoPowerFinish(){
        mContext.sendBroadcast(new Intent("com.sprd.car.shutdown"));
        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        //SPRD: add shutdown reason for PhoneInfo feature
        intent.putExtra("shutdown_mode", "battery");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    private void  handleBatteryChanged(Intent intent){
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        if(plugged != 0){

        }
    }
    void startCarMonitorActivity(){
        ComponentName cn = new ComponentName("cn.sprd.com.carmonitor","cn.sprd.com.carmonitor.MainActivity");
        Intent intent = new Intent();
        intent.setComponent( cn);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }

    private void startActivitySafe(){
        try{
            startCarMonitorActivity();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
