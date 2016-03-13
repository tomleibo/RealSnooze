package tom.realsnooze;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * Created by thinkPAD on 3/14/2016.
 */
public class MusicPlayer {

    private static Ringtone ringtone;

    public static void start(Context context) {
        //this will sound the alarm tone
        //this will sound the alarm once, if you wish to
        //raise alarm in loop continuously then use MediaPlayer and setLooping(true)
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();

    }

    public static void stop() {
        if (ringtone!=null){
            ringtone.stop();
        }
    }
}
