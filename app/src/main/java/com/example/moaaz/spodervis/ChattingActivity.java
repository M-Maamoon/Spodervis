package com.example.moaaz.spodervis;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.text.TextWatcher;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by Moaaz on 4/13/2017.
 */

public class ChattingActivity extends AppCompatActivity
{

    EditText messageTextField;
    ScrollView scroll;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting_layout);
        scroll =  (ScrollView) findViewById(R.id.chatScrollView);
        messageTextField = (EditText) findViewById(R.id.messageTextField);
        scroll.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 100);

    }


    public void sendMessage(View view)
    {

        String stringMessage = messageTextField.getText().toString();
        if (!stringMessage.equals(""))
        {
            TextView message = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(fromDpToPixel(72), 0, fromDpToPixel(8), fromDpToPixel(8));
            lp.gravity = Gravity.RIGHT;

            message.setTextColor(Color.rgb(65, 105, 225));
            message.setBackgroundResource(R.drawable.user_text_bubble);
            message.setLayoutParams(lp);
            message.setText(stringMessage);
            message.setTextSize(16);
            message.setPadding(fromDpToPixel(16), fromDpToPixel(8), fromDpToPixel(16), fromDpToPixel(8));
            LinearLayout messageArea = (LinearLayout) findViewById(R.id.messagesArea);
            messageArea.addView(message);
            messageTextField.setText("");
            scroll.post(new Runnable() {

                @Override
                public void run() {
                    scroll.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

        }

    }




    public int fromDpToPixel(int dpValue)
    {
        float d = getResources().getDisplayMetrics().density;
        return (int)(dpValue * d);
    }

}
