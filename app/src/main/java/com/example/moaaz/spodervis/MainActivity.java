package com.example.moaaz.spodervis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.os.Vibrator;
import android.widget.TextView;

import com.example.moaaz.spodervis.utils.RoundedImageView;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;
import java.util.Random;


public class MainActivity extends AppCompatActivity {


    PNConfiguration pnConfiguration;
    PubNub pubnub;
    boolean connected = false;
    boolean lightOn = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePubNub();


        ImageView spodermenIcon = (ImageView) findViewById(R.id.spoderbot_icon);
        spodermenIcon.setImageDrawable(new BitmapDrawable(getResources(), RoundedImageView.getCroppedBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.spoderman),100)));


        ImageView babySpodermenIcon = (ImageView) findViewById(R.id.child_icon);
        babySpodermenIcon.setImageDrawable(new BitmapDrawable(getResources(), RoundedImageView.getCroppedBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.baby_spodermen),100)));

        registerReceiver(new NetworkStateReceiver(),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        setListeners();


    }

    public void setListeners()
    {
        findViewById(R.id.spoderbot_icon).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (connected)
                {
                    findViewById(R.id.noConnectionText).setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(MainActivity.this, ChattingActivity.class);
                    startActivity(intent);
                }
                else
                {
                    displayNotConnected();
                }
            }

        });

        findViewById(R.id.child_icon).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public  void onClick(View view)
            {
                if (connected)
                {
                    findViewById(R.id.noConnectionText).setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(MainActivity.this, BabyActivity.class);
                    startActivity(intent);
                }
                else
                {
                    displayNotConnected();
                }
            }
        });

        findViewById(R.id.stream_icon).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public  void onClick(View view)
            {
                if (connected)
                {
                    findViewById(R.id.noConnectionText).setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(MainActivity.this, StreamActivity.class);
                    startActivity(intent);
                }
                else
                {
                    displayNotConnected();
                }
            }
        });

        findViewById(R.id.door_icon).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public  void onClick(View view)
            {
                if (connected)
                {
                    findViewById(R.id.noConnectionText).setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(MainActivity.this, DoorActivity.class);
                    startActivity(intent);
                }
                else
                {
                    displayNotConnected();
                }
            }
        });
    }

    public class NetworkStateReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable();
        }
    }

    private void isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        ImageView connectionIcon = (ImageView) findViewById(R.id.connectionIcon);

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
        {
            connected = true;
            connectionIcon.setImageResource(R.drawable.ic_connected);
            findViewById(R.id.noConnectionText).setVisibility(View.INVISIBLE);
        }
        else
        {
            connected = false;
            connectionIcon.setImageResource(R.drawable.ic_disconnected);
            displayNotConnected();
        }

    }


    public void startChatting(View view)
    {
        if (connected)
        {
            findViewById(R.id.noConnectionText).setVisibility(View.INVISIBLE);
            Intent intent = new Intent(MainActivity.this, ChattingActivity.class);
            startActivity(intent);
        }
        else
        {
            displayNotConnected();
        }
    }

    public void displayNotConnected()
    {
        Log.i("Not", "Connected");
        ImageView connectionIcon = (ImageView) findViewById(R.id.connectionIcon);
        connectionIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        final Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(50);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                v.vibrate(50);
            }
        }, 100);


        Random n = new Random();
        int message = n.nextInt(5);
        String m = "";
        switch (message)
        {
            case 0:
                m = "No internet connection"; break;
            case 1:
                m = "How are you even alive without internet?"; break;
            case 2:
                m = "Did you hear that we are in 2017? Have internet!"; break;
            case 3:
                m = "No internet. Does not matter, it is all meaningless anyways ..."; break;
            case 4:
                m = "Internet connection not found. Just like your life lel."; break;
        }

        TextView noInternetConnectionView = (TextView) findViewById(R.id.noConnectionText);
        noInternetConnectionView.setText(m);
        noInternetConnectionView.setVisibility(View.VISIBLE);
    }


    public void switchLight(String option)
    {
        pubnub.publish()
                .message(Arrays.asList(option))
                .channel("commands")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                    }
                });
    }
/*
    public void switchLight(View view)
    {
        if (!lightOn)
        {
            Button ls = (Button)  findViewById(R.id.switchLightButton);
            ls.setText("Switch Off");
            lightOn = true;
            pubnub.publish()
                    .message(Arrays.asList("on"))
                    .channel("commands")
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError to see if error happened
                        }
                    });
            return;
        }
        lightOn = false;
        Button ls = (Button)  findViewById(R.id.switchLightButton);
        ls.setText("Switch On");
        pubnub.publish()
                .message(Arrays.asList("off"))
                .channel("commands")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                    }
                });
    }
*/
    public void configurePubNub()
    {
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

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
    }
}
