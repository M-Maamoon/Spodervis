package com.example.moaaz.spodervis;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Moaaz on 4/13/2017.
 */

public class ChattingActivity extends AppCompatActivity implements RecognitionListener {

    EditText messageTextField;
    ScrollView scroll;
    nlp n;
    private SpeechRecognizer speech;
    boolean listening = false;
    boolean isRevealed = false;
    boolean connected = false;
    Hashtable<String, Boolean> state = new Hashtable<String, Boolean>();
    String[][] messages = new String[5][];
    PNConfiguration pnConfiguration;
    PubNub pubnub;
    TextToSpeech speaker;
    MediaPlayer messageEffect;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_layout);
        scroll =  (ScrollView) findViewById(R.id.chatScrollView);
        messageTextField = (EditText) findViewById(R.id.messageTextField);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        Bitmap recImage = BitmapFactory.decodeResource(getResources(), R.drawable.spoderman);

        Bitmap circle = RoundedImageView.getCroppedBitmap(recImage ,100);
        Drawable d = new BitmapDrawable(getResources(), circle);
        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setImageDrawable(d);


        scroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);

        messageTextField.setOnTouchListener(new View.OnTouchListener()
        {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (v.getId() == messageTextField.getId())
                {
                    if (isRevealed)
                    {
                        reveal();
                    }
                    messageTextField.setCursorVisible(true);
                }
                return false;
            }

        });


        final LinearLayout messageArea = (LinearLayout) findViewById(R.id.messagesArea);
        messageArea.setOnTouchListener(new View.OnTouchListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v,  MotionEvent event)
            {
                if (v.getId() == messageArea.getId())
                {
                    if (isRevealed)
                    {
                        reveal();
                    }
                }
                return false;
            }


        });



        buttonBackgroundChanger();
        initMessageArrays();
        initState();
        speaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                speaker.setLanguage(Locale.US);
            }
        });

        messageEffect = MediaPlayer.create(this, R.raw.message_effect);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);

        configurePubNub();
    }

    public void initState()
    {
        state.put("light", false);
        state.put("music", false);
    }

    public void initMessageArrays()
    {
        messages[0] = getResources().getStringArray(R.array.light_on);
        messages[1] = getResources().getStringArray(R.array.light_off);
        messages[2] = getResources().getStringArray(R.array.music_on);
        messages[3] = getResources().getStringArray(R.array.music_off);
        messages[4] = getResources().getStringArray(R.array.noop);

    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backButton:
                this.finish();
                return true;
            case R.id.revealButtonItem:
                reveal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void backHome(View view)
    {
        this.finish();
    }


    public void buttonBackgroundChanger()
    {
        messageTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Button sendButton = (Button) findViewById(R.id.sendButton);
               if (!s.toString().equals(""))
               {
                   if (!listening)
                   sendButton.setBackgroundResource(R.drawable.send_button_blue);
               }
                else
               {
                   sendButton.setBackgroundResource(R.drawable.mic_button_blue);
               }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void sendMessage(View view) throws ExecutionException, InterruptedException {
        if(isRevealed)
        {
            reveal();
        }

        final String stringMessage = messageTextField.getText().toString();
        if (!stringMessage.equals(""))
        {

            TextView message = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(fromDpToPixel(72), 0, fromDpToPixel(8), fromDpToPixel(8));
            lp.gravity = Gravity.RIGHT;

            message.setTextColor(Color.rgb(255, 255, 255));
            message.setBackgroundResource(R.drawable.user_text_bubble);
            message.setLayoutParams(lp);
            message.setText(stringMessage);
            message.setTextSize(16);
            message.setPadding(fromDpToPixel(16), fromDpToPixel(8), fromDpToPixel(16), fromDpToPixel(8));
            LinearLayout messageArea = (LinearLayout) findViewById(R.id.messagesArea);
            messageArea.addView(message, messageArea.getChildCount() - 1);
            messageTextField.setText("");
            scroll.post(new Runnable() {

                @Override
                public void run() {
                    scroll.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            messageEffect.start();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run() {
                    try {
                        handleCommand(stringMessage);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, 100);

        }
        else
        {
            Log.d("Started", "Listening");
            listening = true;
            startAnimation();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speech.startListening(intent);
            messageTextField.setTextColor(Color.GRAY);
            messageTextField.setCursorVisible(false);
            messageTextField.setHint("Listening ... ");
        }
    }


    public void handleCommand(String command) throws ExecutionException, InterruptedException {
        n = new nlp(this);
        n.execute(command).get();
        reply(n.value);

        Thread thread = new Thread() {
            @Override
            public void run() {
                executeCommand(n.value);
            }
        };

        thread.start();

    }

    public void executeCommand(String response)
    {
        String command = "";
        if (!state.get("light") &&
                response.equals("switch_light on")) {

            command = "light_on";
        }
        else if (state.get("light") &&
                response.equals("switch_light_off")) {
            command = "light_off";
        }
        else if (!state.get("music") &&
                response.equals("play_music")) {

            command = "music_on";
        }
        else if (state.get("music") &&
                response.equals("stop_music")) {
           command = "music_off";
        }
        if (!command.equals(""))
             sendCommand(command);


    }

    public void sendCommand(String command)
    {

        pubnub.publish()
                .message(Arrays.asList(command))
                .channel("commands")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                    }
                });
    }

    public void reply(String response)
    {
     /*   TextView message = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(fromDpToPixel(8), 0, fromDpToPixel(72), fromDpToPixel(8));
        lp.gravity = Gravity.LEFT;

        message.setTextColor(Color.rgb(255, 255, 255));
        message.setBackgroundResource(R.drawable.spodervis_text_bubble);
        message.setLayoutParams(lp);
        message.setTextSize(16);
        message.setPadding(fromDpToPixel(16), fromDpToPixel(8), fromDpToPixel(16), fromDpToPixel(8));
        LinearLayout messageArea = (LinearLayout) findViewById(R.id.messagesArea);
        messageArea.addView(message, messageArea.getChildCount() - 1);
        String r = makeReply(response);

        message.setText(r);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (r.contains("_"))
                speaker.speak("This is meaningless to me, Sir!",TextToSpeech.QUEUE_FLUSH,null,null);
            else
                speaker.speak(r, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else
        {
            if (r.contains("_"))
                speaker.speak("This is meaningless to me, Sir!", TextToSpeech.QUEUE_FLUSH, null);
            else
                speaker.speak(r, TextToSpeech.QUEUE_FLUSH, null);

        }
        scroll.post(new Runnable() {

            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
*/
        String r = makeReply(response);
        appendMessage(r);
    }


    public void appendMessage(String m)
    {
        TextView message = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(fromDpToPixel(8), 0, fromDpToPixel(72), fromDpToPixel(8));
        lp.gravity = Gravity.LEFT;

        message.setTextColor(Color.rgb(255, 255, 255));
        message.setBackgroundResource(R.drawable.spodervis_text_bubble);
        message.setLayoutParams(lp);
        message.setTextSize(16);
        message.setPadding(fromDpToPixel(16), fromDpToPixel(8), fromDpToPixel(16), fromDpToPixel(8));
        LinearLayout messageArea = (LinearLayout) findViewById(R.id.messagesArea);
        messageArea.addView(message, messageArea.getChildCount() - 1);


        message.setText(m);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                speaker.speak(m, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else
        {
                speaker.speak(m, TextToSpeech.QUEUE_FLUSH, null);
        }

        scroll.post(new Runnable() {

            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    public String makeReply(String response)
    {

        if (response.equals("switch_on_light"))
        {
            if(state.get("light"))
            {
                return messages[0][(new Random().nextInt(8 - 6) + 6)];
            }

            state.put("light", true);
            Log.i("Message:", messages[0][(new Random().nextInt(6))]);
            return messages[0][(new Random().nextInt(6))];
        }

        else if (response.equals("switch_off_light"))
        {
            if(!state.get("light"))
            {
                return messages[1][(new Random().nextInt(8 - 6) + 6)];
            }

            state.put("light", false);
            return messages[1][(new Random().nextInt(6))];
        }
        else if (response.equals("play_music"))
        {
            if(state.get("music"))
            {
                return messages[2][(new Random().nextInt(8 - 6) + 6)];
            }

            state.put("music", true);
            return messages[2][(new Random().nextInt(6))];
        }

        else if (response.equals("stop_music"))
        {
            if(!state.get("music"))
            {
                return messages[3][(new Random().nextInt(8 - 6) + 6)];
            }

            state.put("music", false);
            return messages[3][(new Random().nextInt(6))];
        }
        else if (response.equals("k"))
        {
            return "K";
        }
        else if (response.equals("question"))
        {
            return "Do you have an existential crisis, Sir?";
        }
        else if (response.equals("null"))
        {
            return messages[4][(new Random().nextInt(10))];
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void reveal()
    {
        if (!isRevealed)
        {
            isRevealed = true;
            LinearLayout myView = (LinearLayout) findViewById(R.id.shortcutLayout);
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
            final LinearLayout myView = (LinearLayout) findViewById(R.id.shortcutLayout);

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




    //Button Animation
    public void startAnimation()
    {
        final Animation fadeOut = AnimationUtils.loadAnimation(ChattingActivity.this, R.anim.fade_out);
        final Animation fadeIn = AnimationUtils.loadAnimation(ChattingActivity.this, R.anim.fade_in);
        fadeOut.setRepeatCount(Animation.INFINITE);
        final Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                sendButton.startAnimation(fadeIn);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (listening)
                sendButton.startAnimation(fadeOut);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

// Speech Recognizer methods
    @Override
    public void onReadyForSpeech(Bundle params) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }


    @Override
    public void onError(int error) {

    }
    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onPartialResults(Bundle partialResults)
    {
        ArrayList<String> matches = partialResults.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        messageTextField.setTextColor(Color.GRAY);
        messageTextField.setText(matches.get(0));
        for (String s: matches)
        {
            Log.d("Partial: ", s);
        }
    }

    @Override
    public void onEndOfSpeech() {
        if (messageTextField.getText().toString().equals(""))
        {
            messageTextField.setHint("Say or write something ... ");
        }
        listening = false;
        Log.d("end", "end");
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        messageTextField.setTextColor(Color.BLACK);
        messageTextField.setText(matches.get(0));

        for (String s: matches)
        {
            Log.d(matches.indexOf(s) + "", s);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                messageTextField.setHint("Say or write something ... ");
                try {
                    sendMessage(null);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 1000);
    }




//Helpers
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
                if (message.getChannel().equals("door_cam"))
                {
                    final String m = message.getMessage().toString().replaceAll("\\[", "").replaceAll("\\]", "");
                    Log.i("Message received:", m);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appendMessage(m);
                        }
                    });
                }
                else
                {
                    String m = message.getMessage().toString().replaceAll("\\[", "").replaceAll("\\]", "");
                    m = m.replace("\"", "");

                    String[] messageEntity = m.split(",");
                    String appendingText = "";
                    if (messageEntity[0].equals("light"))
                    {
                        if(messageEntity[1].equals("1"))
                        {
                            appendingText = "The room is bright, sir.";
                        }
                        else
                        {
                            appendingText = "The room is dark, sir. Should I switch on the lights?";
                        }
                    }
                    Log.i("Entity", messageEntity[0]);
                    final String finalAppendingText = appendingText;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appendMessage(finalAppendingText);
                        }
                    });

                }


            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });

        pubnub.subscribe().channels(Arrays.asList("door_cam")).execute();
        pubnub.subscribe().channels(Arrays.asList("sensor_data")).execute();
        Log.i("Subscribed:", "Subscribed to channel door_cam");
    }
}
