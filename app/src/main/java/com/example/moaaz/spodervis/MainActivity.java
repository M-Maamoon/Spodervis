package com.example.moaaz.spodervis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

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
    Typeface font;
    boolean connected = false;
    boolean lightOn = false;

    private TextSwitcher mSwitcher;

    String textToShow[]={"Chat with Spoderbot","Control your home ...","See home status ...", "See your daily routine ...", "Let him judge you ..."};
    int messageCount=textToShow.length;
    int currentIndex = -1;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configurePubNub();

        font = Typeface.createFromAsset(getAssets(),"fonts/HelveticaNeue-Light.ttf");

        ImageView spodermenIcon = (ImageView) findViewById(R.id.spoderbot_icon);
        spodermenIcon.setImageDrawable(new BitmapDrawable(getResources(), RoundedImageView.getCroppedBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.spoderman),100)));


        ImageView babySpodermenIcon = (ImageView) findViewById(R.id.child_icon);
        babySpodermenIcon.setImageDrawable(new BitmapDrawable(getResources(), RoundedImageView.getCroppedBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.baby_spodermen),100)));

        registerReceiver(new NetworkStateReceiver(),
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        TextView spoderText = (TextView)findViewById(R.id.spoderbot_text);
        spoderText.setTypeface(font);
        setListeners();

        setTextSwitcher();
    }

    public void setTextSwitcher()
    {
        mSwitcher = (TextSwitcher) findViewById(R.id.headline);

        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(MainActivity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(20);
                myText.setTypeface(font);
                myText.setTextColor(Color.DKGRAY);
                return myText;
            }
        });

        Animation in = AnimationUtils.loadAnimation(this,R.anim.slide_in);
        Animation out = AnimationUtils.loadAnimation(this,R.anim.slide_out);

        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);


    }

    public void setListeners()
    {
        findViewById(R.id.spoder_layout).setOnClickListener(new View.OnClickListener()
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
                   // findViewById(R.id.noConnectionText).setVisibility(View.INVISIBLE);
                    //Intent intent = new Intent(MainActivity.this, BabyActivity.class);
                    //startActivity(intent);

                }
                else
                {
                    displayNotConnected();
                }
                currentIndex++;
                // If index reaches maximum reset it
                if(currentIndex==messageCount)
                    currentIndex=0;
                mSwitcher.setText(textToShow[currentIndex]);
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
                    intent.putExtra(StreamActivity.LOCATION, "http://197.53.26.106:22143");
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
            connectionIcon.setVisibility(View.INVISIBLE);
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
                Log.i("Message received:", message.getMessage().toString());
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });

        pubnub.subscribe().channels(Arrays.asList("door_cam")).execute();
        Log.i("Subscribed:", "Subscribed to channel door_cam");
    }
}
