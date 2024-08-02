package com.example.testrun.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.testrun.service.MyService;

public class PlaySongReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
//            context.sendBroadcast(new Intent(intent.getAction()));
//            if(intent.getAction().equals("PAUSE")) {
//                Log.d("PlaySongReceivedr",intent.getAction());
//                Intent intent1 = new Intent(context.getApplicationContext(), MyService.class);
//                intent1.setAction(intent.getAction());
//                context.startService(intent1);
//            }

        }
    }
}

