package com.kaist.ninjas.cs408ninjas;

import android.util.Log;

import java.util.TimerTask;

public class IntervalBackgroundTask extends TimerTask {
    @Override
    public void run() {
        Log.i("INTERVAL JOB", "started");
    }
}
