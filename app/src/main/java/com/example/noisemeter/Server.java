package com.example.noisemeter;
import com.example.noisemeter.messages.TimeStamp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public void ListenAndSendResponse() throws IOException, ClassNotFoundException, InterruptedException {
        ServerSocket ss = new ServerSocket(7777);
        Logger logger = Logger.instance();
        logger.i("ServerSocket awaiting connections...");

        Socket socket = ss.accept();
        socket.setTcpNoDelay(true);
        socket.setTrafficClass(0x10);
        logger.i("traffic class: " + socket.getTrafficClass());

        logger.i("Connection from " + socket + "!");
        while(true)
        {
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);//this blocks

            TimeStamp requestedAt = new TimeStamp();
            Object request =  objectInputStream.readObject();
            Serializable serializable =  MsgHandler.handleMsg(request,requestedAt);
            if(serializable != null)
            {
                logger.i("Sending response...");
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(serializable);
            }
            else
            {
                logger.i("Not sending response");
            }
        }
    }

}
