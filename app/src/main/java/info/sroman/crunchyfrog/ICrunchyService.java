package info.sroman.crunchyfrog;

import android.app.IntentService;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

public interface ICrunchyService {
    default void startServiceAndNotify(Context context,
                                       IntentService service,
                                       String channelId,
                                       String title,
                                       String text) {
        NotificationManagerCompat nManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder( context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        nManager.notify(14, mBuilder.build());

        service.startForeground(14, mBuilder.build());
    }

    default void makePost(String url, String msg) {
        RequestQueue queue = VolleyQueueFactory.getInstance((Context) this);
        StringRequest stringRequest =
                new StringRequest(
                        Request.Method.POST,
                        url,
                        System.out::println,
                        System.out::println
                );
            queue.add(stringRequest);
        }
}
