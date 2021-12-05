package com.example.noisemeter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Results implements Serializable {
    public double clockOffset;
    public List<Double> rtts = new ArrayList<>();
    public List<Double> offsets = new ArrayList<>();
    public List<Double> rawDelays = new ArrayList<>();
    public List<Double> delays = new ArrayList<>();
};