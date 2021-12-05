package com.example.noisemeter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class DatagramSocketIO {
    public static void send(DatagramSocket datagramSocket, InetSocketAddress destination, Serializable serializable) throws IOException {
        ByteArrayOutputStream byteStream = new
                ByteArrayOutputStream(5000);
        ObjectOutputStream os = new ObjectOutputStream(new
                BufferedOutputStream(byteStream));
        os.flush();
        os.writeObject(serializable);
        os.flush();
        //retrieves byte array
        byte[] sendBuf = byteStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(
                sendBuf, sendBuf.length, destination);
        int byteCount = packet.getLength();
        datagramSocket.send(packet);
        os.close();
    }

    public static ReceivedData receive(DatagramSocket datagramSocket) throws IOException, ClassNotFoundException {
        byte[] recvBuf = new byte[5000];
        DatagramPacket packet = new DatagramPacket(recvBuf,
                recvBuf.length);
        datagramSocket.receive(packet);
        int byteCount = packet.getLength();

        ByteArrayInputStream byteStream = new
                ByteArrayInputStream(recvBuf);
        ObjectInputStream is = new
                ObjectInputStream(new BufferedInputStream(byteStream));
        Object obj = is.readObject();
        is.close();

        return new ReceivedData((Serializable) obj, new InetSocketAddress(packet.getAddress(),packet.getPort()));
    }
}
