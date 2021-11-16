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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.time.Instant;


public class NoiseMeter extends Activity {

    TextView mStatusView;
    ProgressBar mProgressBar;
    SoundDetector soundDetector;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noise_meter);
        mStatusView = (TextView) findViewById(R.id.status);
        mStatusView.setText("Waiting for sound...");
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        String filepath = cw.getExternalCacheDir() + File.separator + "record.3gp";

        long pollingRateMs = 1;
        int thresholdDb = 60;
//        soundDetector = new SoundDetector(filepath, thresholdDb, pollingRateMs);
//        soundDetector.executeOnThresholdReached(() ->
//                {
//                    android.util.Log.e("[Monkey]", "Executing...: ");
//                    this.runOnUiThread(new Runnable()
//                    {
//                        public void run() {
//                            showTimeDialog();
//                            mStatusView.setText("Done");
//                            mProgressBar.setVisibility(View.GONE);
//                        }
//                    });
//
//                }
//        );
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void showTimeDialog() {
        long millis = System.currentTimeMillis();
        String measuredAt = Instant.ofEpochMilli(millis).toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(NoiseMeter.this);
        builder.setCancelable(true);
        builder.setTitle("Threshold exceeded sadge :(");
        builder.setMessage(String.format("Threshold reached at %s", measuredAt));

        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        builder.show();
    }

}