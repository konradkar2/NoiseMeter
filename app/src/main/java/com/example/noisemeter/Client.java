package com.example.noisemeter;
import static android.content.Context.WIFI_SERVICE;
import static android.media.AudioManager.STREAM_MUSIC;
import static android.media.ToneGenerator.TONE_PROP_BEEP;

import android.content.Context;
import android.content.ContextWrapper;
import android.media.ToneGenerator;
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
    Socket socket;
    Logger logger;
    SoundDetector soundDetector;
    public Client(Context context) throws Exception {
        logger = Logger.instance();
        mContext = context;
        String ipAddress = getHotspotAdress();
        if(ipAddress.isEmpty())
        {
            throw new Exception("failed to get addresss");
        }
        socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, 7777), 1000);
        logger.i("Connected to" + ipAddress);

        logger.i("Initializing soundetector...");
        ContextWrapper cw = new ContextWrapper(mContext);
        String filepath = cw.getExternalCacheDir() + File.separator + "record.3gp";
        long pollingRateMs = 1;
        int thresholdDb = 40;
//        soundDetector = new SoundDetector(filepath, thresholdDb, pollingRateMs,new Runnable() {
//            @Override
//            public void run() {
//                logger.i("Got something in detector");
//                soundDetector.disable();
//            }
//        });

    }
    public void sendAndWaitForResponse() throws IOException, ClassNotFoundException {
        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        ArrayList tsSentAt = new ArrayList<Long>();
        ArrayList tsGotResponseAt = new ArrayList<Long>();
        ArrayList tsResponseTimestamp = new ArrayList<Long>();

        for (int i = 0; i < 10; i++) {
            logger.i("Sending request");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            tsSentAt.add(System.currentTimeMillis());
            objectOutputStream.writeObject(new GetTimestampReq());


            logger.i("waiting for response...");
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream); //this blocks
            tsGotResponseAt.add(System.currentTimeMillis());
            TimeStampMsg response = (TimeStampMsg) objectInputStream.readObject();
            tsResponseTimestamp.add(response.timestamp);
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
        double rttAvg = rttDiffSum/(double)size;
        double offsetAvg = (offsetSum/(double)size) - rttAvg;
        logger.i("Calculated rttAvg: " + rttAvg + " [ms] ");
        logger.i("Our avg offset to server is: " + offsetAvg + " [ms] ");

        for (int i = 0; i < 3; i++)
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            PlayAudioReq req = new PlayAudioReq();
            logger.i("Sending playAudioRequest");
            objectOutputStream.writeObject(req);
            //soundDetector.enable();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long tSentAt = System.currentTimeMillis();
            logger.i("waiting for response...");

            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            TimeStampMsg response = (TimeStampMsg) objectInputStream.readObject();
            //soundDetector.disable();
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
