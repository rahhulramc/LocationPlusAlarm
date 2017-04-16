package com.example.abhisheikh.locationplusalarm.adapter;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.R;
import com.example.abhisheikh.locationplusalarm.activity.EditAlarmActivity;
import com.example.abhisheikh.locationplusalarm.database.DatabaseHandler;
import com.example.abhisheikh.locationplusalarm.geofence.GeoFenceTransitionsIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhisheikh on 14/4/17.
 */

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    List<Alarm> alarmList;
    Context context;
    GoogleApiClient mGoogleApiClient;

    public class AlarmViewHolder extends RecyclerView.ViewHolder{

        TextView nameTextView, labelTextView;
        ImageButton deleteAlarmButton;
        SwitchCompat alarmActivateSwitch;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView)itemView.findViewById(R.id.destinationNameTextView);
            labelTextView = (TextView)itemView.findViewById(R.id.destinationLabelTextView);
            deleteAlarmButton = (ImageButton)itemView.findViewById(R.id.deleteAlarmButton);
            alarmActivateSwitch = (SwitchCompat)itemView.findViewById(R.id.alarmActivateSwitch);
        }
    }

    public AlarmAdapter(Context context, List<Alarm> alarmList) {
        this.alarmList = alarmList;
        this.context = context;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        connetApiClient();
    }

    private void connetApiClient(){
        if(mGoogleApiClient!=null && !mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            //Toast.makeText(context,"Google Api Connected",Toast.LENGTH_SHORT).show();
        }
            //Toast.makeText(context,"Google Api not connected",Toast.LENGTH_SHORT).show();
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.alarm_list_row,parent,false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder holder, final int position) {

        final Alarm alarm = alarmList.get(position);
        holder.nameTextView.setText(alarm.getLocationName());
        holder.labelTextView.setText(alarm.getLabel());
        holder.alarmActivateSwitch.setChecked(alarm.isActive());
        holder.alarmActivateSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context,""+alarm.isActive(),Toast.LENGTH_SHORT).show();
                if (!holder.alarmActivateSwitch.isChecked()){
                    //Toast.makeText(context,"Alarm "+position+" Deactivated",Toast.LENGTH_SHORT).show();
                    alarm.setActive(false);
                    removeGeofence(alarm);
                }else {
                    //Toast.makeText(context,"Alarm "+position+" Activated",Toast.LENGTH_SHORT).show();
                    alarm.setActive(true);
                    addGeofence(alarm);
                }

                Alarm newAlarm = new Alarm(alarm);
                Alarm oldAlarm = new Alarm(alarm);
                oldAlarm.setActive(!newAlarm.isActive());
                /*Toast.makeText(context,"Old "+oldAlarm.isActive(),Toast.LENGTH_SHORT).show();
                Toast.makeText(context,"New "+newAlarm.isActive(),Toast.LENGTH_SHORT).show();*/

                DatabaseHandler dh = new DatabaseHandler(context);
                dh.updateDatabase(dh,oldAlarm,newAlarm);
            }
        });

        holder.deleteAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmList.remove(position);
                removeGeofence(alarm);
                DatabaseHandler dh = new DatabaseHandler(context);
                dh.deleteFromDatabase(dh, alarm);
                Toast.makeText(context,"Alarm "+alarm.getLabel()+" Deleted",Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,EditAlarmActivity.class);
                intent.putExtra("alarm",alarmList.get(position));
                intent.putExtra("position",""+position);
                ((AppCompatActivity)context).startActivityForResult(intent,1);
            }
        });

    }

    private void removeGeofence(Alarm alarm){
        if(mGoogleApiClient.isConnected()) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getRequestIDList(alarm));
            //Toast.makeText(context,"removed",Toast.LENGTH_SHORT).show();
            return;
        }
        //Toast.makeText(context,"Not connected",Toast.LENGTH_SHORT).show();
    }

    private List<String> getRequestIDList(Alarm alarm){
        JSONObject object= new JSONObject();
        try {
            object.put("location_name",alarm.getLocationName());
            object.put("ringtone_id",alarm.getRingtoneId());
            object.put("vibration",alarm.isVibrate());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<String> list = new ArrayList<>();
        list.add(object.toString());
        return list;
    }

    public void addGeofence(Alarm alarm) {
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            //Toast.makeText(context,"Google API Client is not connected",Toast.LENGTH_SHORT).show();
            mGoogleApiClient.connect();
        }

        JSONObject object= new JSONObject();
        try {
            object.put("location_name",alarm.getLocationName());
            object.put("ringtone_id",alarm.getRingtoneId());
            object.put("vibration",alarm.isVibrate());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(object.toString())
                .setCircularRegion(alarm.getLocation().latitude,
                        alarm.getLocation().longitude,
                        alarm.getRange())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(geofence),
                getGeofencePendingIntent(alarm)
        ).setResultCallback(this);
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(Alarm alarm){
        Intent intent = new Intent(context,GeoFenceTransitionsIntentService.class);
        //intent.putExtra("alarm",alarm);

        PendingIntent pendingIntent = PendingIntent.getService(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        //Toast.makeText(context,"Disconnected",Toast.LENGTH_SHORT).show();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

}
