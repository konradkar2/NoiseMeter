package com.example.noisemeter;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class ReceivedData {
    private Serializable serializable;
    private InetSocketAddress sender;

    public Serializable getSerializable() {
        return serializable;
    }
    public InetSocketAddress getSender() {
        return sender;
    }

    public ReceivedData(Serializable serializable, InetSocketAddress sender) {
        this.serializable = serializable;
        this.sender = sender;
    }
}
