package com.example.noisemeter.messages;

import java.io.Serializable;

public class TimeStamp implements Serializable {
    private long mValue;
    public TimeStamp()
    {
        mValue = System.currentTimeMillis();
    }
    public long get()
    {
        return mValue;
    }

}
