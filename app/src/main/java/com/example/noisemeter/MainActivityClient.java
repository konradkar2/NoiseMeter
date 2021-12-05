package com.example.noisemeter;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;


public class MainActivityClient extends AppCompatActivity {
    TextView mTextViewLogClient;
    LogStoreTextView logStoreTextView;
    ScrollView mScrollView;
    Button mBtnTest;
    Client client;
    SoundDetector mSoundDetector;
    HTTPServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_client);

        server = new HTTPServer();
        server.start();

        mBtnTest = (Button) findViewById(R.id.btnTest);
        mScrollView = (ScrollView) findViewById(R.id.scrollViewLog);
        mTextViewLogClient = (TextView) findViewById(R.id.textViewLog);
        logStoreTextView = new LogStoreTextView(mTextViewLogClient);

        Logger.instance().initialize(logStoreTextView);

        ContextWrapper cw = new ContextWrapper(this);
        String filepath = cw.getExternalCacheDir() + File.separator + "record.3gp";
        long pollingRateMs = 1;
        int thresholdDb = 33;
       
        mSoundDetector = new SoundDetector(filepath, thresholdDb, pollingRateMs);

        mTextViewLogClient.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                // you can add a toast or whatever you want here
            }

        });

        mBtnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnTest.setEnabled(false);
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
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mBtnTest.post(new Runnable() {
                            @Override
                            public void run() {
                                mBtnTest.setEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        server.stop();
    }

}