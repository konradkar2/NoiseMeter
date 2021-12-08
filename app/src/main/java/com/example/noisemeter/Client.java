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
    boolean canAddSoundDetectedAtTs = true;

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
        mSocket.setSoTimeout(5000);
        mSocket.setTcpNoDelay(true);
        mSocket.setTrafficClass(0x10);
        mSocket.setPerformancePreferences(1,0,2);
        mSocket.connect(new InetSocketAddress(ipAddress, 7777), 1000);
        mLogger.i("Connected to" + ipAddress);

        mLogger.i("setting work to sound detector...");
    }
    public void sendAndWaitForResponse() throws IOException, ClassNotFoundException, InterruptedException {
        tsSoundDetectedAt = new ArrayList<TimeStamp>();
        OutputStream outputStream = mSocket.getOutputStream();
        InputStream inputStream = mSocket.getInputStream();

        List<TimeStamp> tsSentAt = new ArrayList<TimeStamp>();
        List<TimeStamp> tsGotResponseAt = new ArrayList<TimeStamp>();
        List<TimeStamp> tsResponseTimestamp = new ArrayList<TimeStamp>();
        Results res = new Results();

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

        List<Double> offsetList = new ArrayList<>();
        long size = tsSentAt.size();

        res.rtts = new ArrayList<Double>();
        double bestRtt = 10000.0;
        int bestOffsetIdx = 0;
        for(int i = 0 ; i< size;i++)
        {
            double rtt = tsGotResponseAt.get(i).get() - tsSentAt.get(i).get();
            rtt = rtt/2.0;
            res.rtts.add(rtt);
            if(rtt < bestRtt) {
                bestOffsetIdx = i;
                bestRtt = rtt;
            }
            mLogger.i("rtt:" + String.valueOf(rtt));
            double clockOffset = tsResponseTimestamp.get(i).get() - tsSentAt.get(i).get() + rtt;
            offsetList.add(clockOffset);
        }

        res.offsets = offsetList;
        double clockOffsetBest = offsetList.get(bestOffsetIdx);

        mLogger.i("Our offset to server is: " + clockOffsetBest + " [ms] ");
        List<TimeStamp> tsPlayedAt = new ArrayList<TimeStamp>();
        for (int i = 0; i < 5; i++)
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            PlayAudioReq req = new PlayAudioReq();
            mLogger.i("Sending playAudioRequest");
            objectOutputStream.writeObject(req);

            mSoundDetector.enable();
            try{
                mSoundDetector.waitForSound();
                tsSoundDetectedAt.add(new TimeStamp());
            }
            catch (Exception e)
            {
                tsSoundDetectedAt.add(null);
            }
            mLogger.i("waiting for response...");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            TimeStamp tPlayedAt = (TimeStamp) objectInputStream.readObject();
            tsPlayedAt.add(tPlayedAt);
            mSoundDetector.waitForNoSound();
            mSoundDetector.disable();
        }


        res.clockOffset = clockOffsetBest;
        res.rawDelays = new ArrayList<Double>();

        if(tsPlayedAt.size() != tsSoundDetectedAt.size())
        {
            mLogger.e("Not all signals were detected...");
        }
        else
        {
            for(int i = 0; i<tsSoundDetectedAt.size(); i++)
            {
                if(tsSoundDetectedAt.get(i) == null){
                    mLogger.i("Probe #" + i + " sound not detected");
                    continue;
                }
                long rawOffset = tsSoundDetectedAt.get(i).get() - tsPlayedAt.get(i).get();
//                long rtt = tsPlayedAt.get(i).get() - tsSoundSentAt.get(i).get() - (long) clockOffsetAvg;
//                mLogger.i("Rtt: " + rtt);
                mLogger.i("Probe #" + i);
                mLogger.e("Raw delay: " + rawOffset);
                res.rawDelays.add((double) rawOffset);
                double offset = (double) rawOffset + clockOffsetBest;

                mLogger.e("delay: " + offset);
                res.delays.add(offset);
            }
        }

        Global.instance().setResults(res);
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
