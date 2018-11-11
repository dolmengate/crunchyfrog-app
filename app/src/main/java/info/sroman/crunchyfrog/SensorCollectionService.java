package info.sroman.crunchyfrog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import java.util.Arrays;

public class SensorCollectionService extends IntentService implements SensorEventListener, ICrunchyService {

    private static int SENSOR_COLLECTION_SERVICE_RUNTIME = 0;

    private static int SAMPLING_RATE = Integer.MAX_VALUE;
    private static int MAX_REPORT_LATENCY = 5 * 60 * 100; // in microseconds

    private Sensor sTemperature;
    private Sensor sAccelerometer;
    private Sensor sLight;
    private Sensor sPressure;

    private float[] lastPressure = new float[1];
    private float[] lastTemp = new float[1];
    private float[] lastAccel= new float[3];
    private float[] lastLight = new float[1]; // in lux

    public SensorCollectionService() {
        super("SensorCollectionService");
    }

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        startTimer();
        this.startServiceAndNotify(
                this,
                this,
                CrunchyGlobals.NOTIFICATION_CHANNEL_ID,
                "Sensor Collection Service",
                "Service started"
        );

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sTemperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        log("service started");

        if(sPressure != null)
            sensorManager.registerListener(this, sPressure, SAMPLING_RATE, MAX_REPORT_LATENCY);
        if(sTemperature != null)
            sensorManager.registerListener(this, sTemperature, SAMPLING_RATE, MAX_REPORT_LATENCY);
        if(sAccelerometer != null)
            sensorManager.registerListener(this, sAccelerometer, SAMPLING_RATE, MAX_REPORT_LATENCY);
        if(sLight != null)
            sensorManager.registerListener(this, sLight, SAMPLING_RATE, MAX_REPORT_LATENCY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sTemperature != null)
            if (event.sensor.getName().equals(sTemperature.getName()))
            {
                logSensorData(lastTemp, event);
            }

        if (sAccelerometer != null)
            if (event.sensor.getName().equals(sAccelerometer.getName()))
            {
                logSensorData(lastAccel, event);
            }

        if (sPressure != null)
            if (event.sensor.getName().equals(sPressure.getName()))
            {
                logSensorData(lastPressure, event);
            }

        if (sLight != null)
            if (event.sensor.getName().equals(sLight.getName()))
            {
                logSensorData(lastLight, event);
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        log("Sensor " + sensor.getName() + " changed to accuracy: " + accuracy);
    }

    public void logSensorData(float[] oldData, SensorEvent event){
        if (!Arrays.equals(oldData, event.values)) {
            oldData = event.values;
            log(event.sensor.getName() + ": " + Arrays.toString(event.values));
        }
    }

    public void log(String msg) {
        System.out.println(SENSOR_COLLECTION_SERVICE_RUNTIME + "s : " + msg);
    }

    /**
     * for testing
     */
    private void startTimer() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000L);
                    SENSOR_COLLECTION_SERVICE_RUNTIME++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
