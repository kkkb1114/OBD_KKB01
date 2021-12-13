package com.example.obd_kkb01;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Fragment_INFO extends Fragment {

    RecyclerView log_info_recyclerview;
    LogActivity_Adapter logActivity_adapter;
    Context context;
    LogDataFrame logDataFrame = new LogDataFrame();
    TextView info_title;
    int info_log = 0;
    int log_data_count = 0;
    ArrayList<String> daySelect_INFO_Log = new ArrayList<>();
    ArrayList<String> daySelect_INFO_nowTimelist = new ArrayList<>();
    ArrayList<String> search_INFO_Log = new ArrayList<>();
    ArrayList<String> search_INFO_nowTimelist = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        context = getContext();
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        log_info_recyclerview = view.findViewById(R.id.log_info_recyclerview);
        log_info_recyclerview.setLayoutManager(linearLayoutManager);

        // 로그 화면에서 날짜를 선택하지 않았다면
        if (logDataFrame.Log_day.equals("null")){
            // 로그 검색어가 있다면
            if (!logDataFrame.Log_search.equals("null")){

                //todo 로그 데이터를 전부 검새어가 포함 되있는지 확인하고 포함되있으면 새로 만든 arraylist에 add하고 어뎁터에 넣는다.
                for (int i=0; i<logDataFrame.INFO_Log.size(); i++){
                    if (logDataFrame.INFO_Log.get(i).contains(logDataFrame.Log_search)){
                        search_INFO_Log.add(logDataFrame.INFO_Log.get(i));
                        search_INFO_nowTimelist.add(logDataFrame.INFO_nowTimelist.get(i));
                    }
                }
                logActivity_adapter = new LogActivity_Adapter(context, search_INFO_Log, search_INFO_nowTimelist, info_log);
                log_info_recyclerview.setAdapter(logActivity_adapter);
                //화면 돌입시 로그 데이터 출력
                logActivity_adapter.notifyDataSetChanged();

                info_title = view.findViewById(R.id.info_title);
                if (search_INFO_Log.size() == 0){
                    info_title.setText("총 0개의 로그가 있습니다.");
                }else {
                    info_title.setText("총 "+search_INFO_Log.size()+"개의 로그가 있습니다.");
                }
                // 로그 검색어가 없다면
            }else {

                logActivity_adapter = new LogActivity_Adapter(context, logDataFrame.INFO_Log, logDataFrame.INFO_nowTimelist, info_log);
                log_info_recyclerview.setAdapter(logActivity_adapter);
                //화면 돌입시 로그 데이터 출력
                logActivity_adapter.notifyDataSetChanged();

                info_title = view.findViewById(R.id.info_title);
                if (logDataFrame.INFO_Log.size() == 0){
                    info_title.setText("총 0개의 로그가 있습니다.");
                }else {
                    info_title.setText("총 "+(logDataFrame.INFO_Log.size())+"개의 로그가 있습니다.");
                }
            }
            // 로그 화면에서 날짜를 선택했다면
        }else {

            // 로그 검색어가 있다면
            if (!logDataFrame.Log_search.equals("null")){

            // 로그 시간 데이터를 전부 선택한 날짜가 포함 되있는지 확인하고 포함되있으면 새로 만든 arraylist에 add하고 어뎁터에 넣는다.
                for (int i=0; i<logDataFrame.INFO_nowTimelist.size(); i++){
                    if (logDataFrame.INFO_nowTimelist.get(i).contains(logDataFrame.Log_day)&&
                            logDataFrame.INFO_Log.get(i).contains(logDataFrame.Log_search)){
                        search_INFO_Log.add(logDataFrame.INFO_Log.get(i));
                        search_INFO_nowTimelist.add(logDataFrame.INFO_nowTimelist.get(i));
                    }
                }
                logActivity_adapter = new LogActivity_Adapter(context, search_INFO_Log, search_INFO_nowTimelist, info_log);
                log_info_recyclerview.setAdapter(logActivity_adapter);
                //화면 돌입시 로그 데이터 출력
                logActivity_adapter.notifyDataSetChanged();

                info_title = view.findViewById(R.id.info_title);
                if (logDataFrame.INFO_Log.size() == 0){
                    info_title.setText("총 0개의 로그가 있습니다.");
                }else {
                    info_title.setText("총 "+(search_INFO_Log.size())+"개의 로그가 있습니다.");
                }
            // 로그 검색어가 없다면
            }else {

            // 로그 시간 데이터를 전부 선택한 날짜가 포함 되있는지 확인하고 포함되있으면 새로 만든 arraylist에 add하고 어뎁터에 넣는다.
                for (int i=0; i<logDataFrame.INFO_nowTimelist.size(); i++){
                    if (logDataFrame.INFO_nowTimelist.get(i).contains(logDataFrame.Log_day)){
                        daySelect_INFO_Log.add(logDataFrame.INFO_Log.get(i));
                        daySelect_INFO_nowTimelist.add(logDataFrame.INFO_nowTimelist.get(i));
                    }
                }
                logActivity_adapter = new LogActivity_Adapter(context, daySelect_INFO_Log, daySelect_INFO_nowTimelist, info_log);
                log_info_recyclerview.setAdapter(logActivity_adapter);
                //화면 돌입시 로그 데이터 출력
                logActivity_adapter.notifyDataSetChanged();

                info_title = view.findViewById(R.id.info_title);
                if (logDataFrame.INFO_Log.size() == 0){
                    info_title.setText("총 0개의 로그가 있습니다.");
                }else {
                    info_title.setText("총 "+(daySelect_INFO_nowTimelist.size())+"개의 로그가 있습니다.");
                }
            }
        }
    }
}
