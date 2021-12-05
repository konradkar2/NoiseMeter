package com.example.noisemeter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


public class MainActivityServer extends AppCompatActivity {
    ScrollView mScrollView;
    TextView mTextViewLogServer;
    LogStoreTextView logStoreTextView;
    Button mBtnRestartServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_server);

        mTextViewLogServer = (TextView) findViewById(R.id.textViewLog);
        logStoreTextView = new LogStoreTextView(mTextViewLogServer);
        mBtnRestartServer = (Button) findViewById(R.id.btn1);
        mScrollView = (ScrollView) findViewById(R.id.scrollViewLog);

        Logger.instance().initialize(logStoreTextView);

        mBtnRestartServer.setEnabled(false);
        startServer();

        mBtnRestartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServer();
            }
        });

        mTextViewLogServer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable arg0) {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    public void startServer() {
        new Thread(() -> {
            Server server = new Server();
            try {
                server.ListenAndSendResponse();
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
            mBtnRestartServer.post(() -> mBtnRestartServer.setEnabled(true));
        }).start();
    }
}