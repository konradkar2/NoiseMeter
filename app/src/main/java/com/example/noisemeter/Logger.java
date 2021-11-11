package com.example.noisemeter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static volatile Logger mInstance;
    private IStoreLogs mStore;

    private Logger(){};
    public static Logger instance() {
        if(mInstance == null) {
            synchronized (Logger.class) {
                if (mInstance == null) {
                    mInstance = new Logger();
                }
            }
        }
        return mInstance;
    }
    public void e(String msg)
    {
        log("e: " + msg);
    }
    public void i(String msg)
    {
        log("i: " + msg);
    }
    private String getTime()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }
    private void log(String msg)
    {
        mStore.store(getTime() + " " + msg + "\n");
    }
    public void initialize(IStoreLogs store)
    {
        mStore = store;
    }
}
