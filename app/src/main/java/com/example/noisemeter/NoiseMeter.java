package com.example.noisemeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.time.Instant;


public class NoiseMeter extends Activity {

    TextView mStatusView;
    boolean alert = false;
    MediaRecorder mRecorder;
    Thread runner;
    private static double MAX_AMPLITUDE = 32767.0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detectSound();
    }

    public void onResume() {
        super.onResume();
        startRecorder();
    }

    public void onPause() {
        super.onPause();
        stopRecorder();
    }

    public void detectSound() {
        setContentView(R.layout.noise_meter);
        mStatusView = (TextView) findViewById(R.id.status);

        runner = new Thread(() -> {
            while (runner != null) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("Noise", "Tock");
                runOnUiThread(this::updateTv);
            }
        });

        runner.start();
        Log.d("Noise", "start runner()");
    }

    public void startRecorder() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        String filepath = cw.getExternalCacheDir() + File.separator + "record.3gp";
        File file = new File(filepath);
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(filepath);
            try {
                mRecorder.prepare();

            } catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
            try {
                mRecorder.start();
            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            } catch (IllegalStateException e) {
                android.util.Log.e("[Monkey]", "IllegalStateException: " + android.util.Log.getStackTraceString(e));
            } catch (Exception e) {
                android.util.Log.e("[Monkey]", "Exception: " + android.util.Log.getStackTraceString(e));
            }


            //mEMA = 0.0;
        }

    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void updateTv(){
        double amplitude = getAmplitudeDb();

        if (amplitude > 70 && !alert) {
            long millis = System.currentTimeMillis();
            String measuredAt = Instant.ofEpochMilli(millis).toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(NoiseMeter.this);

            alert = true;
            builder.setCancelable(true);
            builder.setTitle("Threshold exceeded sadge");
            builder.setMessage(String.format("Measured %.0f dB at %s", amplitude, measuredAt));

            builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    alert = false;
                }
            });

            builder.show();

        }

        mStatusView.setText(amplitude + " dB");
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return mRecorder.getMaxAmplitude();
        else
            return 0;
    }

    public double getAmplitudeDb() {
        double amp = getAmplitude();
        android.util.Log.w("[Monkey]", "p: " + Double.toString(amp));
        return 20 * Math.log10(amp);
    }

}