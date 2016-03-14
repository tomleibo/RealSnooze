package tom.realsnooze;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class MusicPlayer {

    private static Ringtone ringtone;

    public static void start(Context context) {
        //this will sound the alarm tone
        //this will sound the alarm once, if you wish to
        //raise alarm in loop continuously then use MediaPlayer and setLooping(true)
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.setStreamType(AudioManager.STREAM_ALARM);
        ringtone.play();
    }

    public static void stop() {
        if (ringtone!=null){
            ringtone.stop();
        }
    }
}
