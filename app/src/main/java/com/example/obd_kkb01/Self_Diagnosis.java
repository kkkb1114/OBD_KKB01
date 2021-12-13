package com.example.obd_kkb01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Self_Diagnosis extends AppCompatActivity {

    Button Self_Diagnosis, fault_code_delete;
    Context context;
    DataFrame dataFrame;
    TextView error_code, explanation, google_search;
    RecyclerView Self_Diagnosis_Recycler;
    ArrayList<String> error_datalist;
    Self_Diagnosis_Adater self_diagnosis_adater;
    private static final int Self_Diagnosis_sign = 100;
    Self_Diagnosis_code self_diagnosis_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_diagnosis);

        Self_Diagnosis = findViewById(R.id.Self_Diagnosis);
        fault_code_delete = findViewById(R.id.fault_code_delete);
        error_code = findViewById(R.id.error_code);
        explanation = findViewById(R.id.explanation);
        Self_Diagnosis_Recycler = findViewById(R.id.Self_Diagnosis_Recycler);

        context = this;
        dataFrame = new DataFrame();

        // 터미널 들어오자마자 일단 계산 루틴은 정지
        dataFrame.firststopThread = false;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        Self_Diagnosis_Recycler.setLayoutManager(linearLayoutManager);

        // 자가 진단 버튼
        Self_Diagnosis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataFrame.Terminal_Write_code = "03";
                dataFrame.stopThread = true;
                self_diagnosis_adater.notifyDataSetChanged();
            }
        });

        // 진단 기록 삭제 버튼
        fault_code_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataFrame.Terminal_Write_code = "04";
                dataFrame.stopThread = true;
                self_diagnosis_adater.notifyDataSetChanged();
            }
        });
    }


    class Self_Diagnosis_code extends Handler {
        Context context;

        public Self_Diagnosis_code(Context context) {
            this.context = context;
        }

        public void handleMessage(Message mgs) {


            switch (mgs.what) {
                case Self_Diagnosis_sign:

                    Self_Diagnosis_Recycler.setAdapter(self_diagnosis_adater);
                    sendEmptyMessage(Self_Diagnosis_sign);
                    break;
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        self_diagnosis_code.removeCallbacksAndMessages(null);
    }
}