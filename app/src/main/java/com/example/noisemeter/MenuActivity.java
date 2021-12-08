package com.example.noisemeter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {
    private Button mBtnServer;
    private Button mBtnClient;
    private Button mBtnNoiseMeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mBtnServer = (Button) findViewById(R.id.btnChooseServer);
        mBtnServer.setOnClickListener(v -> openMainServer());

        mBtnClient = (Button) findViewById(R.id.btnChooseClient);
        mBtnClient.setOnClickListener(v -> openMainClient());

        mBtnNoiseMeter = (Button) findViewById(R.id.btnNoiseMeter);
        mBtnNoiseMeter.setOnClickListener(v -> openNoiseMeter());
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_WIFI_STATE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        boolean hasLowLatencyFeature =
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);

        boolean hasProFeature =
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);

        Log.e("[LowLatency]", "Low latency: " + String.valueOf(hasLowLatencyFeature));
        Log.e("[LowLatency]", "System feture: " + String.valueOf(hasProFeature));

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String sampleRateStr = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int sampleRate = Integer.parseInt(sampleRateStr);
        if (sampleRate == 0) sampleRate = 44100; // Use a default value if property not found

        Log.e("[Latency]", "Optimal sampling: " + sampleRate);

        String framesPerBuffer = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int framesPerBufferInt = Integer.parseInt(framesPerBuffer);
        if (framesPerBufferInt == 0) framesPerBufferInt = 256; // Use default

        Log.e("[Latency]", "Optimal buffer: " + framesPerBufferInt);

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void openMainServer() {
        Intent intent = new Intent(this, MainActivityServer.class);
        startActivity(intent);
    }

    public void openMainClient() {
        Intent intent = new Intent(this, MainActivityClient.class);
        startActivity(intent);
    }
    public void openNoiseMeter() {
        Intent intent = new Intent(this, NoiseMeter.class);
        startActivity(intent);
    }

}