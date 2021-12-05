package com.example.noisemeter;
import com.example.noisemeter.messages.TimeStamp;

import java.io.*;
import java.net.DatagramSocket;

public class Server {
    public void ListenAndSendResponse() throws IOException, ClassNotFoundException, InterruptedException {
        DatagramSocket socket = new DatagramSocket(7777);
        Logger logger = Logger.instance();
        logger.i("ServerSocket awaiting connections...");

        socket.setTrafficClass(0x10);
        logger.i("traffic class: " + socket.getTrafficClass());
        while(true)
        {
            ReceivedData receivedData = DatagramSocketIO.receive(socket);
            logger.i("Request from " + receivedData.getSender().toString() + "!");
            TimeStamp requestedAt = new TimeStamp();
            Serializable serializable =  MsgHandler.handleMsg(receivedData.getSerializable(),requestedAt);
            if(serializable != null)
            {
               DatagramSocketIO.send(socket,receivedData.getSender(),serializable);
            }
            else
            {
                logger.i("Not sending response");
            }
        }
    }

}
