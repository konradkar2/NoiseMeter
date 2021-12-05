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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Client {
    Context mContext;
    DatagramSocket mSocket;
    InetSocketAddress mServerAddress;
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
        mSocket = new DatagramSocket();
        mSocket.setReuseAddress(true);
        mSocket.setTrafficClass(0x10);
        mServerAddress = new InetSocketAddress(ipAddress,7777);

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
    public void sendAndWaitForResponse() throws IOException, ClassNotFoundException, InterruptedException {
        tsSoundDetectedAt = new ArrayList<TimeStamp>();

        List<TimeStamp> tsSentAt = new ArrayList<TimeStamp>();
        List<TimeStamp> tsGotResponseAt = new ArrayList<TimeStamp>();
        List<TimeStamp> tsResponseTimestamp = new ArrayList<TimeStamp>();

        for (int i = 0; i < 30; i++) {
            //mLogger.i("Sending request");
            tsSentAt.add(new TimeStamp());
            DatagramSocketIO.send(mSocket,mServerAddress,new GetTimestampReq());
            //mLogger.i("waiting for response...");
            ReceivedData receivedData = DatagramSocketIO.receive(mSocket);
            tsGotResponseAt.add(new TimeStamp());
            TimeStamp response = (TimeStamp) receivedData.getSerializable();
            tsResponseTimestamp.add(response);
        }

        List<Double> offsetList = new ArrayList<>();
        long size = tsSentAt.size();

        for(int i = 0 ; i< size;i++)
        {
            double rtt = tsGotResponseAt.get(i).get() - tsSentAt.get(i).get();
            rtt = rtt/2.0;
            mLogger.i("rtt:" + String.valueOf(rtt));
            double clockOffset = tsResponseTimestamp.get(i).get() - tsSentAt.get(i).get() + rtt;
            offsetList.add(clockOffset);
        }

        Collections.sort(offsetList);
        mLogger.i("offset list"+offsetList.toString());
        double clockOffsetAvg = ((offsetList.get(14) + offsetList.get(15))/2);

        mLogger.i("Our avg offset to server is: " + clockOffsetAvg + " [ms] ");
        List<TimeStamp> tsPlayedAt = new ArrayList<TimeStamp>();
        for (int i = 0; i < 5; i++)
        {

            mLogger.i("Sending playAudioRequest");
            DatagramSocketIO.send(mSocket,mServerAddress,new PlayAudioReq());
            mSoundDetector.enable();
            mLogger.i("waiting for response...");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ReceivedData receivedData = DatagramSocketIO.receive(mSocket);
            TimeStamp tPlayedAt = (TimeStamp) receivedData.getSerializable();
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
//                long rtt = tsPlayedAt.get(i).get() - tsSoundSentAt.get(i).get() - (long) clockOffsetAvg;
//                mLogger.i("Rtt: " + rtt);
                mLogger.i("clockOffsetAvg: " + clockOffsetAvg);
                mLogger.i("Probe #" + i);
                mLogger.e("Raw delay: " + rawOffset);
                double offset = (double) rawOffset + clockOffsetAvg;

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
