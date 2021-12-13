package com.example.obd_kkb01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Terminal extends AppCompatActivity {

    EditText commend_insert;
    Button commend_click;
    Context context;
    RecyclerView Terminal_data;
    TextView ECU;
    Terminal_Adapter terminal_adapter;
    ArrayList<String> send_data_list = new ArrayList<>();
    ArrayList<Integer> Type_list = new ArrayList<>(); // 0: sender, 1: receiver
    BlueToothSearch_Thead blueToothSearch_thead;
    BluetoothSocket bluetoothSocket;
    DataFrame dataFrame;
    private static final int MESSAGE_Read = 100;
    Terminal_Read_code terminal_read_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        context = this;
        dataFrame = new DataFrame();

        commend_insert = findViewById(R.id.commend_insert);
        commend_click = findViewById(R.id.commend_click);
        Terminal_data = findViewById(R.id.Terminal_data);

        // 터미널 들어오자마자 일단 계산 루틴은 정지
        dataFrame.firststopThread = false;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        Terminal_data.setLayoutManager(linearLayoutManager);

        terminal_adapter = new Terminal_Adapter(context, send_data_list, Type_list);
        Terminal_data.setAdapter(terminal_adapter);

        //핸들러 클래스 객체
        terminal_read_code = new Terminal_Read_code(context);
        terminal_read_code.sendEmptyMessage(MESSAGE_Read);

        commend_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataFrame.Terminal_Write_code = commend_insert.getText().toString();
                dataFrame.stopThread = true;
                send_data_list.add(commend_insert.getText().toString());
                Type_list.add(0);
                terminal_adapter.notifyDataSetChanged();
            }
        });
    }

    class Terminal_Read_code extends Handler {
        Context context;

        public Terminal_Read_code(Context context){
            this.context = context;
        }

        public void handleMessage(Message mgs) {
            //Log.i("Connect", "Run Connect");

            switch (mgs.what){
                case MESSAGE_Read:
                    //Log.i("terminal_handler1", "read");
                    // 차량 속도
                    //Log.i("car data_recieve", "car data_recieve");
                    if (dataFrame.Terminal_Read_code.equals("0")) {

                        //Log.i("terminal_handler2", "read");
                    }else {
                        Log.i("terminal_handler_read", dataFrame.Terminal_Read_code);
                        if (dataFrame.Terminal_Read_code.contains("@@@@@")){
                            String[] ErrorCode = dataFrame.Terminal_Read_code.split("@@@@@");
                            for (int i=0; i<ErrorCode.length; i++){
                                send_data_list.add(ErrorCode[i]);
                                Type_list.add(1);
                            }
                        }else {
                            send_data_list.add(dataFrame.Terminal_Read_code);
                            Type_list.add(1);
                        }
                        //Log.i("terminal_handler3", "read");
                        terminal_adapter.notifyDataSetChanged();
                        dataFrame.Terminal_Read_code ="0";
                    }
                    sendEmptyMessage(MESSAGE_Read);
                    break;
            }

        }
    }

    public void onResume() {
        super.onResume();
        //dataFrame.Terminal_Entrance_boolean = true;
    }

    public void onStop() {
        super.onStop();
        dataFrame.Terminal_code_boolean = false;
    }

    public void onDestroy() {
        super.onDestroy();
        terminal_read_code.removeCallbacksAndMessages(null);
    }

}