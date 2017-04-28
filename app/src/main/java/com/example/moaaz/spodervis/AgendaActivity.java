package com.example.moaaz.spodervis;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.os.AsyncTask;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;


public class AgendaActivity extends AppCompatActivity {

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.remindersLayout);

        insertPoint.addView(makeView("6:00", "Wake up"), 0, new
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        insertPoint.addView(makeView("6:30", "Coffee"), 1, new
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));


        setListeners();

    }

    public void setListeners()
    {
        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v)
            {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AgendaActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        Log.i("Time:", selectedHour + " : " + selectedMinute);

                        popUpTitleDialog();

                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }

        });
    }

    public void popUpTitleDialog()
    {



        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
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

                Log.i("Title:", titleInput.getText().toString());
                Log.i("Command:", commandInput.getText().toString());


                if(!commandInput.getText().toString().equals("")) {

                    final nlp n = new nlp();
                    progress = ProgressDialog.show(AgendaActivity.this, "Executing",
                            "Please wait until the command is verified", true);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                    n.execute(commandInput.getText().toString()).get();
                                    if (n.value.equals("null"))
                                    {
                                        progress.dismiss();
                                        error.setVisibility(View.VISIBLE);
                                        Log.i("WRONG", "INPUT");
                                    }
                                    else
                                    {
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

    public View makeView(String time, String reminder)
    {
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.reminder_view, null);

        TextView textView = (TextView) v.findViewById(R.id.reminderText);
        TextView timeView = (TextView) v.findViewById(R.id.timetText);
        timeView.setTypeface(MainActivity.font);
        timeView.setText(time);
        textView.setText(reminder);
        textView.setTypeface(MainActivity.font);

        return v;
    }


    public int fromDpToPixel(int dpValue)
    {
        float d = getResources().getDisplayMetrics().density;
        return (int)(dpValue * d);
    }



    public boolean checkCommand(String command) throws ExecutionException, InterruptedException {


        Log.i("LFMA","LFAL");
        nlp n = new nlp();
        n.execute(command).get();
        Log.i("FINISHED","JRE");
        if (n.value.equals("null"))
        {
            progress.dismiss();
            return false;
        }
        progress.dismiss();
        return true;
    }


}
