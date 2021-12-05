package com.example.noisemeter;

import java.io.Serializable;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Global {
    private static volatile Global mInstance;
    private Results mStore;

    private Global(){};
    public static Global instance() {
        if(mInstance == null) {
            synchronized (Global.class) {
                if (mInstance == null) {
                    mInstance = new Global();
                }
            }
        }
        return mInstance;
    }

    public Results getResults()
    {
        return mStore;
    }

    public void setResults(Results res) {
        mStore = res;
    }
}



