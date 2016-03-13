package tom.realsnooze;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        intent.setClassName("tom.realsnooze","tom.realsnooze.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        //setText();
        //soundAlarm(context);
        //startService(context, intent);
        setResultCode(Activity.RESULT_OK);
    }

    private void setText() {
        MainActivity inst = MainActivity.instance();
        inst.setAlarmText("Alarm! Wake up! Wake up!");
    }

    private void startService(Context context, Intent intent) {
        //this will send a notification message
        ComponentName comp = new ComponentName(context.getPackageName(),
                NotificationService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
    }

    private void soundAlarm(Context context) {
        MusicPlayer.start(context);
    }
}
