package com.example.noisemeter;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.ToneGenerator.TONE_DTMF_1;
import static android.media.ToneGenerator.TONE_PROP_BEEP;

import android.media.ToneGenerator;

import com.example.noisemeter.messages.GetTimestampReq;
import com.example.noisemeter.messages.PlayAudioReq;
import com.example.noisemeter.messages.TimeStamp;

import java.io.Serializable;

public class MsgHandler {
    public static Serializable handleMsg(Object obj, TimeStamp requestedAt)
    {
        if(obj instanceof GetTimestampReq){
            return requestedAt;
        }
        if(obj instanceof PlayAudioReq){
            ToneGenerator toneGenerator = new ToneGenerator(STREAM_MUSIC,100);
            TimeStamp response =  new TimeStamp();

            int playForMs = 200;
            //this runs on new thread, to be replaced
            toneGenerator.startTone(TONE_DTMF_1,playForMs);
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
