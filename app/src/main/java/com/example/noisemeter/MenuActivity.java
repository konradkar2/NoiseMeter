package com.example.noisemeter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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