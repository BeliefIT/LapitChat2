package solutionexpert.org.lapitchat2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by sachin on 06-Feb-18.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private NotificationUtilsOreo mNotificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();

        String from_user_id = remoteMessage.getData().get("from_user_id");
        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();


        if (Build.VERSION.SDK_INT >= 26) {

            mNotificationUtils = new NotificationUtilsOreo(this);
            Notification.Builder mBuilder = mNotificationUtils.
                    getAndroidChannelNotification(notification_title, notification_message);

            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("user_id", from_user_id);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);


            // Builds the notification and issues it.
            mNotificationUtils.getManager().notify(mNotificationId, mBuilder.build());
        } else {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(notification_title)
                            .setContentText(notification_message);

            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("user_id", from_user_id);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);

            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());

        }
    }
}