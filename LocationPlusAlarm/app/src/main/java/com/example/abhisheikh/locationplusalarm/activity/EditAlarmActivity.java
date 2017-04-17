package com.example.abhisheikh.locationplusalarm.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.R;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditAlarmActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    EditText labelEditText, currentLocationEditText, destinationEditText;
    Spinner rangeSpinner, ringtoneSpinner;
    SwitchCompat vibrationRadioButton;
    Button saveAlarmButton;
    String position, currentLocationName;
    Alarm alarm;
    Location myLocation;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        Intent intent = getIntent();
        alarm = (Alarm) intent.getParcelableExtra("alarm");
        position = intent.getStringExtra("position");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.edit_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        //Toast.makeText(this,position,Toast.LENGTH_SHORT).show();
        setIds();
        setInitialValuesOfAlarm(alarm);
        setOnClickListeners();
    }

    private void setIds() {
        labelEditText = (EditText) findViewById(R.id.labelEditText);
        currentLocationEditText = (EditText) findViewById(R.id.current_location_textview);
        currentLocationEditText.setEnabled(false);
        destinationEditText = (EditText) findViewById(R.id.destinationEditText);
        destinationEditText.setEnabled(false);
        rangeSpinner = (Spinner) findViewById(R.id.range_value);
        ringtoneSpinner = (Spinner) findViewById(R.id.spinner_ringtone);
        setSpinner();
        vibrationRadioButton = (SwitchCompat) findViewById(R.id.vibrationRadioButton);
        saveAlarmButton = (Button) findViewById(R.id.saveAlarmButton);
    }

    private void setSpinner(){
        List<String> ringtoneList =  new ArrayList<>();
        Map<String, String> ringtoneMap = getRingtoneMap();

        for(Map.Entry<String,String> entry : ringtoneMap.entrySet()){
            ringtoneList.add(entry.getKey());
        }

        //Toast.makeText(this,ringtoneMap.get("Oxygen"),Toast.LENGTH_SHORT).show();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, ringtoneList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringtoneSpinner.setAdapter(adapter);
    }

    public Map<String, String> getRingtoneMap() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();

        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = manager.getRingtoneUri(cursor.getPosition()).toString();

            list.put(notificationTitle, notificationUri);
        }

        return list;
    }

    private void setInitialValuesOfAlarm(Alarm alarm) {
        labelEditText.setText(alarm.getLabel());
        destinationEditText.setText(alarm.getLocationName());
        //Toast.makeText(getBaseContext(),""+alarm.getRange(),Toast.LENGTH_SHORT).show();
        String [] rangeList = getResources().getStringArray(R.array.range);
        int rangeIndex = 4;
        for(int i = 0;i<rangeList.length;i++){
            if(rangeList[i].equals(""+alarm.getRange()))
                rangeIndex = i;
        }
        rangeSpinner.setSelection(rangeIndex);
        vibrationRadioButton.setChecked(alarm.isVibrate());
    }

    private void setOnClickListeners() {

        saveAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rangeSpinner.getSelectedItemPosition() != 0) {
                    Alarm oldAlarm = new Alarm(alarm);
                    alarm.setLabel(labelEditText.getText().toString());
                    alarm.setLocationName(destinationEditText.getText().toString());
                    alarm.setRange(Integer.parseInt(rangeSpinner.getSelectedItem().toString()));
                    alarm.setRingtoneId(ringtoneSpinner.getSelectedItem().toString());
                    alarm.setVibrate(vibrationRadioButton.isChecked());

                    if (position == null) {
                        alarm.setRange(Integer.parseInt(rangeSpinner.getSelectedItem().toString()));
                        if (alarm.isActive()) {
                            removeGeofence(oldAlarm);
                        }
                        addGeofence(alarm);
                    }

                    Intent intent = new Intent();
                    intent.putExtra("alarm", alarm);
                    intent.putExtra("position", position);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else Toast.makeText(getBaseContext(),R.string.invalid_range,Toast.LENGTH_SHORT).show();
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
        /*if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            Toast.makeText(this,"Google API Client is not connected",Toast.LENGTH_SHORT).show();
            return;
        }*/

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Intent intent = new Intent(this,GeoFenceTransitionsIntentService.class);
        //intent.putExtra("alarm",alarm);

        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        currentLocationName = getCurrentLocationName(myLocation);
        currentLocationEditText.setText(currentLocationName);

    }

    private String getCurrentLocationName(Location location){
        if(location!=null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses.get(0).getFeatureName() + ", " +
                    addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " +
                    addresses.get(0).getCountryName();
        }
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Announcement button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
