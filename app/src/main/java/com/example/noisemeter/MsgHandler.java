package com.example.noisemeter;

import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.ToneGenerator.TONE_DTMF_9;

import android.media.ToneGenerator;

import com.example.noisemeter.messages.GetTimestampReq;
import com.example.noisemeter.messages.PlayAudioReq;
import com.example.noisemeter.messages.TimeStamp;

import java.io.Serializable;

public class MsgHandler  {
    private static final ToneGenerator toneGenerator = new ToneGenerator(STREAM_MUSIC,100);

    public static Serializable handleMsg(Object obj, TimeStamp requestedAt) throws InterruptedException {
        if(obj instanceof GetTimestampReq){
            return requestedAt;
        }
        if(obj instanceof PlayAudioReq){

            int playForMs = 100;
//            Thread.sleep(1000);
            //this runs on new thread, to be replaced
            TimeStamp response =  new TimeStamp();
            toneGenerator.startTone(TONE_DTMF_9,playForMs);
            try {
                //simulate blocking
                Thread.sleep(playForMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        }
        return null;
    }
}
