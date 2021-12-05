package com.example.noisemeter;

public interface IHandleAmplitude {
    void handle(double amplitudeDb, boolean thresholdReached);
}
