package com.example.noisemeter;
import com.example.noisemeter.messages.GetTimestampReq;
import com.example.noisemeter.messages.TimeStampMsg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public void ListenAndSendResponse() throws IOException, ClassNotFoundException {
        ServerSocket ss = new ServerSocket(7777);
        Logger logger = Logger.instance();
        logger.i("ServerSocket awaiting connections...");
        Socket socket = ss.accept();
        socket.setTcpNoDelay(true);

        logger.i("Connection from " + socket + "!");
        for(int i = 0; i < 1000 ; i++)
        {
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);//this blocks
            long requestedAt = System.currentTimeMillis();
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


        logger.i("Closing sockets.");
        ss.close();
        socket.close();
    }

}
