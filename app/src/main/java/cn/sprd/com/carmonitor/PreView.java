package cn.sprd.com.carmonitor;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by SPREADTRUM\joe.yu on 7/25/15.
 */
public class PreView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceHolder mHolder;
    Camera mCamera;
    List<Camera.Size> mPreviewSize;
    int mPreviewWidth,mPreViewHeight;
    MediaRecorder mRecorder = null;
    Context mContext;
    String mCurrentFileName;
    String STORAGE_PATH = "/sdcard/rec";
    Paint mPaint;
    boolean mStarted;
    String TAG = "PreView";
    public PreView(Context context) {
        super(context);
        mContext = context;
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        File dir = new File(STORAGE_PATH);
        if(!dir.exists()){
            dir.mkdir();
        }
        mPaint = new Paint();
        mPaint.setTextSize(30);
        mStarted = false;
    }

    public PreView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        /*
        if(mCamera != null){
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    private void stopPreviewAndFreeCamera() {

        if (mCamera != null) {
            // Call stopPreview() to stop updating the preview surface.
            mCamera.stopPreview();

            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            mCamera.release();

            mCamera = null;
        }
    }

    public void setCamera(Camera camera){
        if(camera == mCamera)
        {
            return;
        }
        stopPreviewAndFreeCamera();
        mCamera = camera;
        if(mCamera != null){
            List<Camera.Size> localSize = mCamera.getParameters().getSupportedPreviewSizes();
            mPreviewSize = localSize;
            try{
                mCamera.setPreviewDisplay(mHolder);
            }catch (Exception e){}

            Camera.Parameters parms = mCamera.getParameters();
           // parms.setPreviewSize(320,480);
           // mCamera.setParameters(parms);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();

        }

    }


    private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                mContext.getString(R.string.video_file_name_format));
        return dateFormat.format(date);
    }

    String getRecFileName(){
        long time = System.currentTimeMillis();
        String name = createName(time);
        return STORAGE_PATH+"/"+name+".mp4";
    }
    public String getCurrentFileName(){
        return mCurrentFileName;
    };
    public void startRec(){
        Log.e(TAG,"startRec");
        try {
            if(mCamera != null){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            mCamera = Camera.open(0);
            Camera.Parameters parms = mCamera.getParameters();
            mRecorder = new MediaRecorder();
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            mCamera.setDisplayOrientation(90);
            mCamera.unlock();
            mCurrentFileName = getRecFileName();
            mRecorder.setCamera(mCamera);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setPreviewDisplay(mHolder.getSurface());
            mRecorder.setProfile(profile);
            mRecorder.setOutputFile(mCurrentFileName);
            mRecorder.prepare();
            mRecorder.start();
            mStarted =true;
           // mThread.start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void stopRec(){
        Log.e(TAG,"stopRec");
        if(mRecorder != null){
            mRecorder.stop();
            mRecorder.release();
            Toast.makeText(mContext,"Movie has been stored in "+mCurrentFileName,Toast.LENGTH_LONG).show();
            mStarted = false;
            mRecorder = null;
        }
    }




}
