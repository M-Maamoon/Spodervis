package com.example.moaaz.spodervis;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import com.example.moaaz.spodervis.utils.AlertReceiver;

public class BabyActivity extends AppCompatActivity {

    NotificationManager notificationManager;
    boolean isActive = false;
    int id = 33;
    Button show, hide, alarm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby);
        show = (Button) findViewById(R.id.show);
        hide = (Button) findViewById(R.id.stop);
        alarm = (Button) findViewById(R.id.five);

//        registerReceiver(new AlertReceiver(), null);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(View view)
    {
        NotificationCompat.Builder notificBuilder = new
                NotificationCompat.Builder(this);
        notificBuilder.setContentTitle("titile lmfao");
        notificBuilder.setContentText("text lmfao");
        notificBuilder.setTicker("ticker lmfao");
        notificBuilder.setSmallIcon(R.drawable.ic_add);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notificBuilder.build());
        isActive = true;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showNotificationDelay(View view)
    {
        Long alert = new GregorianCalendar().getTimeInMillis()+5*1000;
        Intent alertIntent = new Intent(this, AlertReceiver.class);
        alertIntent.putExtra("command", "lel");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);

        AlarmManager m = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        m.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                PendingIntent.FLAG_UPDATE_CURRENT ));

    }

    public void hideNotification(View view)
    {
        if(!isActive)
        {
            notificationManager.cancel(id);
            isActive = false;
        }
    }



}
