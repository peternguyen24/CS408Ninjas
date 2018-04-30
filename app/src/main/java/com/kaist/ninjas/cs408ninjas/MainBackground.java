package com.kaist.ninjas.cs408ninjas;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainBackground extends Service {
    public static final String MAIN_BACKGROUND = "com.kaist.ninjas.cs408ninjas.MAIN_BACKGROUND";

    private BroadcastReceiver backgroundReceiver;
    private IntervalBackgroundTask intervalTask;

    private Timer timer;
    public MainBackground() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        receiverInit();
        bindNotification();
        backgroundStart();
    }

    private void receiverInit() {
        this.backgroundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MainBackground.this.stopSelf();
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SEND);
        this.registerReceiver(this.backgroundReceiver, intentFilter);
    }

    private void bindNotification() {
        Intent broadcastIntent = new Intent(Intent.ACTION_SEND);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("MAIN BG")
                .setContentText("Time now is " + Calendar.getInstance().getTime().toString())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(100, noti);
        }
    }

    private void backgroundStart() {
        Log.i("BACKGROUND", "created");
        timer = new Timer();
        intervalTask = new IntervalBackgroundTask();
        timer.scheduleAtFixedRate(intervalTask, 0, 200);
    }

    private void backgroundStop() {
        timer.cancel();
    }

    @Override
    public void onDestroy() {
        backgroundStop();
        this.unregisterReceiver(this.backgroundReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Never bind this service
        return null;
    }
}
