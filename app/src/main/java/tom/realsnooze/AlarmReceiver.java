package tom.realsnooze;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private SleepDetector sleepDetector =null;
    private static final String TAG = "AlarmReceiver";


    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG, "got broadcast. sleep detector is" + (sleepDetector == null ? " not" : "") + " null.");

        intent.setClassName("tom.realsnooze","tom.realsnooze.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.INTENT_PARAM_IS_ALARM, true);
        context.startActivity(intent);

        //setText();
        //soundAlarm(context);
        //startService(context, intent);
        setResultCode(Activity.RESULT_OK);
    }

    private void startService(Context context, Intent intent) {
        //this will send a notification message
        ComponentName comp = new ComponentName(context.getPackageName(),
                NotificationService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
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
