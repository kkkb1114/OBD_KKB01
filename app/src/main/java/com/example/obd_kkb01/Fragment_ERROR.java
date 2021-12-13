package com.example.obd_kkb01;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Fragment_ERROR extends Fragment {

    RecyclerView log_error_recyclerview;
    LogActivity_Adapter logActivity_adapter;
    Context context;
    LogDataFrame logDataFrame = new LogDataFrame();
    TextView error_title;
    int error_log = 1;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        context = getContext();
        return inflater.inflate(R.layout.fragment_error, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        log_error_recyclerview = view.findViewById(R.id.log_error_recyclerview);
        log_error_recyclerview.setLayoutManager(linearLayoutManager);

        logActivity_adapter = new LogActivity_Adapter(context, logDataFrame.ERROR_Log, logDataFrame.ERROR_nowTimelist, error_log);
        log_error_recyclerview.setAdapter(logActivity_adapter);
        //화면 돌입시 로그 데이터 출력
        logActivity_adapter.notifyDataSetChanged();

        error_title = view.findViewById(R.id.error_title);
        if (logDataFrame.ERROR_Log.size() == 0){
            error_title.setText("총 0개의 로그가 있습니다.");
        }else {
            error_title.setText("총 "+(logDataFrame.ERROR_Log.size()-1)+"개의 로그가 있습니다.");
        }
    }

}
