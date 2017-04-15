package com.example.abhisheikh.locationplusalarm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.abhisheikh.locationplusalarm.Alarm;
import com.example.abhisheikh.locationplusalarm.database.TableData.TableInfo;

/**
 * Created by abhisheikh on 15/4/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final int database_version = 1;
    public String CREATE_QUERY = "CREATE TABLE "+TableInfo.TABLE_NAME+"("+TableInfo.LATITUDE+" TEXT,"+
            TableInfo.LONGITUDE+" TEXT,"+TableInfo.LABEL+" TEXT,"+TableInfo.LOCATION_NAME+" TEXT,"+
            TableInfo.RANGE+" TEXT,"+TableInfo.RINGTONE_ID+" TEXT,"+TableInfo.ALARM_STATUS+" TEXT,"+
            TableInfo.VIBRATION+" TEXT);";
    private Context context;

    public DatabaseHandler(Context context) {
        super(context, TableInfo.DATABASE_NAME, null, database_version);
        this.context = context;
        Log.d("DatabaseHandler","Database Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY);
        //Toast.makeText(context,"Database Added",Toast.LENGTH_SHORT).show();
        Log.d("DataBaseHandler","Table Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addInfo(DatabaseHandler dh, Alarm alarm){
        SQLiteDatabase db = dh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TableInfo.LATITUDE,""+alarm.getLocation().latitude);
        cv.put(TableInfo.LONGITUDE,""+alarm.getLocation().longitude);
        cv.put(TableInfo.LABEL,alarm.getLabel());
        cv.put(TableInfo.LOCATION_NAME,alarm.getLocationName());
        cv.put(TableInfo.RANGE,""+alarm.getRange());
        cv.put(TableInfo.RINGTONE_ID,""+alarm.getRingtoneId());
        cv.put(TableInfo.ALARM_STATUS,""+(alarm.isActive()?1:0));
        cv.put(TableInfo.VIBRATION,""+(alarm.isVibrate()?1:0));
        db.insert(TableInfo.TABLE_NAME,null,cv);
        //Toast.makeText(context,"Added to Database",Toast.LENGTH_SHORT).show();
        Log.d("DatabaseHandler","Row inserted");
    }

    public Cursor getInfo(DatabaseHandler dh){
        SQLiteDatabase db = dh.getReadableDatabase();
        String [] columns = {TableInfo.LATITUDE,TableInfo.LONGITUDE,TableInfo.LABEL,
                            TableInfo.LOCATION_NAME,TableInfo.RANGE,TableInfo.RINGTONE_ID,
                            TableInfo.ALARM_STATUS,TableInfo.VIBRATION};
        Cursor cr = db.query(TableInfo.TABLE_NAME,columns,null,null,null,null,null);
        //Toast.makeText(context,"Retrieved",Toast.LENGTH_SHORT).show();
        return cr;
    }

    public void updateDatabase(DatabaseHandler dh, Alarm oldAlarm, Alarm newAlarm){
        SQLiteDatabase db = dh.getWritableDatabase();
        String selection = TableInfo.LATITUDE+" LIKE ? AND "+ TableInfo.LONGITUDE+" LIKE ? AND "+
                TableInfo.LABEL+" LIKE ? AND "+ TableInfo.LOCATION_NAME+" LIKE ? AND "+
                TableInfo.RANGE+" LIKE ? AND "+TableInfo.RINGTONE_ID+" LIKE ? AND "+
                TableInfo.ALARM_STATUS+" LIKE ? AND "+TableInfo.VIBRATION+" LIKE ?";
        String [] args = {""+oldAlarm.getLocation().latitude,""+oldAlarm.getLocation().longitude,
                    oldAlarm.getLabel(),oldAlarm.getLocationName(),""+oldAlarm.getRange(),
                    ""+oldAlarm.getRingtoneId(),""+(oldAlarm.isActive()?1:0),""+(oldAlarm.isVibrate()?1:0)};
        /*Toast.makeText(context,"data "+oldAlarm.isActive(),Toast.LENGTH_SHORT).show();
        Toast.makeText(context,"data "+newAlarm.isActive(),Toast.LENGTH_SHORT).show();*/
        ContentValues cv = new ContentValues();
        cv.put(TableInfo.LATITUDE,""+newAlarm.getLocation().latitude);
        cv.put(TableInfo.LONGITUDE,""+newAlarm.getLocation().longitude);
        cv.put(TableInfo.LABEL,newAlarm.getLabel());
        cv.put(TableInfo.LOCATION_NAME,newAlarm.getLocationName());
        cv.put(TableInfo.RANGE,""+newAlarm.getRange());
        cv.put(TableInfo.RINGTONE_ID,""+newAlarm.getRingtoneId());
        cv.put(TableInfo.ALARM_STATUS,""+(newAlarm.isActive()?1:0));
        cv.put(TableInfo.VIBRATION,""+(newAlarm.isVibrate()?1:0));
        db.update(TableInfo.TABLE_NAME,cv,selection,args);
    }

    public void deleteFromDatabase(DatabaseHandler dh, Alarm alarm){
        SQLiteDatabase db = dh.getWritableDatabase();
        String selection = TableInfo.LATITUDE+" LIKE ? AND "+ TableInfo.LONGITUDE+" LIKE ? AND "+
                TableInfo.LABEL+" LIKE ? AND "+ TableInfo.LOCATION_NAME+" LIKE ? AND "+
                TableInfo.RANGE+" LIKE ? AND "+TableInfo.RINGTONE_ID+" LIKE ? AND "+
                TableInfo.ALARM_STATUS+" LIKE ? AND "+TableInfo.VIBRATION+" LIKE ?";
        String [] args = {""+alarm.getLocation().latitude,""+alarm.getLocation().longitude,
                alarm.getLabel(),alarm.getLocationName(),""+alarm.getRange(),
                ""+alarm.getRingtoneId(),""+(alarm.isActive()?1:0),""+(alarm.isVibrate()?1:0)};
        db.delete(TableInfo.TABLE_NAME,selection,args);
    }

}
