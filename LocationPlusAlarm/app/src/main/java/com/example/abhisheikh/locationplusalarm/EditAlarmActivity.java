package com.example.abhisheikh.locationplusalarm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class EditAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        Intent intent = getIntent();
        Alarm alarm = (Alarm)intent.getParcelableExtra("alarm");
    }
}
