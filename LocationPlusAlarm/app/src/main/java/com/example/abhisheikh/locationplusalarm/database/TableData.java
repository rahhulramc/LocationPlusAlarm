package com.example.abhisheikh.locationplusalarm.database;

import android.provider.BaseColumns;

/**
 * Created by abhisheikh on 15/4/17.
 */

public class TableData {

    public TableData(){

    }

    public static abstract class TableInfo implements BaseColumns{
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "lng";
        public static final String LABEL = "label";
        public static final String LOCATION_NAME = "location_name";
        public static final String RANGE = "range";
        public static final String RINGTONE_ID = "ringtone_id";
        public static final String ALARM_STATUS = "alarm_status";
        public static final String VIBRATION = "vibration";
        public static final String DATABASE_NAME = "user_info";
        public static final String TABLE_NAME = "reg_info";
    }

}
