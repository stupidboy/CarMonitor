package cn.sprd.com.carmonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import android.app.usage.UsageStatsManager;

public class MainActivity extends Activity {


    private HandlerThread mHandlerThread = new HandlerThread("CameraThread");
    private static final int MSG_MAIN_HANDLE_REC = 0;
    private static final int MSG_MAIN_HANDLE_REC_ROLL = 1;
    private static final long DELAY_ROLL = 10 * 60 * 1000; //10min 600M
    private static final int REMAIN_FILES = 5;
    private Camera mCamera;
    private PreView mPreView;
    private RelativeLayout mLayout;
    private Button mButtonCap, mButtonRecord, mButtonSet;
    private boolean mRecStarted = false;
    private Handler mMainHandler;
    private BroadcastReceiver mReceiver;
    private String SHUTDOWN_ACTION = "com.sprd.car.shutdown";
    private IntentFilter mFilter;
    String TAG = "mainActivity";
    String STORAGE_PATH = "/sdcard/rec";
    private boolean mPendingMark = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandlerThread.start();
        //mCamerCtlHandler =new CameraControlHandler(mHandlerThread.getLooper());
        mPreView = new PreView(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        mLayout = (RelativeLayout) this.findViewById(R.id.camerapreview);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mButtonCap = (Button) this.findViewById(R.id.button_cap);
        mButtonRecord = (Button) this.findViewById(R.id.button_start);
        mButtonSet= (Button) this.findViewById(R.id.button_set);
        mButtonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRec(!mRecStarted);
            }
        });
        mButtonCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markCurrentFile();
            }
        });
        mButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRec(false);
                ComponentName cn = new ComponentName("cn.sprd.com.carmonitor","cn.sprd.com.carmonitor.CarMonitorSettings");
                Intent intent = new Intent();
                intent.setComponent( cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mLayout.addView(mPreView);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (SHUTDOWN_ACTION.equals(action)) {
                    handleRec(false);
                    finish();
                }
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(SHUTDOWN_ACTION);
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_MAIN_HANDLE_REC:
                        handleRec((boolean) msg.obj);
                        break;
                    case MSG_MAIN_HANDLE_REC_ROLL:
                        handlerRoll();
                        break;

                }
            }
        };
        HotProcessList hotProcessList= new HotProcessList(this);
        hotProcessList.updateUseageStats();
        Test.test();
    }
    void setBackLight(int brightness){

    }
    void clearAll() {
        if (!mRecStarted) {
            Log.e(TAG, "clear All....");
            File recDir = new File(STORAGE_PATH);
            File[] recs = recDir.listFiles();
            if (recs != null) {
                for (File file : recs) {
                    file.delete();
                }
            }
            Toast.makeText(this, R.string.delete_all_sucess, Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "clear All can't be done while recording");
            Toast.makeText(this, R.string.delete_all_fail, Toast.LENGTH_LONG).show();
        }

    }
    void markCurrentFile(){


        String currentName =  mPreView.getCurrentFileName();
        File currentFile = new File(currentName);
        if(mRecStarted) {
            handleRec(false);
        }
        currentFile.renameTo(new File(currentName+".stored"));
    }
    void handlerRoll() {
        File recDir = new File(STORAGE_PATH);
        File[] recs = recDir.listFiles();
        if (recs != null) {
            Arrays.sort(recs);
            Log.e(TAG, "handle Roll , size= " + recs.length);
            if (recs.length > REMAIN_FILES) {
                for (int i = 0; i <= recs.length - REMAIN_FILES; i++) {
                    String name = recs[i].getName();
                    if (!name.endsWith(".stored")) {
                        Log.e(TAG, "handle Roll , delete...= " + recs[i].getName());
                        recs[i].delete();
                    }
                }
            }
        }  
        handleRec(false);
        handleRec(true);
    }

    void handleRec(boolean start) {
        Log.e(TAG, "handleRec  :" + start);
        if (start) {
            mPreView.startRec();
            mRecStarted = true;
            mButtonRecord.setText(R.string.button_stop);
            mMainHandler.removeMessages(MSG_MAIN_HANDLE_REC_ROLL);
            mMainHandler.sendEmptyMessageDelayed(MSG_MAIN_HANDLE_REC_ROLL, DELAY_ROLL);
            setBackLight(0);
        } else {
            mPreView.stopRec();
            mRecStarted = false;
            mButtonRecord.setText(R.string.button_start);
            mMainHandler.removeMessages(MSG_MAIN_HANDLE_REC_ROLL);
            setBackLight(1);
        }
    }


    @Override
    protected void onStop() {
        Log.e(TAG, "OnStop");
        super.onStop();
        //closeCamera();
        handleRec(false);
        //finish();
    }

    @Override
    public void finish() {
        Log.e(TAG, "finish");
        super.finish();
        try {
            this.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        this.registerReceiver(mReceiver, mFilter);
        Message msg = new Message();
        msg.what = MSG_MAIN_HANDLE_REC;
        msg.obj = true;
        mMainHandler.sendMessageDelayed(msg, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



/*
    private void openCameraSafe(){
        try{
            releaseCameraAndPreView();
            mCamera = Camera.open(0);
            mPreView.setCamera(mCamera);
        }catch (Exception e){
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
    }

    class  CameraControlHandler extends Handler{

        public CameraControlHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_OPEN_CAMERA:
                    openCameraSafe();
                   // handleRecInMainLoop(true);
                    break;
                case MSG_CLOSE_CAMERA:
                    releaseCameraAndPreView();
                    break;
                default:
                    break;

            }
        }
    }
    */
}
