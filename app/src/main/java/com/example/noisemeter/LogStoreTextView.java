package com.example.noisemeter;

import android.widget.TextView;

public class LogStoreTextView implements IStoreLogs {

    TextView mTextView;

    public LogStoreTextView(TextView textView) {
        mTextView = textView;
    }

    @Override
    public void store(String log) {
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                mTextView.append(log);
            }
        });
    }

}
