package tom.realsnooze;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private SleepDetector sleepDetector =null;
    private static final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e(TAG,"got broadcast");


        intent.setClassName("tom.realsnooze","tom.realsnooze.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

}
