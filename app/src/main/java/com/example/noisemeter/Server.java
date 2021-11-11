package com.example.noisemeter;
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
            RtpMsg msg = (RtpMsg) objectInputStream.readObject();


            logger.i("Sending response...");
            OutputStream outputStream = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            msg.t = System.currentTimeMillis();
            objectOutputStream.writeObject(msg);
            logger.i("Sent " + msg.t);
        }


        logger.i("Closing sockets.");
        ss.close();
        socket.close();
    }

}
