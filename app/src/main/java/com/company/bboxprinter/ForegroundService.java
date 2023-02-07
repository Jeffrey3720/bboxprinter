package com.company.bboxprinter;/*
    Created by Jeffrey of Blue-fly  on $(DATE)
*/

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class ForegroundService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Log.v("Jeffrey"," Foreground runing...");

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        Log.v("Jeffrey"," Foreground stop.");

    }

    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Intent activityIntent = new Intent(this, MainActivity.class);
        Log.v("Jeffrey"," Foreground runing...");

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(), 0, activityIntent, 0);
        Notification notification = new Notification.Builder(getApplication()).setAutoCancel(true).
                setSmallIcon(R.drawable.ic_launcher_foreground).setTicker("前台Service啟動").setContentTitle("前台Service運行中").
                setContentText("這是一個正在運行的前台 Service").setWhen(System.currentTimeMillis()).setContentIntent(pendingIntent).build();

        startForeground(1, notification);

        return START_STICKY;
    }

     */


}
