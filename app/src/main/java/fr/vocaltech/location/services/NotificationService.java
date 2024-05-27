package fr.vocaltech.location.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import fr.vocaltech.location.R;

public class NotificationService {
    private Context context;
    private NotificationManager notificationManager;

    public NotificationService(Context context) {
        this.context = context;
        notificationManager = context.getSystemService(NotificationManager.class);
    }

    public Notification createNotification(boolean notifyManager) {
        Notification notification = new NotificationCompat.Builder(context, "location_notification")
                .setContentTitle("Location Notification")
                .setContentText("Location service is started")
                .setSmallIcon(R.drawable.location)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setAutoCancel(true)
                .build();

        if (notifyManager)
            notificationManager.notify(1, notification);

        return notification;
    }

    public void hideNotification() {
        //notificationManager.deleteNotificationChannel("location_notification");
        notificationManager.cancel(1);
    }

}
