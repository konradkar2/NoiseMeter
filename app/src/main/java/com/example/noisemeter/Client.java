package com.example.noisemeter;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.example.noisemeter.messages.GetTimestampReq;
import com.example.noisemeter.messages.PlayAudioReq;
import com.example.noisemeter.messages.TimeStampMsg;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Client {
    Context mContext;
    Socket mSocket;
    Logger mLogger;
    SoundDetector mSoundDetector;
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
                mSoundDetector.disable();
            }
        });

    }
    public void sendAndWaitForResponse() throws IOException, ClassNotFoundException {
        OutputStream outputStream = mSocket.getOutputStream();
        InputStream inputStream = mSocket.getInputStream();

        ArrayList tsSentAt = new ArrayList<Long>();
        ArrayList tsGotResponseAt = new ArrayList<Long>();
        ArrayList tsResponseTimestamp = new ArrayList<Long>();

        for (int i = 0; i < 30; i++) {
            //logger.i("Sending request");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            long tSentAt = System.currentTimeMillis();
            objectOutputStream.writeObject(new GetTimestampReq());


            //logger.i("waiting for response...");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream); //this blocks
            long tGotResponseAt = System.currentTimeMillis();
            TimeStampMsg response = (TimeStampMsg) objectInputStream.readObject();
            tsResponseTimestamp.add(response.timestamp);
            tsSentAt.add(tSentAt);
            tsGotResponseAt.add(tGotResponseAt);
        }
        long rttDiffSum = 0;
        long offsetSum = 0;
        long size = tsSentAt.size();
        for(int i = 0 ; i< size;i++)
        {
            rttDiffSum = rttDiffSum + ((Long)tsGotResponseAt.get(i)).longValue() - ((Long)tsSentAt.get(i)).longValue();
        }
        for(int i = 0 ; i< size;i++)
        {
            offsetSum =  offsetSum + ((Long)tsResponseTimestamp.get(i)).longValue() - ((Long)tsSentAt.get(i)).longValue();
        }
        double rttAvg = rttDiffSum/(double)size*2;
        double offsetAvg = (offsetSum/(double)size) - rttAvg;
        mLogger.i("Calculated rttAvg: " + rttAvg + " [ms] ");
        mLogger.i("Our avg offset to server is: " + offsetAvg + " [ms] ");

        for (int i = 0; i < 3; i++)
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            PlayAudioReq req = new PlayAudioReq();
            mLogger.i("Sending playAudioRequest");
            objectOutputStream.writeObject(req);
            mSoundDetector.enable();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long tSentAt = System.currentTimeMillis();
            mLogger.i("waiting for response...");

            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            TimeStampMsg response = (TimeStampMsg) objectInputStream.readObject();
            mSoundDetector.disable();
            long tGotResponseAt = System.currentTimeMillis();
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
