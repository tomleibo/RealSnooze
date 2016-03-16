package tom.realsnooze;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Created by thinkPAD on 3/14/2016.
 */
public class SleepDetector extends Service implements SensorEventListener,Runnable {

    private static final int SENSOR_FREQUENCY = 500000;
    private static final long COLLECTION_BREAK_TIME = 30000;
    private static final String TAG = "sleepDetector";

    private SensorManager sensorManager;
    private Sensor gravity;
    private Sensor linearAcceleration;
    private SummaryStatistics gravityStatistics;
    private SummaryStatistics accelerationStatistics;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        gravityStatistics = new SummaryStatistics();
        accelerationStatistics = new SummaryStatistics();
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        registerSensors();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    private void registerSensors() {
        sensorManager.registerListener(this,gravity, SENSOR_FREQUENCY);
        sensorManager.registerListener(this, linearAcceleration, SENSOR_FREQUENCY);
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
        Log.e(TAG, "Woke up. resetting statistics and taking a break from collection of " + COLLECTION_BREAK_TIME + " millis.");
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
        Log.e(TAG, "going to sleep for " + COLLECTION_BREAK_TIME + " millis.");
        try {
            Thread.sleep(COLLECTION_BREAK_TIME);
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
        Log.e(TAG, "isAsleep: gravity + movement = " + result);
        return result < 1.2;
    }

    public class Binder extends android.os.Binder {
        public boolean isAsleep() {
            return SleepDetector.this.isAsleep();
        }

        public void wokeUp() {
            SleepDetector.this.wokeUp();
        }
    }
}
