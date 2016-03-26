package tom.realsnooze;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by thinkPAD on 3/24/2016.
 */
public class Log {
    private static final String FILE_NAME = "log";
    private static File file=null;
    public static void init(Context context) {
        if (file!=null){
            return;
        }
        file = new File(Environment.getExternalStorageDirectory().toString()+"/"+FILE_NAME+new Date().getTime());
        if (!file.exists())
        {
            try
            {
                file.getParentFile().mkdirs();
                file.createNewFile();
                for (File f :file.getParentFile().listFiles()){
                    android.util.Log.e("FILE", f.getAbsolutePath());
                }
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

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
