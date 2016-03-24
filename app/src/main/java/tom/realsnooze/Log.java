package tom.realsnooze;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by thinkPAD on 3/24/2016.
 */
public class Log {
    private static final String BASE_PATH = "sdcard/real_snooze/log";
    private static File file;
    static {
        file = new File(BASE_PATH);
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (!file.exists()) {
            System.exit(123);
        }
    }

    public static void log(String tag, String s, char logLevel) {
        switch (logLevel) {
            case 'd':
                android.util.Log.d(tag,s);
                break;
            case 'i':
                android.util.Log.i(tag,s);
                break;
            case 'e':
                android.util.Log.e(tag,s);
                break;
            case 'f':
                android.util.Log.wtf(tag, s);
                break;
            case 'w':
                android.util.Log.w(tag,s);
                break;
            case 'v':
                android.util.Log.v(tag,s);
                break;
        }
        try
        {
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append("Level: "+Character.toUpperCase(logLevel)+" TAG: "+tag+" MSG: "+s);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void log(String tag,String s,Throwable t,char logLevel) {
        log(tag,"dev msg: "+s+". Exception thrown: "+t.getLocalizedMessage(),logLevel);
    }

    public static void e(String tag,String msg) {
        log(tag,msg,'e');
    }

    public static void e(String tag,String msg, Throwable t) {
        log(tag,msg,t,'e');
    }
}
