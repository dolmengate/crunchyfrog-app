package info.sroman.crunchyfrog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class SensorCollectionService extends IntentService implements SensorEventListener, ICrunchyService {

    public static SensorCollectionService service;

    private static final String URL = "";

    private static long lastRequestTime = 0;
    private static final long MAX_REQUEST_DELAY = 1000 * 1; // 1 seconds
    private static final int MAX_QUEUE_SIZE = 100;

    private static Queue<SensorEvent> sensorEventQueue = new LinkedTransferQueue<>();

    private static final int SAMPLING_DELAY = Integer.MAX_VALUE;
    private static final int MAX_REPORT_LATENCY = 5 * 60 * 100; // in microseconds

    private Sensor sAccelerometer;

    private float[] lastAccel= new float[3];

    public SensorCollectionService() {
        super("SensorCollectionService");
    }

    @Override
    public void onCreate() {
        service = this;
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        startCheckSensorEventQueueThread();
        this.startServiceAndNotify(
                this,
                this,
                CrunchyGlobals.NOTIFICATION_CHANNEL_ID,
                "Sensor Collection Service",
                "Service started"
        );

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        System.out.println("service started");

        if(sAccelerometer != null)
            sensorManager.registerListener(this, sAccelerometer, SAMPLING_DELAY, MAX_REPORT_LATENCY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sAccelerometer != null)
            if (event.sensor.getName().equals(sAccelerometer.getName()))
            {
                enqueueNewSensorData(event);
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    public void enqueueNewSensorData(SensorEvent event){
        if (!Arrays.equals(lastAccel, event.values)) {
            lastAccel = Arrays.copyOf(event.values, event.values.length);
            sensorEventQueue.add(event);
        }
    }

    private void startCheckSensorEventQueueThread() {
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(2000L);
                    long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
                    if (sensorEventQueue.size() >= MAX_QUEUE_SIZE || timeSinceLastRequest > MAX_REQUEST_DELAY) {
                        dumpSensorQueue();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private SensorEvent[] decimateSensorData(Object[] data) {
        for (int i = 0; i < data.length; i++)
            if (i % 10 != 0) data[i] = null;

        int i = 0;
        int j = 0;
        int len = 0;
        while (i < data.length) {
            if (data[i] == null) {
                j = i + 1;
                while (j < data.length) {
                    if (data[j] != null) {
                        data[i] = data[j];
                        data[j] = null;
                        len = i+1;
                        break;
                    }
                    j++;
                }
            }
            i++;
        }
        return Arrays.copyOf(data, len, SensorEvent[].class);
    }

    public void dumpSensorQueue() {
        Gson gson = new Gson();
        Object[] arr = sensorEventQueue.toArray();
        String json = gson.toJson(decimateSensorData(arr));
//        this.makePost(URL, json);
        lastRequestTime = System.currentTimeMillis();
        System.out.println(json);
        sensorEventQueue.clear();
    }
}
