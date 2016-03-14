package tom.realsnooze;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Created by thinkPAD on 3/14/2016.
 */
public class SleepDetector extends Service implements SensorEventListener,Runnable {

    private static final int DELAY = 500000;
    private static final String TAG = "sleepDetector";
    private long lastWakeUpTime;
    private long collectionBreakTime;
    private final SensorManager sensorManager;
    private final Sensor gravity;
    private final Sensor linearAcceleration;
    private Context activity;
    SummaryStatistics gravityStatistics;
    SummaryStatistics accelerationStatistics;

    public SleepDetector(Context mainActivity,long collectionBreakTime) {
        Log.e(TAG,"constructing with breakTime = "+collectionBreakTime);
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
                break;
            case Sensor.TYPE_GRAVITY:
                float z = event.values[2];
                gravityStatistics.addValue(Math.abs(z));
                break;
        }
    }

    public void wokeUp() {
        Log.e(TAG, "Woke up. resetting statistics and taking a break from collection of " + collectionBreakTime + " millis.");
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
        Log.e(TAG, "going to sleep for " + collectionBreakTime + " millis.");
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
        Log.e(TAG,"isAsleep: result is based on "+gravityStatistics.getN()+" gravity events and "+accelerationStatistics.getN()+" movement events");
        result += Math.abs(gravityStatistics.getMean())/10;
        Log.e(TAG,"isAsleep: gravity mean = "+result);
        result += (1-accelerationStatistics.getMean());
        Log.e(TAG,"isAsleep: gravity + movement = "+result);
        return result < 1.2;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
