package com.example.noisemeter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {
    TextView mTextView;
    LogStoreTextView logStoreTextView;
    Button mStartServerButton;
    Button mSyncClientButton;
    Client client;
    SoundDetector mSoundDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.textViewLog);
        logStoreTextView = new LogStoreTextView(mTextView);
        Logger.instance().initialize(logStoreTextView);
        Logger.instance().i("Test");

        mStartServerButton = (Button) findViewById(R.id.syncServer);
        mSyncClientButton = (Button) findViewById(R.id.syncClient);

        ContextWrapper cw = new ContextWrapper(this);
        String filepath = cw.getExternalCacheDir() + File.separator + "record.3gp";
        long pollingRateMs = 1;
        int thresholdDb = 40;
        mSoundDetector = new SoundDetector(filepath, thresholdDb, pollingRateMs);

        mStartServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartServerButton.setEnabled(false);
                mSyncClientButton.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Server server = new Server();
                        try {
                            server.ListenAndSendResponse();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        mStartServerButton.post(new Runnable() {
                            @Override
                            public void run() {
                                mStartServerButton.setEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        });

        mSyncClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSyncClientButton.setEnabled(false);
                mStartServerButton.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(client == null) {
                                client = new Client(v.getContext(),mSoundDetector);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            client.sendAndWaitForResponse();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        mSyncClientButton.post(new Runnable() {
                            @Override
                            public void run() {
                                mSyncClientButton.setEnabled(true);
                            }
                        });
                    }
                }).start();

            }
        });
    }
}