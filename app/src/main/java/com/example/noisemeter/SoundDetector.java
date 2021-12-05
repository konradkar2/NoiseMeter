package com.example.noisemeter;

import android.media.MediaRecorder;

import java.io.File;

public class SoundDetector {
    private MediaRecorder mRecorder;
    private String mFilepath;
    private int mThresholdDb;
    private long mPollingIntervalMs;
    private Boolean mEnabled = false;
    private IHandleAmplitude mHandler = null;
    private final Object lock = new Object();

    public SoundDetector(String filepath, int thresholdDb, long pollingIntervalMs) {
        mFilepath = filepath;
        mThresholdDb = thresholdDb;
        mPollingIntervalMs = pollingIntervalMs;
        initializeRecorder();
        startRecording();
        mainLoop();
    }

    public void enable() {
        synchronized (lock) {
            getAmplitudeDb();
            mEnabled = true;
        }
    }

    public void disable() {
        synchronized (lock) {
            mEnabled = false;
        }
    }

    public void setHandler(IHandleAmplitude handler) {
        synchronized (lock) {
            mHandler = handler;
        }
    }

    private void mainLoop() {
        new Thread(() ->
        {
            while (true) {
                synchronized (lock) {
                    if (mEnabled && mHandler != null) {
                        double amplitudeDb = getAmplitudeDb();
                        if (amplitudeDb > mThresholdDb)
                            mHandler.handle(amplitudeDb,true);
                        else
                            mHandler.handle(amplitudeDb,false);
                    }
                }
                try {
                    Thread.sleep(mPollingIntervalMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void initializeRecorder() {
        File file = new File(mFilepath);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setOutputFile(mFilepath);
        try {
            mRecorder.prepare();

        } catch (java.io.IOException ioe) {
            android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

        } catch (java.lang.SecurityException e) {
            android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
        }

    }

    private void startRecording() {
        try {
            mRecorder.start();
        } catch (java.lang.SecurityException e) {
            android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
        } catch (IllegalStateException e) {
            android.util.Log.e("[Monkey]", "IllegalStateException: " + android.util.Log.getStackTraceString(e));
        } catch (Exception e) {
            android.util.Log.e("[Monkey]", "Exception: " + android.util.Log.getStackTraceString(e));
        }
    }

    private double getAmplitudeDb() {
        double amp = getAmplitude();
        android.util.Log.w("[Monkey]", "p: " + Double.toString(amp));
        return 20 * Math.log10(amp);
    }

    private double getAmplitude() {
        if (mRecorder != null)
            return mRecorder.getMaxAmplitude();
        else
            return 0;
    }


}
