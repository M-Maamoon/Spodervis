package com.example.moaaz.spodervis.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.moaaz.spodervis.MainActivity;
import com.example.moaaz.spodervis.R;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import java.util.Arrays;

/**
 * Created by Moaaz on 5/3/2017.
 */

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("lel","hrey");
        String title = intent.getStringExtra("title");
        String command = intent.getStringExtra("command");
        createNotification(context, title, command);
    }

    private void createNotification(Context context, String title, String command) {
        Log.i("title", title);
        Log.i("command", command);

        PendingIntent notifiIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class),0 );

        NotificationCompat.Builder nBuilder = new
                NotificationCompat.Builder(context);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(command);
        nBuilder.setSmallIcon(R.drawable.ic_add);
        nBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.spoderman));
        nBuilder.setContentIntent(notifiIntent);
        nBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        nBuilder.setAutoCancel(true);
        if (pubnubService.state)
        {
            pubnubService.pubnub.publish()
                    .message(Arrays.asList(command))
                    .channel("commands")
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            Log.i("PubNub result error", status.isError() + "");
                        }
                    });
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, nBuilder.build());
    }
}
