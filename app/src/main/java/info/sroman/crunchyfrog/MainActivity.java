package info.sroman.crunchyfrog;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        System.out.println("Activity started");
        super.onCreate(bundle);
        createNotificationChannel();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        this.registerReceiver(WifiScanReceiverFactory.getInstance(), intentFilter);

        this.startForegroundService(new Intent().setClass(this, SensorCollectionService.class));
//        this.startForegroundService(new Intent().setClass(this, WifiCollectionService.class));
        finish();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defNotifyChann =
                    new NotificationChannel(CrunchyGlobals.NOTIFICATION_CHANNEL_ID, "SENSOR NOTIFY", NotificationManager.IMPORTANCE_HIGH);
            defNotifyChann.setDescription("SENSOR COLLECTION SERVICE NOTIFICATION CHANNEL");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(defNotifyChann);
        }
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(WifiScanReceiverFactory.getInstance());
        super.onDestroy();
    }
}
