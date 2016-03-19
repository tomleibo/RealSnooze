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
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;

/**
 * TODOS:
 * V - fixed service flags.
 * V - fixed activity showing one more time after snooze.
 * V - closed app and service when awake.
 * V - extracted toggle behavior to act both on toggle and new time set.
 * V - onNewTimeSet also cancels all previous alarms.
 *
 * 1. set snooze input field.
 * 2. save snooze value as shared preferences.
 * 4. improve music player.
 * 5. create a proper notification service?
 * 3. fix log levels
 */

public class MainActivity extends Activity  {


    public static final int DEFAULT_SNOOZE_MINUTES = 2;
    private static final long TIME_ALIVE_IMPLIES_NOT_FIRST_RUN = 30;
    private int snoozeMinutes = DEFAULT_SNOOZE_MINUTES;
    public static final String INTENT_PARAM_IS_ALARM = "isAlarm";
    private static final String TAG = "MainActivity";

    private SleepDetector.Binder binder=null;
    private ToggleButton toggle;
    private TimePicker alarmTimePicker;
    private PendingIntent pendingIntent;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG,"Service connected.");
            binder = (SleepDetector.Binder) service;
            if (binder.getTimeAliveSeconds() > TIME_ALIVE_IMPLIES_NOT_FIRST_RUN) {
                Log.e(TAG,"this is not the first run of the service.");
                if (binder.isAsleep()) {
                    SoundAlarmAndSetSnooze();
                    binder.wokeUp();
                }
                else {
                    closeApp();
                }
                return;
            }
            Log.e(TAG,"first run of the service just sounding alarm and setting snooze.");
            SoundAlarmAndSetSnooze();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        toggle = (ToggleButton) findViewById(R.id.alarmToggle);
        alarmTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (toggle.isChecked()) {
                    onNewTimeSet();
                }
            }
        });
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
        Log.e(TAG, "onAlarm. bound? " + (binder != null));
        toggle.setChecked(true);
        try {
            if (binder!=null) {
                if (binder.isAsleep()) {
                    SoundAlarmAndSetSnooze();
                    binder.wokeUp();
                }
                else {
                    toggle.setChecked(false);
                }
            }
            else {
                Intent intent = new Intent(this, SleepDetector.class);
                getApplicationContext().startService(intent);
                getApplicationContext().bindService(intent, serviceConnection, Context.BIND_ABOVE_CLIENT);
            }
        }
        catch (Exception e){
            Log.e(TAG,TAG,e);
        }
    }

    private void SoundAlarmAndSetSnooze() {
        soundAlarm(this);
        setSnooze(MainActivity.DEFAULT_SNOOZE_MINUTES);
    }

    public void onToggleClicked(View view) {
        Log.e(TAG, "onToggle");
        if (toggle.isChecked()) {
            onNewTimeSet();
        } else {
            Log.e(TAG, "Music off");
            MusicPlayer.stop();
        }
    }

    private void onNewTimeSet() {
        cancelPreviousAlarms();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
        setAlarm(calendar, snoozeMinutes);
    }

    private void cancelPreviousAlarms() {
        if (pendingIntent!=null) {
            ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
            pendingIntent=null;
        }
    }


    public void setAlarm(Calendar calendar,int snoozeMinutes) {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.INTENT_PARAM_IS_ALARM, true);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.e(TAG, "Alarm set to " + calendar.getTime().toString());
    }


    private void setSnooze(int snoozeMinutes) {
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

    private void closeApp() {
        stopService(new Intent(this, SleepDetector.class));
        if (binder!=null) {
            unbindService(serviceConnection);
        }
        finish();
    }


}
