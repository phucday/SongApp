package com.example.testrun.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "channel_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Channel Service Music", NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null,null);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
