package com.example.abhisheikh.locationplusalarm.activity;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.R;

import java.util.HashMap;
import java.util.Map;

public class RingingActivity extends AppCompatActivity {

    String notificationDetails, ringtoneID;
    boolean vibration;
    Map<String,String> ringtoneList;

    TextView notificationDetailsTextView;
    FloatingActionButton fab;

    MediaPlayer mediaPlayer;
    Vibrator vibrator;

    //Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringing);

        Intent intent = getIntent();
        notificationDetails = intent.getStringExtra("notification");
        ringtoneID = intent.getStringExtra("ringtone_id");
        vibration = intent.getBooleanExtra("vibration",true);
        //alarm = intent.getParcelableExtra("alarm");
        Toast.makeText(this,ringtoneID,Toast.LENGTH_SHORT).show();

        ringtoneList = getRingtoneMap();

        setIDs();
        startRinging();

    }

    private void startRinging(){
        mediaPlayer = new MediaPlayer();
        if(vibration){
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 1000, 200, 200, 200 };
            vibrator.vibrate(pattern, 0);
        }
        Uri alarmTone = Uri.parse(ringtoneList.get(ringtoneID));
        try {
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setDataSource(this,
                    alarmTone);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (Exception e){
            mediaPlayer.release();
        }
    }

    private void setIDs(){
        notificationDetailsTextView = (TextView)findViewById(R.id.reachedTextView);
        notificationDetailsTextView.setText(notificationDetails);
        fab = (FloatingActionButton)findViewById(R.id.fab);
    }

    private Map<String, String> getRingtoneMap() {
        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();

        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

            list.put(notificationTitle, notificationUri);
        }

        return list;
    }
}
