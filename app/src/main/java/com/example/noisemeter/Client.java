package com.example.noisemeter;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.example.noisemeter.messages.GetTimestampReq;
import com.example.noisemeter.messages.PlayAudioReq;
import com.example.noisemeter.messages.TimeStamp;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client {
    Context mContext;
    Socket mSocket;
    Logger mLogger;
    SoundDetector mSoundDetector;
    List<TimeStamp> tsSoundDetectedAt;
    public Client(Context context, SoundDetector soundDetector) throws Exception {
        mLogger = Logger.instance();
        mContext = context;
        this.mSoundDetector = soundDetector;
        String ipAddress = getHotspotAdress();
        if(ipAddress.isEmpty())
        {
            throw new Exception("failed to get addresss");
        }
        mSocket = new Socket();
        mSocket.setTcpNoDelay(true);
        mSocket.connect(new InetSocketAddress(ipAddress, 7777), 1000);
        mLogger.i("Connected to" + ipAddress);

        mLogger.i("setting work to sound detector...");
        mSoundDetector.setWork(new Runnable() {
            @Override
            public void run() {
                mLogger.i("Got something in detector");
                tsSoundDetectedAt.add(new TimeStamp());
                mSoundDetector.disable();
            }
        });

    }
    public void sendAndWaitForResponse() throws IOException, ClassNotFoundException {
        tsSoundDetectedAt = new ArrayList<TimeStamp>();
        OutputStream outputStream = mSocket.getOutputStream();
        InputStream inputStream = mSocket.getInputStream();

        List<TimeStamp> tsSentAt = new ArrayList<TimeStamp>();
        List<TimeStamp> tsGotResponseAt = new ArrayList<TimeStamp>();
        List<TimeStamp> tsResponseTimestamp = new ArrayList<TimeStamp>();

        for (int i = 0; i < 30; i++) {
            //logger.i("Sending request");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            tsSentAt.add(new TimeStamp());
            objectOutputStream.writeObject(new GetTimestampReq());


            //logger.i("waiting for response...");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream); //this blocks
            tsGotResponseAt.add(new TimeStamp());
            TimeStamp response = (TimeStamp) objectInputStream.readObject();
            tsResponseTimestamp.add(response);
        }
        long rttDiffSum = 0;
        List<Long> rttDiffList = new ArrayList<>();
        long offsetSum = 0;
        List<Long> offsetList = new ArrayList<>();
        long size = tsSentAt.size();

        for(int i = 0 ; i< size;i++)
        {
            long val = tsGotResponseAt.get(i).get() - tsSentAt.get(i).get();
            rttDiffSum = rttDiffSum + val;
            rttDiffList.add(val);
        }

        for(int i = 0 ; i< size;i++)
        {
            long val = tsResponseTimestamp.get(i).get() - tsSentAt.get(i).get();
            offsetSum =  offsetSum + val;
            offsetList.add(val);
        }


        Collections.sort(rttDiffList);
        Collections.sort(offsetList);

//        double rttAvg = rttDiffSum/(double)size*2;
//        double clockOffsetAvg = (offsetSum/(double)size) - rttAvg;

        double rttAvg = (float) (rttDiffList.get(14) + rttDiffList.get(15))/4;
        double clockOffsetAvg = (float) ((offsetList.get(14) + offsetList.get(15))/2) - rttAvg;


        mLogger.i("Calculated rttAvg: " + rttAvg + " [ms] ");
        mLogger.i("Our avg offset to server is: " + clockOffsetAvg + " [ms] ");
//        mLogger.i("rttDiffList: " + rttDiffList);
//        mLogger.i("offsetList: " + offsetList);
//        mLogger.i("newRttAvg: " + newRttAvg);
//        mLogger.i("newOffset: " + newOffset);


        List<TimeStamp> tsPlayedAt = new ArrayList<TimeStamp>();
        for (int i = 0; i < 5; i++)
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            PlayAudioReq req = new PlayAudioReq();

            mLogger.i("Sending playAudioRequest");
            objectOutputStream.writeObject(req);
            mSoundDetector.enable();
            long tSentAt = System.currentTimeMillis();
            mLogger.i("waiting for response...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            TimeStamp tPlayedAt = (TimeStamp) objectInputStream.readObject();
            tsPlayedAt.add(tPlayedAt);
            mSoundDetector.disable();
        }
        if(tsPlayedAt.size() != tsSoundDetectedAt.size())
        {
            mLogger.e("Not all signals were detected...");
        }
        else
        {
            for(int i = 0; i<tsSoundDetectedAt.size(); i++)
            {
                long rawOffset = tsSoundDetectedAt.get(i).get() - tsPlayedAt.get(i).get();
                mLogger.i("Probe #" + i);
                mLogger.e("Raw delay: " + rawOffset);
                double offset = (double) rawOffset - clockOffsetAvg;
                mLogger.e("delay: " + offset);
            }
        }


    }

    private String getHotspotAdress(){
        final WifiManager manager = (WifiManager)mContext.getSystemService(WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        int ipAddress = dhcp.gateway;
        ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ?
                Integer.reverseBytes(ipAddress) : ipAddress;
        byte[] ipAddressByte = BigInteger.valueOf(ipAddress).toByteArray();
        try {
            InetAddress myAddr = InetAddress.getByAddress(ipAddressByte);
            return myAddr.getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            Logger.instance().e("Error getting Hotspot IP address ");
        }
        return "";
    }


}
