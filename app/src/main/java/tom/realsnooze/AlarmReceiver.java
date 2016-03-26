package tom.realsnooze;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "AlarmReceiver";


    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.init(context);
        Log.e(TAG, "got broadcast. ");
        intent = new Intent(context,MainActivity.class);
        //intent.setClassName("tom.realsnooze","tom.realsnooze.MainActivity");
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

    private void soundAlarm(Context context) {
        Log.e(TAG, "ALARM ALARM!!");
        MusicPlayer.start(context);
    }

}
