package info.sroman.crunchyfrog;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        createNotificationChannel();
        System.out.println("Activity started");
        this.startForegroundService(new Intent().setClass(this, SensorCollectionService.class));
        finish();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel defNotifyChann =
                    new NotificationChannel("SENSOR NOTIFY", "SENSOR NOTIFY", NotificationManager.IMPORTANCE_HIGH);
            defNotifyChann.setDescription("SENSOR COLLECTION SERVICE NOTIFICATION CHANNEL");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(defNotifyChann);
        }
    }

}
