package tom.realsnooze;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.text.DecimalFormat;

/**
 * Created by thinkPAD on 3/14/2016.
 */
public class SleepDetector implements SensorEventListener,Runnable {

    private static final int DELAY = 500000;
    private static final String TAG = "sleepDetector";
    private long lastWakeUpTime;
    private long collectionBreakTime;
    private final long THREAD_SLEEP_TIME = 10000;
    private final SensorManager sensorManager;
    private final Sensor gravity;
    private final Sensor linearAcceleration;
    MainActivity activity;
    SummaryStatistics gravityStatistics;
    SummaryStatistics accelerationStatistics;

    public SleepDetector(MainActivity mainActivity,long collectionBreakTime) {
        this.activity=mainActivity;
        this.collectionBreakTime =collectionBreakTime;
        sensorManager = (SensorManager)mainActivity.getSystemService(Context.SENSOR_SERVICE);
        gravityStatistics = new SummaryStatistics();
        accelerationStatistics = new SummaryStatistics();
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        registerSensors();
    }

    private void registerSensors() {
        sensorManager.registerListener(this,gravity,DELAY);
        sensorManager.registerListener(this, linearAcceleration, DELAY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                double speed = Math.sqrt(Math.pow(event.values[0], 2) +
                        Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
                accelerationStatistics.addValue(speed);
                activity.text2.setText("mean acceleration = "+ new DecimalFormat("###.##").format(accelerationStatistics.getMean()));
                break;
            case Sensor.TYPE_GRAVITY:
                float z = event.values[2];
                gravityStatistics.addValue(Math.abs(z));
                activity.alarmTextView.setText("mean |z| = "+new DecimalFormat("###.##").format(gravityStatistics.getMean()));
                break;
        }
    }

    public void wokeUp() {
        Log.d(TAG,"Woke up. resetting statistics and taking a break from collection of "+THREAD_SLEEP_TIME+" millis.");
        lastWakeUpTime= SystemClock.currentThreadTimeMillis();
        sensorManager.unregisterListener(this);
        gravityStatistics.clear();
        accelerationStatistics.clear();
        takeBreak();
    }

    private void takeBreak() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void run() {
        Log.d(TAG,"going to sleep for "+collectionBreakTime+" millis.");
        try {
            Thread.sleep(collectionBreakTime);
        }
        catch (InterruptedException e) {

        }
        Log.e(TAG,"sleep over. registering sensors.");
        registerSensors();
    }

    public boolean isAsleep() {
        double result =0;
        result += Math.abs(gravityStatistics.getMean())/10;
        Log.d(TAG,"isAsleep: gravity mean = "+result);
        result += (1-accelerationStatistics.getMean());
        Log.d(TAG,"isAsleep: gravity + movement = "+result);
        return result >1.2;
    }
}
