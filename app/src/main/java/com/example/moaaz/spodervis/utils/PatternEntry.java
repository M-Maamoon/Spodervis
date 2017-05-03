package com.example.moaaz.spodervis.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Moaaz on 5/3/2017.
 */

public class PatternEntry implements Comparable<PatternEntry>, Serializable {

    private boolean isActivated;
    private int timeStamp;
    private int id;
    private String title;
    private String command;
    private String hour;
    private String minute;


    public boolean isActivated() {
        return isActivated;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public String getTitle() {
        return title;
    }

    public String getCommand() {
        return command;
    }

    public String getHour() {
        return hour;
    }

    public String getMinute() {
        return minute;
    }



    public void setActivated(boolean a)
    {
        this.isActivated = a;
    }


    public PatternEntry(String t, String c, String h, String m) {
        this.title = t;
        this.command = c;
        this.hour = h;
        this.minute = m;

        this.isActivated = true;
        this.timeStamp = Integer.parseInt(hour + minute);
    }

    @Override
    public String toString()
    {
        return "Entry:- At: " + this.hour + ":" + this.minute + " Title: " + title + " Executing: " + command;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int compareTo(PatternEntry o) {
        return Integer.compare(this.timeStamp, o.timeStamp);
    }
}
