package com.example.moaaz.spodervis;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import java.util.ArrayList;

/**
 * Created by Moaaz on 4/13/2017.
 */

public class ChattingActivity extends AppCompatActivity implements RecognitionListener {

    EditText messageTextField;
    ScrollView scroll;
    private SpeechRecognizer speech;
    boolean listening = false;
    boolean isRevealed = false;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_layout);
        scroll =  (ScrollView) findViewById(R.id.chatScrollView);
        messageTextField = (EditText) findViewById(R.id.messageTextField);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     //   getSupportActionBar().setDisplayShowHomeEnabled(true);

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
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
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
    public void sendMessage(View view)
    {

        if(isRevealed)
        {
            reveal();
        }

        String stringMessage = messageTextField.getText().toString();
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

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    reply();
                }
            }, 1000);

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

    public void reply()
    {
        TextView message = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(fromDpToPixel(8), 0, fromDpToPixel(72), fromDpToPixel(8));
        lp.gravity = Gravity.LEFT;

        message.setTextColor(Color.rgb(255, 255, 255));
        message.setBackgroundResource(R.drawable.spodervis_text_bubble);
        message.setLayoutParams(lp);
        message.setText("¯\\_(ツ)_/¯");
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
                sendMessage(null);
            }
        }, 1000);
    }




//Helpers
    public int fromDpToPixel(int dpValue)
    {
        float d = getResources().getDisplayMetrics().density;
        return (int)(dpValue * d);
    }

}
