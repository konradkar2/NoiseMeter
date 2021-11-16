package com.example.noisemeter;

import android.content.ContextWrapper;
import android.media.MediaRecorder;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

public class SoundDetector {
    private MediaRecorder mRecorder;
    private String mFilepath;
    private int mThresholdDb;
    private long mPollingIntervalMs;

    private Boolean enabled = false;
    private final Object lock = new Object();

    public SoundDetector(String filepath, int thresholdDb, long pollingIntervalMs, Runnable runnable) {
        mFilepath = filepath;
        mThresholdDb = thresholdDb;
        mPollingIntervalMs = pollingIntervalMs;

        executeOnThresholdReached(runnable);
    }

    public void enable() {
        synchronized (lock) {
            enabled = true;
        }
    }

    public void disable() {
        synchronized (lock) {
            enabled = false;
        }
    }

    private void executeOnThresholdReached(Runnable runnable) {
        initializeRecorder();
        startRecording();
        waitForThresholdAndExecute(runnable);
    }

    private void waitForThresholdAndExecute(Runnable work) {
        CompletableFuture.runAsync(() ->
        {
            while (true) {
                synchronized (lock) {
                    if (enabled)
                        if (getAmplitudeDb() > mThresholdDb) {
                            work.run();
                        }
                }
                try {
                    Thread.sleep(mPollingIntervalMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void initializeRecorder() {
        File file = new File(mFilepath);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
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
