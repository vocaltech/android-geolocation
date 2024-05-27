package fr.vocaltech.location.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TrackService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /*
    // TODO: remove property android:usesCleartextTraffic="true" in AndroidManifest.xml file
    private static final String TAG = "TrackService";
    private String url = BuildConfig.TRACK_URL;
    private SharedPreferences prefs;
    private String user_id;

    // TODO: To migrate to PendingIntent
    // update activity
    private Callbacks activity;
    
    // Binder given to the client
    private final IBinder binder = new TrackBinder();

    // class used for the client binder
    public class TrackBinder extends Binder {
        public TrackService getService() {
            return TrackService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { // Required method from 'Service' class
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");

        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        user_id = prefs.getString("user_id", "mock_user_id");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: ");
    }

    public void postTrack(String trackName) {
        //
        // TODO: check connectivity to server
        //      - if available, then post
        //      - else, save locally the queue
        //
        // isServerReachable = ...

        //
        // timestamp format saved in MongoDB:
        //      2021-10-19T22:29:34.205+00:00
        //
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String timestamp = sdf.format(new Date());

        // generate current track
        String jsonCurrentTrack = "{\"name\":\"" + trackName + "\","
                + "\"start_time\":\"" + timestamp + "\","
                + "\"user_id\": \"" + user_id + "\" }";

        Log.d(TAG, "postTrack: " + jsonCurrentTrack);

        try {
            OkHttpUtils.postJson(url, jsonCurrentTrack, new OkHttpApiCallback() {
                @Override
                public void onOkHttpResponse(String data) {
                    try {
                        JSONObject jsonTrack = new JSONObject(data);
                        String track_id = jsonTrack.getString("id");

                        Track currentTrack = new Track();
                        currentTrack.setId(track_id);
                        currentTrack.setName(jsonTrack.getString("name"));
                        currentTrack.setStartTime(jsonTrack.getString("start_time"));
                        currentTrack.setUserId(jsonTrack.getString("user_id"));

                        // update prefs
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("track_id", track_id);
                        editor.apply();

                        // update UI
                        ((MainActivity)activity).runOnUiThread(() -> ((MainActivity) activity).updateTrackFragment(currentTrack));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onOkHttpFailure(Exception e) {
                    Log.d(TAG, "onOkHttpFailure: " + e);
                    // java.net.SocketTimeoutException: failed to connect to /10.0.2.2 (port 4500) from /10.0.2.16 (port 47860) after 10000ms
                    // java.net.SocketTimeoutException: failed to connect to ngtracking.vocality.fr/95.142.160.207 (port 443) from /10.0.2.16 (port 55600) after 10000ms
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    // TODO: PendingIntent/LocalBroadcastManager
    /*
    private void sendMessageToActivity(String message) {
        Intent intent = new Intent();
        intent.setAction(SENDMESAGGE);
        intent.putExtra("message",message);

        //LocalBroadcastManager.getInstance(TrackService.this).sendBroadcast(intent);
        sendBroadcast(intent);
    }
    */

    /*
    // TODO: Remove callbacks and replace w/PendingIntent
    // activity registers to the services as Callbacks client
    public void registerClient(Activity activity) {
        this.activity = (Callbacks) activity;
    }

    // TODO: Remove callbacks
    public interface Callbacks {
        void updateTrackFragment(Track track);
    }
    */
}
