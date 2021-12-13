package com.example.obd_kkb01;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LogActivity_Adapter extends RecyclerView.Adapter<LogActivity_Adapter.ViewHolder> {

    Context context;
    ArrayList<String> Log_list, time_list;
    //todo 0: INFO 1: ERROR
    int Log_type;
    LogDataFrame logDataFrame;

    public LogActivity_Adapter(Context context, ArrayList<String> Log_list, ArrayList<String> time_list, int Log_type){

        this.context = context;
        this.Log_list = Log_list;
        this.time_list = time_list;
        this.Log_type = Log_type;

        logDataFrame = new LogDataFrame();
    }

    @Override
    public LogActivity_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.log_activity_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LogActivity_Adapter.ViewHolder holder, int position) {
        // INFO면 진행
        if (Log_type == 0){
            if (time_list.size()>position){
                holder.log_kind.setText("I / "+time_list.get(position));
                holder.log_content.setText(Log_list.get(position));
                holder.log_kind.setTextColor(Color.parseColor("#ffffff"));
            }
            // ERROR면 진행
        }else if (Log_type == 1){
            if (time_list.size()>position){
                holder.log_kind.setText("E / "+time_list.get(position));
                holder.log_content.setText(Log_list.get(position));
                holder.log_kind.setTextColor(Color.parseColor("#FFFF0000"));
            }
        }
    }

    @Override
    public int getItemCount() {

        return Log_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView log_kind, log_content;

        public ViewHolder(View itemView) {
            super(itemView);

            log_kind = itemView.findViewById(R.id.log_kind);
            log_content = itemView.findViewById(R.id.log_content);
        }
    }
}
