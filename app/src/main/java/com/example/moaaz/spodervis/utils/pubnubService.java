package com.example.moaaz.spodervis.utils;

/**
 * Created by Moaaz on 5/2/2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.moaaz.spodervis.R;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;

public class pubnubService extends Service {
    private PNConfiguration pnConfiguration;
    private PubNub pubnub;
    BroadcastReceiver receiver;
    public static boolean state = false;

    public void onCreate() {
        super.onCreate();
        state = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        configurePubNub();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void configurePubNub() {
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-6e10bdfe-1ad0-11e7-aca9-02ee2ddab7fe");
        pnConfiguration.setPublishKey("pub-c-bcba6aa9-1ae4-4658-bfbd-ab52df9adb44");
        pubnub = new PubNub(pnConfiguration);

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getOperation() != null) {
                    switch (status.getOperation()) {
                        case PNSubscribeOperation:
                        case PNUnsubscribeOperation:
                            switch (status.getCategory()) {
                                case PNConnectedCategory:
                                    // this is expected for a subscribe, this means there is no error or issue whatsoever
                                case PNReconnectedCategory:
                                    // this usually occurs if subscribe temporarily fails but reconnects. This means
                                    // there was an error but there is no longer any issue
                                case PNDisconnectedCategory:
                                    // this is the expected category for an unsubscribe. This means there
                                    // was no error in unsubscribing from everything
                                case PNUnexpectedDisconnectCategory:
                                    // this is usually an issue with the internet connection, this is an error, handle appropriately
                                case PNAccessDeniedCategory:
                                    // this means that PAM does allow this client to subscribe to this
                                    // channel and channel group configuration. This is another explicit error
                                default:
                                    // More errors can be directly specified by creating explicit cases for other
                                    // error categories of `PNStatusCategory` such as `PNTimeoutCategory` or `PNMalformedFilterExpressionCategory` or `PNDecryptionErrorCategory`
                            }

                        case PNHeartbeatOperation:
                            // heartbeat operations can in fact have errors, so it is important to check first for an error.
                            // For more information on how to configure heartbeat notifications through the status
                            // PNObjectEventListener callback, consult <link to the PNCONFIGURATION heartbeart config>
                            if (status.isError()) {
                                // There was an error with the heartbeat operation, handle here
                            } else {
                                // heartbeat operation was successful
                            }
                        default: {
                            // Encountered unknown status type
                        }
                    }
                } else {
                    // After a reconnection see status.getCategory()
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                Log.i("Message received:", message.getMessage().toString());
                final String m = message.getMessage().toString().replaceAll("\\[", "").replaceAll("\\]", "");
                createNotification(pubnubService.this, m);
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
        pubnub.subscribe().channels(Arrays.asList("door_cam")).execute();
        Log.i("Subscribed:", "Subscribed to channel door_cam through PubNub Service");
      //  createNotification(this, "lmao");

    }

    private void createNotification(Context context, String message) {
        IntentFilter filter = new IntentFilter();
        final String KEY_NO = "NO";
        final String KEY_YES = "YES";
        filter.addAction(KEY_NO);
        filter.addAction(KEY_YES);


        Intent noIntent = new Intent(KEY_NO);
        PendingIntent noPendingIntent = PendingIntent.getBroadcast(this, 0, noIntent, 0);

        Intent yesIntent = new Intent(KEY_YES);
        PendingIntent yesPendingIntent = PendingIntent.getBroadcast(this, 0, yesIntent, 0);

        NotificationCompat.Builder nBuilder = new
                NotificationCompat.Builder(context);
        nBuilder.setContentTitle("Front Door");
        nBuilder.setContentText(message);
        nBuilder.setSmallIcon(R.drawable.ic_lock);
        nBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_lock));

        nBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        nBuilder.addAction(0, "Yes", yesPendingIntent);
        nBuilder.addAction(0, "No", noPendingIntent);

        nBuilder.setAutoCancel(true);
        nBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, nBuilder.build());

       receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("Action", intent.getAction());
                if (intent.getAction().equals(KEY_YES)) {

                    Log.i("Received", "Click on YES Button");

                    pubnub.publish()
                            .message(Arrays.asList("test"))
                            .channel("test")
                            .async(new PNCallback<PNPublishResult>() {
                                @Override
                                public void onResponse(PNPublishResult result, PNStatus status) {
                                    Log.i("PubNub Publish Response", status.isError() + "");
                                }
                            });
                }
                 if (intent.getAction().equals(KEY_NO))
                {
                    Log.i("Received", "Click on NO Button");
                    manager.cancel(1);
                }
            }


        };
        registerReceiver(receiver, filter);
    }
}