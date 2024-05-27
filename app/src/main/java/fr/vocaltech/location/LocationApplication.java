package fr.vocaltech.location;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class LocationApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        NotificationChannel notificationChannel = new NotificationChannel(
                "location_notification",
                "Location",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
