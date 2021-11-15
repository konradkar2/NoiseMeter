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
        Socket socket = null;
        for(int i =0 ; i < 100 ; i++)
        {
            logger.i("ServerSocket awaiting connections...");
            socket = ss.accept();
            logger.i("Connection from " + socket + "!");

            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            GetTimestampReq request = (GetTimestampReq) objectInputStream.readObject();


            logger.i("Sending response...");
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            TimeStampMsg response =  new TimeStampMsg();
            response.timestamp = System.currentTimeMillis();

            objectOutputStream.writeObject(response);
            logger.i("Sent " + response.timestamp);
        }


        logger.i("Closing sockets.");
        ss.close();
        socket.close();
    }

}
