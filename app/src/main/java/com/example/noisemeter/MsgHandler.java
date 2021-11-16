package com.example.noisemeter;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.ToneGenerator.TONE_PROP_BEEP;

import android.media.ToneGenerator;

import com.example.noisemeter.messages.GetTimestampReq;
import com.example.noisemeter.messages.PlayAudioReq;
import com.example.noisemeter.messages.TimeStampMsg;

import java.io.Serializable;

public class MsgHandler {
    public static Serializable handleMsg(Object obj, long requestAt)
    {
        if(obj instanceof GetTimestampReq){
            TimeStampMsg response =  new TimeStampMsg();
            response.timestamp = requestAt;
            return response;
        }
        if(obj instanceof PlayAudioReq){
            ToneGenerator toneGenerator = new ToneGenerator(STREAM_MUSIC,100);
            TimeStampMsg response =  new TimeStampMsg();

            int playForMs = 100;
            response.timestamp = System.currentTimeMillis();
            //this runs on new thread, to be replaced
            toneGenerator.startTone(TONE_PROP_BEEP,playForMs);
            try {
                //simulate blocking
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        }
        return null;
    }
}
