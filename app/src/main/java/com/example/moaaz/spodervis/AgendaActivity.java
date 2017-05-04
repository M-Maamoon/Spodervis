package com.example.moaaz.spodervis;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.example.moaaz.spodervis.utils.AlertReceiver;
import com.example.moaaz.spodervis.utils.PatternEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;


public class AgendaActivity extends AppCompatActivity {

    ProgressDialog progress;
    ArrayList<String[]> entries;
    ArrayList<PatternEntry> patternEntries;
    Hashtable<Integer, PatternEntry> patternIdsTable =  new Hashtable<>();
    Hashtable<Integer, Intent> intents = new Hashtable<>();
    private AlarmManager alarmMgr;
    private PendingIntent alarmPendingIntent;

    String hour = "";
    String minute = "";
    String title = "";
    String command = "";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
        entries = MainActivity.getReminders();
        this.patternEntries = MainActivity.getPatternEntries();

        loadView();
        setListeners();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void loadView()
    {
        for (int i = 0; i < patternEntries.size(); i++)
        {
            PatternEntry p = patternEntries.get(i);
            addAlarm(p);
            makeView(p, p.getHour() + ":" + p.getMinute(), p.getTitle(), i);
        }

    }

    public void setListeners()
    {
        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v)
            {
                AgendaActivity.this.hour = "";
                AgendaActivity.this.minute = "";
                AgendaActivity.this.title = "";
                AgendaActivity.this.command = "";

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AgendaActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        AgendaActivity.this.hour =   selectedHour < 10?
                                "0" + selectedHour + "": selectedHour + "";

                        AgendaActivity.this.minute =   selectedMinute < 10?
                                "0" + selectedMinute + "": selectedMinute + "";

                        popUpTitleDialog();


                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }

        });
    }

    public void popUpTitleDialog()
    {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Set command");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lp_1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        layout.setPadding(16, 0, 16, 0);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
                int dp = fromDpToPixel(16);
        lp.setMargins(dp, 0, dp, 0);
        lp_1.setMargins(dp, dp, dp, 0);

        final TextView enterTitle = new TextView(this);
        enterTitle.setText("Enter title");
        enterTitle.setTypeface(MainActivity.font);
        enterTitle.setTextColor(Color.BLACK);
        enterTitle.setTextSize(16);
        final EditText titleInput = new EditText(this);
        enterTitle.setLayoutParams(lp_1);
        titleInput.setLayoutParams(lp);

        final TextView enterCommand = new TextView(this);
        enterCommand.setText("What should I do?");
        enterCommand.setTypeface(MainActivity.font);
        enterCommand.setTextColor(Color.BLACK);
        enterCommand.setTextSize(16);
        enterCommand.setLayoutParams(lp);
        final EditText commandInput = new EditText(this);
        enterCommand.setLayoutParams(lp);
        commandInput.setLayoutParams(lp);

        final TextView error = new TextView(this);
        error.setText("Your command cannot be understood!");
        error.setTypeface(Typeface.DEFAULT_BOLD);
        error.setTextColor(Color.RED);
        error.setTextSize(14);
        error.setLayoutParams(lp);
        error.setVisibility(View.INVISIBLE);

        layout.addView(enterTitle);
        layout.addView(titleInput);
        layout.addView(enterCommand);
        layout.addView(commandInput);
        layout.addView(error);
        layout.setLayoutParams(lp_1);

        alertDialog.setView(layout);



        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        final AlertDialog dialog = alertDialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(!commandInput.getText().toString().equals(""))
                {

                    final nlp n = new nlp();
                    progress = ProgressDialog.show(AgendaActivity.this, "Executing",
                            "Please wait until the command is verified", true);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void run() {

                            try {
                                    n.execute(commandInput.getText().toString()).get();
                                    if (n.value.equals("null"))
                                    {

                                        progress.dismiss();
                                        error.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        AgendaActivity.this.title = titleInput.getText().toString();
                                        AgendaActivity.this.command = n.value;

                                        AgendaActivity.this.addAlarm(AgendaActivity.this.addEntry());

                                        Toast.makeText(AgendaActivity.this, "Your command is registered",
                                                Toast.LENGTH_SHORT).show();
                                        progress.dismiss();
                                        dialog.dismiss();
                                    }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 500);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addAlarm(PatternEntry entry)
    {
        if (entry.isActivated())
        {
            Intent alertIntent = new Intent(this, AlertReceiver.class);
            intents.put(entry.getId(), alertIntent);
            alertIntent.putExtra("command", entry.getCommand());
            alertIntent.putExtra("title", entry.getTitle());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(entry.getHour()));
            calendar.set(Calendar.MINUTE, Integer.parseInt(entry.getMinute()));

            long diff = Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis();

            if (diff > 0) {
                calendar.add(Calendar.DATE, 1);
            }

            AlarmManager m = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 1, alertIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            m.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmPendingIntent);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public PatternEntry addEntry()
    {
        PatternEntry p = new PatternEntry(title, command, hour, minute);
        patternEntries.add(p);
        Collections.sort(patternEntries);
        makeView(p, hour + ":" + minute, title, patternEntries.indexOf(p));

        return p;

    }

    public void cancelEntry(PatternEntry p)
    {
        AlarmManager m = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = intents.get(p.getId());
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 1, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        m.cancel(alarmPendingIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public View makeView(PatternEntry p, String time, String reminder, int index)
    {
        View v = LayoutInflater.from(this).inflate(R.layout.reminder_view, null);

        TextView textView = (TextView) v.findViewById(R.id.reminderText);
        TextView timeView = (TextView) v.findViewById(R.id.timetText);
        timeView.setTypeface(MainActivity.font);
        timeView.setText(time);
        textView.setText(reminder);
        textView.setTypeface(MainActivity.font);
        int id = View.generateViewId();
        Switch s = (Switch) v.findViewById(R.id.switchButton);
        s.setId(id);
        p.setId(id);
        patternIdsTable.put(id, p);

        if (!p.isActivated())
        {
            s.setChecked(false);
        }

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.remindersLayout);

        insertPoint.addView(v, index, new
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void toggleEntry(View view)
    {
        Switch s = (Switch) view;
        int entryId = s.getId();
        PatternEntry entry = patternIdsTable.get(entryId);

        if (!s.isChecked())
        {
            cancelEntry(entry);
            entry.setActivated(false);
        }
        else
        {
            addAlarm(entry);
            entry.setActivated(true);
        }
    }


    public int fromDpToPixel(int dpValue)
    {
        float d = getResources().getDisplayMetrics().density;
        return (int)(dpValue * d);
    }





}
