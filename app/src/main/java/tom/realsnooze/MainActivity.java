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

public class MainActivity extends Activity {


    private static final int DEFAULT_SNOOZE_MINUTES = 5;
    private static final long COLLECTION_BREAK_TIME = 30000;
    private static final String TAG = "MainActivity";
    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker alarmTimePicker;
    private static MainActivity inst;
    public TextView alarmTextView;
    public TextView text2;
    private SleepDetector sleepDetector =null;
    private int snoozeMinutes = DEFAULT_SNOOZE_MINUTES;
    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        alarmTextView = (TextView) findViewById(R.id.alarmText);
        text2 = (TextView) findViewById(R.id.textView);
        ToggleButton alarmToggle = (ToggleButton) findViewById(R.id.alarmToggle);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        SleepDetector sleepDetector = new SleepDetector(this, COLLECTION_BREAK_TIME);
    }

    public void onToggleClicked(View view) {
        if (((ToggleButton) view).isChecked()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
            setAlarm(calendar);
        } else {
            Log.d(TAG, "Music off");
            MusicPlayer.stop();
        }
    }

    private void setAlarm(Calendar calendar) {
        Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.d(TAG, "Alarm set to "+calendar.getTime().toString());
    }

    public void setAlarmText(String alarmText) {
        alarmTextView.setText(alarmText);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        if (sleepDetector==null) {
            sleepDetector = new SleepDetector(this,COLLECTION_BREAK_TIME);
            soundAlarm(this);
            setSnooze();
        }
        else if (sleepDetector.isAsleep()){
            soundAlarm(this);
            sleepDetector.wokeUp();
            setSnooze();
        }
        else {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void setSnooze() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, snoozeMinutes);
        setAlarm(calendar);
        Log.d(TAG,"snooze set to "+snoozeMinutes+" from now");
    }

    private void soundAlarm(Context context) {
        Log.d(TAG,"ALARM ALARM!!");
        MusicPlayer.start(context);
    }
}
