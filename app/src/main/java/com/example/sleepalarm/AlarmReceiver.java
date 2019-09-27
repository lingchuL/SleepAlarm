package com.example.sleepalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static android.os.SystemClock.sleep;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent){
        final Intent i=new Intent(context,MyService.class);
        Log.d("SleepAlarm","收到广播准备休眠");

        new Thread(new Runnable() {
            @Override
            public void run() {
                sleep((5*1000));               //等一会儿再回去唤醒service
                context.startService(i);
            }
        }).start();
        /*
        sleep((10*1000));               //等一会儿再回去唤醒service
        context.startService(i);
        */
    }
}
