package com.example.abhisheikh.locationplusalarm.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EditAlarmActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    TextView rangeTextView, ringtoneTextView;
    EditText labelEditText, currentLocationEditText, destinationEditText;
    Spinner rangeSpinner, ringtoneSpinner;
    SwitchCompat vibrationRadioButton;
    Button saveAlarmButton;
    String position,currentLocationName;
    Alarm alarm;
    Location myLocation;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        Intent intent = getIntent();
        alarm = (Alarm)intent.getParcelableExtra("alarm");
        position = intent.getStringExtra("position");

        if(mGoogleApiClient==null) {
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

    private void setIds(){
        rangeTextView = (TextView)findViewById(R.id.rangeTextView);
        labelEditText = (EditText)findViewById(R.id.labelEditText);
        currentLocationEditText = (EditText)findViewById(R.id.current_location_textview);
        destinationEditText = (EditText)findViewById(R.id.destinationEditText);
        ringtoneTextView = (TextView)findViewById(R.id.ringtoneTextView);
        rangeSpinner= (Spinner)findViewById(R.id.range_value);
        ringtoneSpinner = (Spinner)findViewById(R.id.spinner_ringtone);
        vibrationRadioButton = (SwitchCompat) findViewById(R.id.vibrationRadioButton);
        saveAlarmButton = (Button)findViewById(R.id.saveAlarmButton);
    }

    private void setInitialValuesOfAlarm(Alarm alarm){
        labelEditText.setText(alarm.getLabel());
        rangeTextView.setText(Integer.toString(alarm.getRange()));
        destinationEditText.setText(alarm.getLocationName());
        ringtoneTextView.setText(Integer.toString(alarm.getRingtoneId()));
        vibrationRadioButton.setChecked(alarm.isVibrate());
    }

    private void setOnClickListeners(){
        saveAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setLabel(labelEditText.getText().toString());
                alarm.setLocationName(destinationEditText.getText().toString());
                alarm.setRange(500);
                alarm.setRingtoneId(0);
                alarm.setVibrate(vibrationRadioButton.isChecked());
                Intent intent = new Intent();
                intent.putExtra("alarm",alarm);
                intent.putExtra("position",position);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        currentLocationName = getCurrentLocationName(myLocation);
        currentLocationEditText.setText(currentLocationName);

    }

    private String getCurrentLocationName(Location location){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses.get(0).getFeatureName() + ", " +
                addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " +
                addresses.get(0).getCountryName();
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
}
