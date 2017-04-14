package com.example.abhisheikh.locationplusalarm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by abhisheikh on 14/4/17.
 */

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    List<Alarm> alarmList;
    Context context;

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
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).
                inflate(R.layout.alarm_list_row,parent,false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AlarmViewHolder holder, final int position) {

        Alarm alarm = alarmList.get(position);
        holder.nameTextView.setText(alarm.getLocationName());
        holder.labelTextView.setText(alarm.getLabel());
        holder.alarmActivateSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.alarmActivateSwitch.isChecked()){
                    Toast.makeText(context,"Alarm "+position+" Deactivated",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(context,"Alarm "+position+" Activated",Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.deleteAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmList.remove(position);
                Toast.makeText(context,"Alarm "+position+" Deleted",Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }
}
