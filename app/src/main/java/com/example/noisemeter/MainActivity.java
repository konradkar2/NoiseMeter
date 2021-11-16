package com.example.noisemeter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public class MainActivity extends AppCompatActivity {
    TextView mTextView;
    LogStoreTextView logStoreTextView;
    Button mStartServerButton;
    Button mSyncClientButton;
    Client client;
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

        mStartServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartServerButton.setEnabled(false);
                CompletableFuture.runAsync(() ->
                {
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
                });
            }
        });

        mSyncClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSyncClientButton.setEnabled(false);
                CompletableFuture.runAsync(() ->
                {

                    try {
                        if(client == null) {
                            client = new Client(v.getContext());
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
                });
            }
        });
    }
}