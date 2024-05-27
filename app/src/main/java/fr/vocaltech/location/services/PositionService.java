package fr.vocaltech.location.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.vocaltech.location.models.Coordinates;
import fr.vocaltech.location.models.Position;
import fr.vocaltech.location.services.rabbitmq.PublishMQService;

public class PositionService extends Service {
    private final static String TAG = "PositionService";
    private static final int NOTIFICATION_ID = 1001;
    private static  final String RABBIT_QUEUE = "android-users-locations";
    private static final int LOCATION_REQUEST_PRIORITY = Priority.PRIORITY_HIGH_ACCURACY;
    private static final int LOCATION_REQUEST_INTERVAL = 5000;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private NotificationService notificationService;
    private PublishMQService publishMQService;
    private ThreadPoolExecutor executor;
    private Position _currentPosition;
    private boolean _isTrackingLocationStarted;
    private final Intent intentForActivity = new Intent("PositionService");
    private String user_id = "userId", track_id = "trackId";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "[onBind()]");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "[onCreate()]");

        super.onCreate();

        createLocationRequest();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(Objects.requireNonNull(locationResult.getLastLocation()));
            }
        };

        // --- init rabbitMq and executor ---
        publishMQService = new PublishMQService();
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors(); // Determine the number of cores on the device
        executor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                1L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[onDestroy()]");

        super.onDestroy();

        // --- service section ---
        stopTrackingLocation();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "[onStartCommand()]");

        // --- start service with notification ---
        notificationService = new NotificationService(this);
        Notification notification = notificationService.createNotification(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        startTrackingLocation();
        return START_STICKY;
    }

    private void startTrackingLocation() {
        Log.d(TAG, "[startTrackingLocation()]");

        _isTrackingLocationStarted = true;

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException exc) {
            Log.e(TAG, "getLastKnownLocation: Lost location permission: ", exc);
        }
    }

    private void stopTrackingLocation() {
        Log.d(TAG, "[stopTrackingLocation()]");

        if (_isTrackingLocationStarted)
            _isTrackingLocationStarted = false;

        notificationService.hideNotification();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void createLocationRequest() {
        Log.d(TAG, "[createLocationRequest()]");

        locationRequest = new LocationRequest.Builder(LOCATION_REQUEST_INTERVAL)
                .setPriority(LOCATION_REQUEST_PRIORITY)
                .build();
    }

    protected void onNewLocation(Location location) {
        double curLat = location.getLatitude();
        double curLng = location.getLongitude();
        long curTs = location.getTime();

        _currentPosition = new Position(
                new Coordinates(curLat, curLng),
                curTs,
                track_id,
                user_id
        );

        // --- log section ---
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        String strCurTs = sdf.format(new Date((_currentPosition.getTimestamp())));

        // serialize _currentPosition into JSON
        String jsonPosition;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonPosition = objectMapper.writeValueAsString(_currentPosition);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        executor.execute(() -> publishMQService.publishToQueue(RABBIT_QUEUE, jsonPosition));

        // --- send broadcast ---
        intentForActivity.putExtra("lat", curLat);
        intentForActivity.putExtra("lng", curLng);
        intentForActivity.putExtra("ts", curTs);
        sendBroadcast(intentForActivity);
    }
}
