package com.example.noisemeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.collection.CircularArray;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.File;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;


public class NoiseMeter extends Activity {

    AnyChartView anyChartView;
    SoundDetector mSoundDetector;

    public List<DataEntry> queueTolist(Queue<Double> queue)
    {
        List<DataEntry> list = new ArrayList<DataEntry>();
        int i =0;
        Iterator<Double> itr = queue.iterator();
        while(itr.hasNext())
        {
            list.add(new ValueDataEntry(i,itr.next()));
            i = i +1;
        }
        return list;
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.noise_meter);
        anyChartView = (AnyChartView) findViewById(R.id.any_chart_view);

        Cartesian cartesian = AnyChart.line();
        cartesian.yScale().maximum(100);
        anyChartView.setChart(cartesian);
        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        Random random = new Random();
        Queue<Double> fifo = new CircularFifoQueue<Double>(50);

        ContextWrapper cw = new ContextWrapper(this);
        String filepath = cw.getExternalCacheDir() + File.separator + "record.3gp";
        long pollingRateMs = 20;
        int thresholdDb = 1;

        mSoundDetector = new SoundDetector(filepath, thresholdDb, pollingRateMs);
        mSoundDetector.enable();
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    double amplitudeDb = 0.0;
                    try {
                        amplitudeDb = mSoundDetector.waitForSound();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    fifo.add(amplitudeDb);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            List<DataEntry> list = queueTolist(fifo);
                            cartesian.data(list);
                        }
                    });
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();


    }


}