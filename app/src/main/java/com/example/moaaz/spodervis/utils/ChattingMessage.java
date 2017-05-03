package com.example.moaaz.spodervis.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Moaaz on 5/3/2017.
 */

public class ChattingMessage implements Comparable<ChattingMessage>, Serializable
{

    private boolean isUser;
    private long time;
    private String hour;
    private String minute;
    private String content;


    public ChattingMessage(String content, boolean isUser)
    {
        this.content = content;
        this.isUser = isUser;

        Calendar c = Calendar.getInstance();
        this.hour = c.get(Calendar.HOUR_OF_DAY) > 10? c.get(Calendar.HOUR_OF_DAY)  + "":
                        "0" + c.get(Calendar.HOUR_OF_DAY);
        this.minute = c.get(Calendar.MINUTE) > 10? c.get(Calendar.MINUTE)  + "":
                "0" + c.get(Calendar.MINUTE);

        time = Long.parseLong(c.get(Calendar.DAY_OF_YEAR) + hour + minute +
                c.get(Calendar.SECOND) + c.get(Calendar.MILLISECOND));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int compareTo(ChattingMessage o) {
        return Long.compare(this.time, o.time);
    }

    @Override
    public String toString()
    {
        return "Message:" + content + " Sent at: " + hour + ":" + minute + " By User? " + isUser + " Time Stamp: " + time;
    }

    public boolean sentByUser()
    {
        return isUser;
    }

    public String getContent()
    {
        return content;
    }

}
