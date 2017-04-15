package com.example.abhisheikh.locationplusalarm.activity;

import android.content.Intent;
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

public class EditAlarmActivity extends AppCompatActivity {

    TextView rangeTextView, ringtoneTextView;
    EditText labelEditText, currentLocationEditText, destinationEditText;
    Spinner rangeSpinner, ringtoneSpinner;
    SwitchCompat vibrationRadioButton;
    Button saveAlarmButton;
    String position;
    Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        Intent intent = getIntent();
        alarm = (Alarm)intent.getParcelableExtra("alarm");
        position = intent.getStringExtra("position");
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
}
