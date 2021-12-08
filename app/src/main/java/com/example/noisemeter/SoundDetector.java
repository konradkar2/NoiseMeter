package com.example.noisemeter;

import android.media.MediaRecorder;

import java.io.File;

public class SoundDetector {
    private MediaRecorder mRecorder;
    private String mFilepath;
    private int mThresholdDb;
    private long mPollingIntervalMs;
    private boolean mEnabled = false;
    private boolean mIsSoundDetected = false;
    private double mLastAmplitudeDb = 0.0;
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
    public double waitForSound() throws Exception {
        synchronized (lock) {
            if(mIsSoundDetected)
                return mLastAmplitudeDb;
            lock.wait(3000);
            if(!mIsSoundDetected)
            {
                throw new Exception("Failed to detect sound");
            }
            return mLastAmplitudeDb;
        }
    }
    public void waitForNoSound() throws InterruptedException {
        synchronized (lock) {
            if(mIsSoundDetected)
                lock.wait();
        }
    }

    private void mainLoop() {
        new Thread(() ->
        {
            while (true) {
                synchronized (lock) {
                    if (mEnabled) {
                        Double amplitudeDb = getAmplitudeDb();
                        if(amplitudeDb != null)
                        {
                            mLastAmplitudeDb = amplitudeDb;
                            boolean isSoundDetected = amplitudeDb > mThresholdDb;
                            android.util.Log.w("[Monkey]", "isSoundDetected: " + Boolean.toString(isSoundDetected));
                            if(isSoundDetected != mIsSoundDetected){
                                mIsSoundDetected = isSoundDetected;
                                lock.notify();
                            }
                        }

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
        mRecorder.setAudioSamplingRate(96000);

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

    private Double getAmplitudeDb() {
        double amp = getAmplitude();
        android.util.Log.w("[Monkey]", "p: " + Double.toString(amp));
        if(amp > 0)
        {
            return 20 * Math.log10(amp);
        }
        return null;
    }

    private double getAmplitude() {
        if (mRecorder != null)
            return mRecorder.getMaxAmplitude();
        else
            return 0;
    }


}
