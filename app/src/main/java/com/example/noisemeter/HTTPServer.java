package com.example.noisemeter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;

public class HTTPServer {
    private WebServer server;

    public void start() {
        server = new WebServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.e("Httpd", "The server could not start.");
        }
        Log.e("Httpd", "Web server initialized.");
    }

    public void stop() {
        if (server != null)
            server.stop();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            return newFixedLengthResponse(new Gson().toJson(Global.instance().getResults()));

//            String msg = "<html><body><h1>Hello server</h1>\n";
//            return newFixedLengthResponse(msg + "</body></html>\n");
        }
    }
}
