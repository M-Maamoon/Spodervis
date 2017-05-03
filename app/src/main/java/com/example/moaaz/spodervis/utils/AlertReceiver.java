package com.example.moaaz.spodervis.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.moaaz.spodervis.MainActivity;
import com.example.moaaz.spodervis.R;

/**
 * Created by Moaaz on 5/3/2017.
 */

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("lel","hrey");
        createNotification(context, "Times Up", "5 Seconds lmfao");


    }

    private void createNotification(Context context, String s, String s1) {


        PendingIntent notifiIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),0 );

        NotificationCompat.Builder notificBuilder = new
                NotificationCompat.Builder(context);
        notificBuilder.setContentTitle(s);
        notificBuilder.setContentText(s1 );
        notificBuilder.setTicker("ticker lmfao");
        notificBuilder.setSmallIcon(R.drawable.ic_add);

        notificBuilder.setContentIntent(notifiIntent);
        notificBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        notificBuilder.setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notificBuilder.build());
    }
}
