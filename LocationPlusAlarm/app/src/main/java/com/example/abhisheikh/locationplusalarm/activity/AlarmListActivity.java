package com.example.abhisheikh.locationplusalarm.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.adapter.AlarmAdapter;
import com.example.abhisheikh.locationplusalarm.R;
import com.example.abhisheikh.locationplusalarm.database.DatabaseHandler;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class AlarmListActivity extends AppCompatActivity {

    private RecyclerView alarmRecyclerView;
    private List<Alarm> alarmList = new ArrayList<>();
    private AlarmAdapter alarmAdapter;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        checkGPS();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton floatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddAlarm.class);
                startActivityForResult(intent,1);
            }
        });

        alarmRecyclerView = (RecyclerView)findViewById(R.id.alarmRecyclerView);

        alarmAdapter = new AlarmAdapter(context, alarmList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        alarmRecyclerView.setLayoutManager(layoutManager);
        alarmRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(alarmRecyclerView.getContext(),
                layoutManager.getOrientation());
        alarmRecyclerView.addItemDecoration(dividerItemDecoration);
        alarmRecyclerView.setAdapter(alarmAdapter);

        //prepareAlarmData();
        getFromDatabase();

    }

    private void checkGPS(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.gps_disabled_message)
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void getFromDatabase(){
        alarmList = new ArrayList<>();
        DatabaseHandler dh = new DatabaseHandler(context);
        Cursor cr = dh.getInfo(dh);
        if(cr.moveToFirst()){
            //Toast.makeText(this,"getFromDatabase",Toast.LENGTH_SHORT).show();
            do{
                Double lat = Double.parseDouble(cr.getString(0));
                Double lng = Double.parseDouble(cr.getString(1));
                LatLng ltlng = new LatLng(lat,lng);
                String label = cr.getString(2);
                String locationName = cr.getString(3);
                int range = Integer.parseInt(cr.getString(4));
                String ringtoneID = (cr.getString(5));
                boolean active = (Integer.parseInt(cr.getString(6))!=0);
                boolean vibration = (Integer.parseInt(cr.getString(7))!=0);

                Alarm alarm = new Alarm(ltlng,locationName,label,range,ringtoneID,active,vibration);
                alarmList.add(alarm);
            }while (cr.moveToNext());
        }
        for(int i = 0;i<alarmList.size();i++){
            System.out.println(alarmList.get(i).getLocationName());
        }
        alarmAdapter = new AlarmAdapter(this,alarmList);
        alarmRecyclerView.setAdapter(alarmAdapter);
    }

    private void prepareAlarmData(){
        /*alarmList.add(new Alarm(new LatLng(29.869419, 77.894877),
                "Ravi","Get well soon",30, 0, true, true));
        alarmList.add(new Alarm(new LatLng(29.870026, 77.895118),
                "Bharat","Get well soon",30, 0, false, false));
        alarmList.add(new Alarm(new LatLng(29.8699363,77.8946516),
                "Purushottam","Get well soon",30, 0, false, false));

        alarmAdapter.notifyDataSetChanged();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                Alarm alarm = data.getParcelableExtra("alarm");
                String position = data.getStringExtra("position");
                if(position==null){
                    alarm.setActive(true);
                    alarmList.add(alarm);
                    addToDatabase(alarm);
                    alarmAdapter.notifyDataSetChanged();
                }
                else{
                    int pos = Integer.parseInt(position);
                    Alarm oldAlarm = alarmList.get(pos);
                    alarmList.set(pos,alarm);
                    updateDatabase(oldAlarm,alarm);
                    alarmAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void addToDatabase(Alarm alarm){
        DatabaseHandler dh = new DatabaseHandler(context);
        dh.addInfo(dh,alarm);
    }

    private void updateDatabase(Alarm oldAlarm, Alarm newAlarm){
        DatabaseHandler dh = new DatabaseHandler(context);
        dh.updateDatabase(dh, oldAlarm, newAlarm);
    }

}
