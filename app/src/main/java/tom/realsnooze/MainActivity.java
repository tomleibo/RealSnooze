package tom.realsnooze;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;

/**
 * BUGS:
 * 2. closeApp causes IllegalArgumentException. and unbinding may not work.
 * DONE:
 * V - fixed service flags.
 * V - fixed activity showing one more time after snooze.
 * V - closed app and service when user awakes.
 * V - extracted toggle behavior to act both on toggle and new time set.
 * V - onNewTimeSet also cancels all previous alarms.
 * V - added snooze field.
 * V - keyboard doesnt close
 * V - previous alarms are not cancelled - last intent number(or extra) can be saved in shared preferences and checked in alarm receiver.
 * V - alarm is on even when not asleep - check isAsleep and its usages
 * V. edit snooze not working. focus listener doesn't launch.
 * V save snooze value as shared preferences.
 * V. Save clock and toggle values in sharedPrefs and load them onCreate.
 * V. ALARM IS SHOOTING IF HOUR IS BEFORE NOW?!  OR  is alarm set to the previous day if time is before now ??
 * V - should instatiate logger with a context from which the filesystem root can be requested. (device restart is required to view in PC)
 * V - when alarm is off - delete alarm time from prefs.
 * TODO:
 * 1. how do I cancel an alarm?! check alarm time versus this time. if now is before then cancel is OK.
 * 4. improve music player.
 * 5. create a proper notification service?
 * 3. fix log levels
 */

public class MainActivity extends Activity  {


    public static final int DEFAULT_SNOOZE_MINUTES = 2;
    private static final long TIME_ALIVE_IMPLIES_NOT_FIRST_RUN = 30;
    private static final int INTENT_IDENTIFICATOR = 1234;
    private static final String PREF_SNOOZE = "snoozeMinutes";
    private static final String PREF_TIME = "time";
    private static final String PREF_ON = "on";
    public static final String INTENT_PARAM_IS_ALARM = "isAlarm";
    private static final String TAG = "MainActivity";

    private int snoozeMinutes = DEFAULT_SNOOZE_MINUTES;
    private EditText snoozeField = null;
    private TextView snoozeText = null;
    private SleepDetector.Binder binder=null;
    private ToggleButton toggle;
    private TimePicker alarmTimePicker;
    private PendingIntent pendingIntent;
    private ServiceConnection serviceConnection = new MyServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.init(getApplicationContext());
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        snoozeText = (TextView) findViewById(R.id.textView);
        snoozeField = (EditText) findViewById(R.id.editText);
        snoozeField.setText(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(PREF_SNOOZE,DEFAULT_SNOOZE_MINUTES) + "");
        snoozeField.setFocusable(true);
        alarmTimePicker = (TimePicker) findViewById(R.id.alarmTimePicker);
        toggle = (ToggleButton) findViewById(R.id.alarmToggle);
        setAlarmTimeFromPrefs(alarmTimePicker);
        setToggleFromPrefs(toggle);
        alarmTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (toggle.isChecked()) {
                    onNewTimeSet();
                }
            }
        });
        snoozeField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e(TAG,""+actionId);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    snoozeChanged(snoozeField.getText());
                }
                return false;
            }
        });
    }

    private void setToggleFromPrefs(ToggleButton toggle) {
        boolean b = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PREF_ON, false);
        toggle.setChecked(b);
    }

    private void setAlarmTimeFromPrefs(TimePicker alarmTimePicker) {
        long l = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getLong(PREF_TIME, 0);
        if (l==0) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);
        //deprecated in level 23.
        alarmTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        alarmTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
    }

    private void snoozeChanged(Editable text) {
        try {
            snoozeMinutes = Integer.parseInt(text.toString());
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(PREF_SNOOZE, snoozeMinutes);
            editor.apply();
            Log.e(TAG,"snooze changed = "+snoozeMinutes);
        }
        catch(NumberFormatException e) {
            Log.e(TAG,"Snooze Changed: Snooze field contained non-integer value",e);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume. ");
        Intent in = getIntent();
        boolean alarm = in.getBooleanExtra(INTENT_PARAM_IS_ALARM, false);
        if (alarm) {
            onAlarm(in);
        }
    }


    private void onAlarm(Intent in) {
        Log.e(TAG, "onAlarm. bound? " + (binder != null));
        toggle.setChecked(true);
        try {
            if (binder!=null) {
                alarmIfAsleepCloseIfAwake();
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

    private void alarmIfAsleepCloseIfAwake() {
        if (binder.isAsleep()) {
            SoundAlarmAndSetSnooze();
            binder.wokeUp();
        }
        else {
            closeApp();
        }
    }

    private void SoundAlarmAndSetSnooze() {
        soundAlarm(this);
        setSnooze(snoozeMinutes);
    }

    public void onToggleClicked(View view) {
        Log.e(TAG, "onToggle");
        boolean checked = toggle.isChecked();
        if (checked) {
            onNewTimeSet();
        } else {
            Log.e(TAG, "Music off");
            MusicPlayer.stop();
        }
        saveToggleStateInPrefs(checked);
    }

    private void saveToggleStateInPrefs(boolean checked) {
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putBoolean(PREF_ON,checked);
        editor.apply();
    }

    private void onNewTimeSet() {
        cancelPreviousAlarms();
        Calendar alarmTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
        alarmTime.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
        if (now.before(alarmTime)) {
            alarmTime.add(Calendar.DATE,1);
        }

        setAlarm(alarmTime);
        saveAlarmTimeToPrefs(alarmTime);
    }

    private void saveAlarmTimeToPrefs(Calendar calendar) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putLong(PREF_TIME, calendar.getTimeInMillis());
        editor.apply();
    }

    private void cancelPreviousAlarms() {
        Log.e(TAG, "previous alarms cancelled");
        if (pendingIntent!=null) {
            ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
            pendingIntent=null;
        }
    }

    public void setAlarm(Calendar calendar) {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.INTENT_PARAM_IS_ALARM, true);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), INTENT_IDENTIFICATOR, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.e(TAG, "Alarm set to " + calendar.getTime().toString());
    }


    private void setSnooze(int snoozeMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, snoozeMinutes);
        setAlarm(calendar);
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
        Log.e(TAG, "closing app");
        deleteAlarmPrefs();
        stopService(new Intent(this, SleepDetector.class));
        if (binder!=null) {
            try {
                unbindService(serviceConnection);
            }
            catch(IllegalArgumentException e) {
                Log.e(TAG,"error during service unbinding.",e);
            }
        }
        finish();
    }

    private void deleteAlarmPrefs() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(PREF_TIME);
        editor.remove(PREF_ON);
        editor.commit();
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "Service connected.");
            binder = (SleepDetector.Binder) service;
            if (binder.getTimeAliveSeconds() > TIME_ALIVE_IMPLIES_NOT_FIRST_RUN) {
                Log.e(TAG,"this is not the first run of the service.");
                alarmIfAsleepCloseIfAwake();
                return;
            }
            Log.e(TAG,"first run of the service just sounding alarm and setting snooze.");
            SoundAlarmAndSetSnooze();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder = null;
        }
    }
}
