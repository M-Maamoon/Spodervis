package com.example.moaaz.spodervis;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.moaaz.spodervis.utils.ChattingMessage;
import com.example.moaaz.spodervis.utils.PatternEntry;
import com.example.moaaz.spodervis.utils.RoundedImageView;
import com.example.moaaz.spodervis.utils.pubnubService;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private int messageCount;
    private int currentIndex = -1;
    private static boolean connected = false;
    private boolean isRevealed = false;
    private String IP = "";
    private String[] textToShow;

    private static ArrayList<ChattingMessage> chatMessages;
    private static ArrayList<PatternEntry> patternEntries;
    private static ArrayList<String[]> reminders = new ArrayList<String[]>();
    private TextSwitcher mSwitcher;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private PNConfiguration pnConfiguration;
    private PubNub pubnub;
    private BroadcastReceiver networkReceiver = new NetworkStateReceiver();
    public static Typeface font;


    public class NetworkStateReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        configurePubNub();
        setListeners();
        setTextSwitcher();
        setTextFont();

        chatMessages = (ArrayList<ChattingMessage>) readObjectFromFile(this, "ChatMessages");
        patternEntries = (ArrayList<PatternEntry>) readObjectFromFile(this, "PatternEntries");

        if (!pubnubService.state)
        {
            Log.i("Service Open", "SERVICEEEE");
            Intent intent = new Intent(this, pubnubService.class);
            startService(intent);
        }

    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
        Collections.sort(chatMessages);
        writeObjectToFile(this, chatMessages, "ChatMessages");
        writeObjectToFile(this, patternEntries, "PatternEntries");

    }

    public void writeObjectToFile(Context context,  Object object, String filename) {

        ObjectOutputStream objectOut = null;
        try {

            FileOutputStream fileOut = context.openFileOutput(filename, Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            fileOut.getFD().sync();
            Log.i("Write " + filename , "Wrote file new arraylist");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    // do nowt
                }
            }
        }
    }

    public static Object readObjectFromFile(Context context, String filename) {

        ObjectInputStream objectIn = null;
        Object object = null;
        try
        {

            FileInputStream fileIn = context.getApplicationContext().openFileInput(filename);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();
            Log.i("Read: "+ filename, "Success");

        }
        catch (FileNotFoundException e)
        {
            Log.i("Read: Not found " + filename, "Created new arraylist");
            e.printStackTrace();
            return new ArrayList<>();
        }
        catch (IOException e)
        {
            Log.i("Read: IO", "Created new arraylist");
            e.printStackTrace();
            return new ArrayList<>();

        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {
                    // do nowt
                }
            }
        }

        return object;
    }


    public void initLayout()
    {
        font = Typeface.createFromAsset(getAssets(),"fonts/HelveticaNeue-Light.ttf");
        textToShow =  getResources().getStringArray(R.array.highlight);
        messageCount = textToShow.length;

        ImageView spodermenIcon = (ImageView) findViewById(R.id.spoderbot_icon);
        spodermenIcon.setImageDrawable(new BitmapDrawable(getResources(), RoundedImageView.getCroppedBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.spoderman),100)));

        ImageView babySpodermenIcon = (ImageView) findViewById(R.id.child_icon);
        babySpodermenIcon.setImageDrawable(new BitmapDrawable(getResources(), RoundedImageView.getCroppedBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.baby_spodermen),100)));



        registerReceiver(networkReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Bitmap recImage = BitmapFactory.decodeResource(getResources(), R.drawable.spoder_icon);

        Bitmap circle = RoundedImageView.getCroppedBitmap(recImage ,100);
        Drawable d = new BitmapDrawable(getResources(), circle);
        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setImageDrawable(d);


    }

    public void setTextSwitcher()
    {
        mSwitcher = (TextSwitcher) findViewById(R.id.headline);
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(MainActivity.this);
                myText.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(18);
                myText.setTypeface(font);
                myText.setTextColor(Color.GRAY);
                return myText;
            }
        });

        Animation in = AnimationUtils.loadAnimation(this,R.anim.slide_in);
        Animation out = AnimationUtils.loadAnimation(this,R.anim.slide_out);

        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                currentIndex++;
                if(currentIndex == messageCount)
                    currentIndex = 0;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwitcher.setText(textToShow[currentIndex]);
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 5000);
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
                findViewById(R.id.stream_icon).performClick();
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
                    intent.putExtra(StreamActivity.LOCATION, "http://197.52.11.60:22143");
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
                 findViewById(R.id.stream_icon).performClick();
            }
        });

        findViewById(R.id.profileButton).setOnClickListener(new View.OnClickListener()
        {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                               reveal();
            }
        });


        findViewById(R.id.pattern_icon).setOnClickListener(new View.OnClickListener()
        {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                reveal();
                Intent intent = new Intent(MainActivity.this, AgendaActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.reminder_layout).setOnClickListener(new View.OnClickListener()
        {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v)
            {
                reveal();
                Intent intent = new Intent(MainActivity.this, AgendaActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.shutdown).setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                final ProgressDialog progress = ProgressDialog.show(MainActivity.this, "Shutting Down",
                        "Killing services and saving them from the pain of existence.", true);
                Handler handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pubnub.publish()
                                .message(Arrays.asList("shutdown"))
                                .channel("commands")
                                .async(new PNCallback<PNPublishResult>() {
                                    @Override
                                    public void onResponse(PNPublishResult result, PNStatus status) {

                                        progress.dismiss();
                                        if (status.isError()) {
                                            Toast.makeText(MainActivity.this, "An error occurred!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }, 1000);
            }
        });


        findViewById(R.id.deleteHistory).setOnClickListener(new View.OnClickListener()
        {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v)
            {
                chatMessages = new ArrayList<ChattingMessage>();
                patternEntries = new ArrayList<PatternEntry>();
                writeObjectToFile(MainActivity.this, chatMessages, "ChatMessages");
                writeObjectToFile(MainActivity.this, patternEntries, "PatternEntries");
            }
        });


    }



    public void setTextFont()
    {
        TextView txt = (TextView)findViewById(R.id.spoderbot_text);
        txt.setTypeface(font);
        txt = (TextView)findViewById(R.id.username_text);
        txt.setTypeface(font);
        txt = (TextView)findViewById(R.id.email_text);
        txt.setTypeface(font);
        txt = (TextView)findViewById(R.id.name_text);
        txt.setTypeface(font);
        txt = (TextView)findViewById(R.id.your_agenda);
        txt.setTypeface(font);
        txt = (TextView)findViewById(R.id.agenda_text);
        txt.setTypeface(font);
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

    public int fromDpToPixel(int dpValue)
    {
        float d = getResources().getDisplayMetrics().density;
        return (int)(dpValue * d);
    }

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void reveal()
    {
        if (!isRevealed)
        {
            isRevealed = true;
            LinearLayout myView = (LinearLayout) findViewById(R.id.profileLayout);
            int cx = myView.getWidth() - 60;
            int cy = 0;
            float finalRadius = (float) Math.hypot(cx, cy);

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

            myView.setVisibility(View.VISIBLE);
            anim.start();
        }
        else
        {
            isRevealed = false;
            final LinearLayout myView = (LinearLayout) findViewById(R.id.profileLayout);

            int cx = myView.getWidth() - 60;
            int cy = 0;

            float initialRadius = (float) Math.hypot(cx, cy);

            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            });
            anim.start();
        }
    }

    public static ArrayList<String[]> getReminders()
    {
        return reminders;
    }

    public static ArrayList<ChattingMessage> getChatMessages()
    {
        return chatMessages;
    }

    public static ArrayList<PatternEntry> getPatternEntries()
    {
        return patternEntries;
    }

    public static  boolean isConnected()
    {
        return connected;
    }



}
