package info.sroman.crunchyfrog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Arrays;
import java.util.logging.Logger;

public class SensorCollectionService extends IntentService implements SensorEventListener {

    private static final Logger log = Logger.getLogger("LOGGER");
    private static int LOG_LINES = 0;
    private static float RUN_TIME = 0.0F;

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
        startServiceAndNotify();
        startTimer();

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

    public void log(String msg) {
//        String time = DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis()));
        log.info(++LOG_LINES + " " + RUN_TIME + ": " + msg);
    }

    public void logSensorData(float[] oldData, SensorEvent event){
        if (!Arrays.equals(oldData, event.values)) {
            oldData = event.values;
            log(event.sensor.getName() + ": " + Arrays.toString(event.values));
        }
    }

    private void startServiceAndNotify() {
        NotificationManagerCompat nManager = NotificationManagerCompat.from(this);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( this, "SENSOR NOTIFY")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Title")
                .setContentText("Content text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        nManager.notify(14, mBuilder.build());

        this.startForeground(14, mBuilder.build());
    }

    private void startTimer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000L);
                        RUN_TIME++;
                    } catch (InterruptedException e) {
                        log(e.getMessage());
                    }

                }
            }
        }).start();
    }

    private void checkDeviceSensors() {
        PackageManager manager = getPackageManager();
        boolean hasAccelerometer = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        boolean hasLight = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT);
        boolean hasTemperature = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE);
        boolean hasPressure = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_BAROMETER);

    }

}
