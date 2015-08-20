package cn.sprd.com.carmonitor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by SPREADTRUM\joe.yu on 8/3/15.
 */
public class AccMonitor {

    private Context mContext;
    private SensorManager mSensorManager;
    private String TAG = "AccMonitor";
    public AccMonitor(Context context){
        mContext = context;
        mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor mAcc = mSensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER);
        Sensor mGravity = mSensorManager.getDefaultSensor(SensorManager.SENSOR_ORIENTATION);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.e(TAG, "x= " + event.values[0] + " y= " + event.values[1] + " z = " + event.values[2]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        },mGravity,SensorManager.SENSOR_DELAY_NORMAL);
    }






    class AccMonitorThread extends Thread{

    }

}
