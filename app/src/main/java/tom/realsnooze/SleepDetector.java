package tom.realsnooze;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.text.DecimalFormat;

/**
 * Created by thinkPAD on 3/14/2016.
 */
public class SleepDetector implements SensorEventListener {

    private static final int DELAY = 500000;
    private final SensorManager sensorManager;
    private final Sensor gravity;
    private final Sensor linearAcceleration;
    MainActivity activity;
    private float lastMeasurement;
    SummaryStatistics gravityStatistics;
    SummaryStatistics accelerationStatistics;

    public SleepDetector(MainActivity mainActivity) {
        this.activity=mainActivity;
        sensorManager = (SensorManager)mainActivity.getSystemService(Context.SENSOR_SERVICE);
        gravityStatistics = new SummaryStatistics();
        accelerationStatistics = new SummaryStatistics();
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this,gravity,DELAY);
        sensorManager.registerListener(this, linearAcceleration,DELAY);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
