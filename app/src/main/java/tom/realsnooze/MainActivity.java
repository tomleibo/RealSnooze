package tom.realsnooze;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;

/**
 * TODOS:
 * 1. implement SleepDetector Destroy. this should save statistics locally and load them when its up again.
 * 2. verify the service flag is correct for re-launch
 * 3. fix log levels
 */

public class MainActivity extends Activity  {


    public static final int DEFAULT_SNOOZE_MINUTES = 2;
    public static final long COLLECTION_BREAK_TIME = 30000;
    private int snoozeMinutes = DEFAULT_SNOOZE_MINUTES;
    public static final String INTENT_PARAM_SNOOZE = "snooze";
    public static final String INTENT_PARAM_IS_ALARM = "isAlarm";
    private static final String TAG = "MainActivity";

    private boolean bound=false;
    private SleepDetector.Binder binder=null;
    private ToggleButton toggle;
    private TimePicker alarmTimePicker;
    public TextView alarmTextView;
    public TextView text2;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SleepDetector.Binder binder = (SleepDetector.Binder) service;
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };



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
        Log.e(TAG, "onResume. starting from alarm? " + alarm);
        if (alarm) {
            onAlarm(in);
        }
    }

    private void onAlarm(Intent in) {
        Log.e(TAG, "onAlarm. bound? " + bound + ". binder = " + binder);
        toggle.setChecked(true);
        try {
            Intent intent = new Intent(this, SleepDetector.class);
            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
            soundAlarm(this);
            setSnooze(this, in.getIntExtra(MainActivity.INTENT_PARAM_SNOOZE, MainActivity.DEFAULT_SNOOZE_MINUTES));
            //TODO need to fix logic to SleeoDetector.isRunning
            if (binder.isAsleep()) {
                soundAlarm(this);
                binder.wokeUp();
                setSnooze(this, in.getIntExtra(MainActivity.INTENT_PARAM_SNOOZE, MainActivity.DEFAULT_SNOOZE_MINUTES));
            }
        }
        catch (Exception e){
            Log.e(TAG,TAG,e);
        }
    }

    public void onToggleClicked(View view) {
        Log.e(TAG,"onToggle");
        if (((ToggleButton) view).isChecked()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
            setAlarm(calendar,snoozeMinutes);
        } else {
            Log.e(TAG, "Music off");
            MusicPlayer.stop();
        }
    }

    public void setAlarm(Calendar calendar,int snoozeMinutes) {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.putExtra(INTENT_PARAM_SNOOZE, snoozeMinutes);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.INTENT_PARAM_IS_ALARM, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.e(TAG, "Alarm set to " + calendar.getTime().toString());
    }

    public void setAlarmText(String alarmText) {
        alarmTextView.setText(alarmText);
    }


    private void setSnooze(Context context, int snoozeMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, snoozeMinutes);
        setAlarm(calendar, snoozeMinutes);
        Log.e(TAG, "snooze set to " + snoozeMinutes + " from now");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onAlarm(intent);
    }

    private void soundAlarm(Context context) {
        Log.e(TAG, "ALARM ALARM!!");
        MusicPlayer.start(context);
    }

    @Override
    protected void onPause() {
        Log.e(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.e(TAG,"onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG,"onDestroy");
        super.onDestroy();
    }
}
