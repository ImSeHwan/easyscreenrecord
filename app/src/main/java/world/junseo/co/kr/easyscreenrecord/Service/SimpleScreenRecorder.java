package world.junseo.co.kr.easyscreenrecord.Service;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import androidx.annotation.NonNull;

import java.io.IOException;

import world.junseo.co.kr.easyscreenrecord.Util.Log.JLog;

public class SimpleScreenRecorder {

    String TAG = "SimpleScreenRecorder";

    private boolean m_bMIC;
    private int m_nWidth;
    private int m_nHeight;
    private int m_nAngle;
    private int m_nBitrate;
    private int m_nFramerate;

    private String m_sDstPath;

    private int m_nScreenDensity;
    private MediaProjection mMediaProjection;
    //private MediaProjectionManager mProjectionManager;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;
    private MediaProjectionCallback mMediaProjectionCallback;

    private boolean m_bIsProjectionManager = false;
    public boolean isProjectionManager() { return m_bIsProjectionManager; }


    private SimpleScreenRecorder() {

    }

    private static class SingletonHolder {
        public static final SimpleScreenRecorder INSTANCE = new SimpleScreenRecorder();
    }
    public static SimpleScreenRecorder getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public MediaProjection getMediaProjection() { return mMediaProjection; }

    public void setMediaProjection(@NonNull MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
    }

/*    public void setProjectionManager(@NonNull MediaProjectionManager mediaProjectionManager) {

        try {
            mProjectionManager = mediaProjectionManager;

            m_bIsProjectionManager = true;
        } catch (Exception e) {
            JLog.e(TAG,e.toString());
        }

    }*/

    private void initdata() {
        m_bMIC = false;
        m_nWidth = 0;
        m_nHeight = 0;
        m_nAngle = 0;
        m_nBitrate = 0;
        m_nFramerate = 0;
        m_nScreenDensity = 0;

        m_sDstPath = "";

        JLog.d(TAG, "initdata");
    }

    public boolean setRecordBase(Context context, boolean bMIC, int width,
                               int height, int angle, int bitrate, int framerate, int dpi, @NonNull String dstPath) {

        boolean bRet = false;

        m_bMIC = bMIC;
        m_nWidth = width;
        m_nHeight = height;
        m_nAngle = angle;
        m_nBitrate = bitrate;
        m_nFramerate = framerate;
        m_nScreenDensity = dpi;
        m_sDstPath = dstPath;

        if(mMediaProjectionCallback == null)
            mMediaProjectionCallback = new MediaProjectionCallback();

        initRecorder();
        if(prepareRecorder()) {
            bRet = true;
        }

        return bRet;
    }

    public void startRecord() {
        JLog.d(TAG, "startRecord");
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    public void stopRecord() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        JLog.d(TAG, "Recording Stopped");

        stopScreenSharing();
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaProjection = null;

            stopScreenSharing();
        }
    }

    private void stopScreenSharing() {
        JLog.d(TAG, "stopScreenSharing");

        mMediaRecorder.release();
        mMediaRecorder = null;
        initdata();

        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                m_nWidth, m_nHeight, m_nScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null /*Handler*/);
    }

    private boolean prepareRecorder() {
        JLog.d(TAG, "prepareRecorder");
        boolean bRet = false;
        try {
            mMediaRecorder.prepare();
            bRet = true;

            JLog.d(TAG, "prepareRecorder success");
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        return bRet;
    }

    private void initRecorder() {
        if (mMediaRecorder == null) {
            JLog.d(TAG, "initRecorder");
            mMediaRecorder = new MediaRecorder();
            if(m_bMIC)
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if(m_bMIC)
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(m_nBitrate);
            mMediaRecorder.setVideoFrameRate(m_nFramerate);
            //mMediaRecorder.setOrientationHint(m_nAngle);
            mMediaRecorder.setVideoSize(m_nWidth, m_nHeight);
            mMediaRecorder.setMaxDuration(1024 * 1024 * 1024); // 1기가
            mMediaRecorder.setOutputFile(m_sDstPath);

            JLog.d(TAG, "width : " + m_nWidth + " height : " + m_nHeight + " Angle : " + m_nAngle);
        }
    }
}
