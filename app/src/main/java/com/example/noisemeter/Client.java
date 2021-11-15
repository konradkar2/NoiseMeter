package com.example.noisemeter;
import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.example.noisemeter.messages.GetTimestampReq;
import com.example.noisemeter.messages.TimeStampMsg;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class Client {
    Context mContext;
    public Client(Context context)
    {
        mContext = context;
    }
    public void sendAndWaitForResponse() throws IOException, ClassNotFoundException {
        Logger logger = Logger.instance();
        String ipAddress = getHotspotAdress();
        if(ipAddress.isEmpty())
        {
            return;
        }
        Socket socket = new Socket(ipAddress, 7777);
        logger.i("Connected to" + ipAddress);

        OutputStream outputStream = socket.getOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        GetTimestampReq req = new GetTimestampReq();

        logger.i("Sending request");
        objectOutputStream.writeObject(req);
        long tSentAt = System.currentTimeMillis();
        logger.i("waiting for response...");

        InputStream inputStream = socket.getInputStream(); //this blocks
        long tGotResponseAt = System.currentTimeMillis();

        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        TimeStampMsg response = (TimeStampMsg)objectInputStream.readObject();

        socket.close();
        logger.i("Received message");
        logger.i("Sent request at: " + tSentAt);
        logger.i("Server's timestamp: " + response.timestamp);
        logger.i("Got response at: " + tGotResponseAt);


        double rtt = (tGotResponseAt - tSentAt)/2;
        logger.i("Calculated rtt: " + rtt + " [ms] ");


        double tOffset = tSentAt - response.timestamp - rtt;
        logger.i("Our offset to server is: " + tOffset + " [ms] ");

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
