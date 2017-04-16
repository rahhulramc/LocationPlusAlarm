package com.example.abhisheikh.locationplusalarm.geofence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.R;
import com.example.abhisheikh.locationplusalarm.StaticWakeLock;
import com.example.abhisheikh.locationplusalarm.activity.AlarmListActivity;
import com.example.abhisheikh.locationplusalarm.activity.RingingActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhisheikh on 12/4/17.
 */

public class GeoFenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "gfservice";
    String requestID;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeoFenceTransitionsIntentService(String name) {
        super(name);
    }

    public GeoFenceTransitionsIntentService(){
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()){
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.d(TAG,errorMessage);
            return;
        }

        //Alarm alarm = intent.getParcelableExtra("alarm");

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_ENTER||
                geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT||
                geofenceTransition==Geofence.GEOFENCE_TRANSITION_DWELL){

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String locationName = "",ringToneID = "";
            boolean vibration = false;

            try {
                requestID = triggeringGeofences.get(0).getRequestId();
                JSONObject baseObject = new JSONObject(requestID);
                locationName = baseObject.getString("location_name");
                ringToneID = baseObject.getString("ringtone_id");
                vibration = baseObject.getBoolean("vibration");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String geofenceTransitionDetails = getTransitionString(geofenceTransition)+": "+locationName;
            Toast.makeText(getBaseContext(),ringToneID,Toast.LENGTH_SHORT).show();
            sendNotification(geofenceTransitionDetails);
            StaticWakeLock.lockOn(getApplicationContext());
            startRingingActivity(geofenceTransitionDetails,ringToneID,vibration);

            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }

    }

    private void startRingingActivity(String notificationDetail, String ringtoneID, boolean vibration){
        Intent intent = new Intent(getApplicationContext(), RingingActivity.class);
        intent.putExtra("notification",notificationDetail)
                .putExtra("ringtone_id",ringtoneID)
                .putExtra("request_id",requestID)
                .putExtra("vibration",vibration);
        //intent.putExtra("alarm",alarm);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), AlarmListActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(AlarmListActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

/*
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }
*/

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}
