package com.example.obd_kkb01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {

    Context context;
    LogActivity_Adapter logActivity_adapter;
    ArrayList<String> Log_list = new ArrayList<>();
    LogDataFrame logDataFrame = new LogDataFrame();
    ArrayList<String> Log_day_list = new ArrayList<>();
    private static final int MESSAGE_LOG = 102;
    Log_stamp log_stamp;
    Button log_start_button, log_end_button, info_btn, error_btn;
    private final int Fragment_INFO_num = 1;
    private final int Fragment_ERROR_num = 2;
    ArrayAdapter Log_day_adapter;
    Spinner Log_day_spinner;
    SearchView Log_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        context = this;

        Log_list.add("2021-08-24");
        Log_list.add("2021-08-23");
        Log_list.add("2021-08-22");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                //API에 만들어져 있는 R.layout.simple_spinner...를 씀
                this,android.R.layout.simple_spinner_item, Log_list
        );

        TextView textView4;
        textView4 = findViewById(R.id.textView4);

        Log_day_spinner = findViewById(R.id.Log_day_spinner);
        Log_day_spinner.setAdapter(adapter);
        Log_day_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //todo 나중에 Log_list를 3개 다 logDataFrame.daylist로 바꾸기
                logDataFrame.Log_day = Log_list.get(position);
                textView4.setText(Log_list.get(position));
                Log.i("스피너 선택", Log_list.get(position));
                // 날짜 선택하면 일단 INFO로 바꿈
                Log_Fragment_Change(Fragment_INFO_num);

                error_btn.setTextColor(Color.parseColor("#FFFFFFFF"));
                info_btn.setTextColor(Color.parseColor("#E91E63"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //todo 처음 시작은 기본 프레그먼트 INFO로 세팅
        Log_Fragment_Change(Fragment_INFO_num);
        info_btn = findViewById(R.id.info_btn);
        info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log_Fragment_Change(Fragment_INFO_num);
                error_btn.setTextColor(Color.parseColor("#FFFFFFFF"));
                info_btn.setTextColor(Color.parseColor("#E91E63"));
            }
        });

        error_btn = findViewById(R.id.error_btn);
        error_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log_Fragment_Change(Fragment_ERROR_num);
                error_btn.setTextColor(Color.parseColor("#E91E63"));
                info_btn.setTextColor(Color.parseColor("#FFFFFFFF"));
            }
        });

        Log_search = (SearchView) findViewById(R.id.Log_search);
        Log_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log_Fragment_Change(Fragment_INFO_num);
                error_btn.setTextColor(Color.parseColor("#FFFFFFFF"));
                info_btn.setTextColor(Color.parseColor("#E91E63"));

                logDataFrame.Log_search = query;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    class Log_stamp extends Handler {
        Context context;

        public Log_stamp(Context context){
            this.context = context;
        }

        public void handleMessage(Message mgs) {

            Log.i("로그 핸들러", "시작");
            switch (mgs.what){
                case MESSAGE_LOG:
                    // 로그
                    Log.i("스위치 시작", "MESSAGE_LOG");
                    logActivity_adapter.notifyDataSetChanged();
                    //sendEmptyMessage(MESSAGE_LOG);
                    sendEmptyMessageDelayed(MESSAGE_LOG, 1000);
                    break;
            }
        }
    }

    public void Log_Fragment_Change(int fragment_num){
        //todo FragmentTransaction를 이용해 프래그먼트를 사용한다. (이 클래스에서는 다른 프래그먼트 트랜잭션을 추가, 삭제, 교체 및 실행하는 API를 제공한다.)
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (fragment_num){
            case 1:
            // INFO 프레그먼트 호출
            Fragment_INFO fragment_info = new Fragment_INFO(); //todo 프레그먼트 객체 생성
            transaction.replace(R.id.fragment_container, fragment_info); //todo 프레그먼트로 화면 교체
            transaction.commit();
            break;

            case 2:
            // INFO 프레그먼트 호출
            Fragment_ERROR fragment_error = new Fragment_ERROR(); //todo 프레그먼트 객체 생성
            transaction.replace(R.id.fragment_container, fragment_error); //todo 프레그먼트로 화면 교체
            transaction.commit();
            break;
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        logDataFrame.Log_day = "null";
        logDataFrame.Log_search = "null";
    }
}