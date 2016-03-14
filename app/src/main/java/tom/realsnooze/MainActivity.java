package tom.realsnooze;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;

public class MainActivity extends Activity  {


    public static final int DEFAULT_SNOOZE_MINUTES = 2;
    public static final long COLLECTION_BREAK_TIME = 30000;
    private int snoozeMinutes = DEFAULT_SNOOZE_MINUTES;
    public static final String INTENT_PARAM_SNOOZE = "snooze";
    public static final String INTENT_PARAM_IS_ALARM = "isAlarm";
    private static final String TAG = "MainActivity";

    private ToggleButton toggle;
    private TimePicker alarmTimePicker;
    public TextView alarmTextView;
    public TextView text2;
    public static SleepDetector sleepDetector =null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        alarmTextView = (TextView) findViewById(R.id.alarmText);
        text2 = (TextView) findViewById(R.id.textView);
        toggle = (ToggleButton) findViewById(R.id.alarmToggle);
        ToggleButton alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent in = getIntent();
        boolean alarm = in.getBooleanExtra(INTENT_PARAM_IS_ALARM, false);
        Log.e(TAG,"onResume. starting from alarm? "+ alarm);
        if (alarm) {
            toggle.setChecked(true);
            if (sleepDetector==null) {
                sleepDetector = new SleepDetector(this,MainActivity.COLLECTION_BREAK_TIME);
                soundAlarm(this);
                setSnooze(this, in.getIntExtra(MainActivity.INTENT_PARAM_SNOOZE,MainActivity.DEFAULT_SNOOZE_MINUTES));
            }
            else if (sleepDetector.isAsleep()){
                soundAlarm(this);
                sleepDetector.wokeUp();
                setSnooze(this,in.getIntExtra(MainActivity.INTENT_PARAM_SNOOZE,MainActivity.DEFAULT_SNOOZE_MINUTES));
            }

        }

    }

    public void onToggleClicked(View view) {
        Log.e(TAG,"onToggle");
        if (((ToggleButton) view).isChecked()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
            setAlarm(this,calendar,snoozeMinutes);
        } else {
            Log.e(TAG, "Music off");
            MusicPlayer.stop();
        }
    }

    public static void setAlarm(Context context,Calendar calendar,int snoozeMinutes) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(INTENT_PARAM_SNOOZE, snoozeMinutes);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.INTENT_PARAM_IS_ALARM, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        ((AlarmManager) context.getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.e(TAG, "Alarm set to " + calendar.getTime().toString());
    }

    public void setAlarmText(String alarmText) {
        alarmTextView.setText(alarmText);
    }


    private void setSnooze(Context context, int snoozeMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, snoozeMinutes);
        MainActivity.setAlarm(context, calendar, snoozeMinutes);
        Log.e(TAG, "snooze set to " + snoozeMinutes + " from now");
    }

    private void soundAlarm(Context context) {
        Log.e(TAG, "ALARM ALARM!!");
        MusicPlayer.start(context);
    }

}
