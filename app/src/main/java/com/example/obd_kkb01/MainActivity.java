package com.example.obd_kkb01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class
MainActivity extends AppCompatActivity {

    TextView speed, rpm, coolant_temperature, intake_air_temperature, engine_temperature, ECU_connect_Text, EML_connect_Text;
    Button connect_button, Terminal_button, log_activity_button, self_diagnosis;
    Context context;
    Connect Connect;
    private static final int MESSAGE_SPEED = 100;
    // 데이터 갱신시킬 DataFrame 객체 생성
    DataFrame dataFrame = new DataFrame();
    ProgressBar speed_progress_bar,rpm_progress_bar,intake_air_progress_bar,cooling_water_progress_bar,inspiration_temperature_progress_bar;
    public static String saveStorage = ""; //저장된 파일 경로

    // 로그 파일 저장 메소드 변수
    File storageDir;
    String textFileName, nowTime_day;
    BufferedWriter buf;
    String nowTime2;
    //public static ArrayList<String> saveData = new ArrayList<>(); //저장된 파일 내용
    LogDataFrame logDataFrame = new LogDataFrame();

    // 로그 데이터 SQLITE 저장 변수
    LogDatabaseHelper logDatabaseHelper;

    boolean insertData_boolean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        PreferenceManager.clear(context);

        // 앱 꺼질때 사용할 오늘 날짜 구하기
        long now_day = System.currentTimeMillis(); //TODO 현재시간 받아오기
        Date date_day = new Date(now_day); //TODO Date 객체 생성
        SimpleDateFormat sdf_day = new SimpleDateFormat("yyyy-MM-dd");
        nowTime_day = sdf_day.format(date_day);
        
        // 로그 데이터 저장용 LogDatabaseHelper 객체 생성
        logDatabaseHelper = new LogDatabaseHelper(context);

        // 바로 데이터 불러와서 로그 데이터 전역 변수에 저장
        Cursor res = logDatabaseHelper.getAllData();
        if (res.getCount() == 0){
            Toast.makeText(MainActivity.this,"데이터 불러오기 실패",Toast.LENGTH_LONG).show();
        }else {
            while (res.moveToNext()) {
                logDataFrame.first_Log_id.add(res.getInt(0));
                logDataFrame.first_daylist.add(res.getString(1));
                logDataFrame.first_INFO_Log.add(res.getString(2));
                logDataFrame.first_ERROR_Log.add(res.getString(3));
                logDataFrame.first_INFO_nowTimelist.add(res.getString(4));
                logDataFrame.first_ERROR_nowTimelist.add(res.getString(5));
            }

            // 쉐어드에 로그 데이터가 있으면 모두 불러와서 로그 전역 변수 어레이에 넣는다.
            for (int i2=0; i2<logDataFrame.first_daylist.size(); i2++){
            if (logDataFrame.first_Log_id.get(0) != null) {

                //String[] Log_id_array = logDataFrame.first_Log_id.get(i2).split("@@@"); // 정수로 값이 나와서 안되는것 같다. 문자열로 바꾸자
                String[] daylist_array = logDataFrame.first_daylist.get(i2).split("@@@");
                String[] INFO_Log_array = logDataFrame.first_INFO_Log.get(i2).split("@@@");
                String[] ERROR_Log_array = logDataFrame.first_ERROR_Log.get(i2).split("@@@");
                String[] INFO_nowTimelist_array = logDataFrame.first_INFO_nowTimelist.get(i2).split("@@@");
                String[] ERROR_nowTimelist_array = logDataFrame.first_ERROR_nowTimelist.get(i2).split("@@@");

                // db에 있는 데이터 전부 넣어준다.
                for (int i = 0; i < daylist_array.length; i++) {
                    if (!daylist_array[i].equals(" ")) {
                        logDataFrame.daylist.add(daylist_array[i]);
                    }
                }

                for (int i = 0; i < INFO_Log_array.length; i++) {
                    if (!INFO_Log_array[i].equals(" ")) {
                        logDataFrame.INFO_Log.add(INFO_Log_array[i]);
                    }
                }

                for (int i = 0; i < INFO_nowTimelist_array.length; i++) {
                    if (!INFO_nowTimelist_array[i].equals(" ")) {
                        logDataFrame.INFO_nowTimelist.add(INFO_nowTimelist_array[i]);
                    }
                }

                for (int i = 0; i < ERROR_Log_array.length; i++) {
                    if (!ERROR_Log_array[i].equals(" ")) {
                        logDataFrame.ERROR_Log.add(ERROR_Log_array[i]);
                    }
                }

                for (int i = 0; i < ERROR_nowTimelist_array.length; i++) {
                    if (!ERROR_nowTimelist_array[i].equals(" ")) {
                        logDataFrame.ERROR_nowTimelist.add(ERROR_nowTimelist_array[i]);
                    }
                }
            }
        }
        }

        // 소켓, 스트림 연결끊는 전역 변수 어플 최초 접속 메인화면 왔을때는 소켓, 스트림 연결이 끊어지지 않게 한다.
        dataFrame.ShutDownApp = false;

        log_activity_button = findViewById(R.id.log_activity_button);
        // ECU_connect_Text = findViewById(R.id.ECU_connect_Text_boolean);
        EML_connect_Text = findViewById(R.id.EML_connect_Text_boolean);
        intake_air_temperature = findViewById(R.id.intake_air);
        engine_temperature = findViewById(R.id.inspiration_temperature);
        coolant_temperature = findViewById(R.id.cooling_water);
        speed = findViewById(R.id.speed);
        rpm = findViewById(R.id.rpm);
        speed_progress_bar = findViewById(R.id.speed_progress_bar);
        rpm_progress_bar = findViewById(R.id.rpm_progress_bar);
        intake_air_progress_bar = findViewById(R.id.intake_air_progress_bar);
        cooling_water_progress_bar = findViewById(R.id.cooling_water_progress_bar);
        inspiration_temperature_progress_bar = findViewById(R.id.inspiration_temperature_progress_bar);

        log_activity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                startActivity(intent);
               /* if (dataFrame.Scaner_connect){
                    Intent intent = new Intent(MainActivity.this, LogActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(context, "먼저 스캐너를 연결해 주세요.", Toast.LENGTH_LONG).show();
                }*/
            }
        });

        connect_button = findViewById(R.id.connect_button);
        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BlueToothSearch.class);
                startActivity(intent);
            }
        });

        Terminal_button = findViewById(R.id.Terminal_button);
        Terminal_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dataFrame.Scaner_connect){
                    Intent intent = new Intent(MainActivity.this, Terminal.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(context, "먼저 스캐너를 연결해 주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //onCreate에서 핸들러 객체 미리 생성 (onResume에서 블루투스 연결 여부 확인 및 컨트롤)
        Connect = new Connect(context);
        PreferenceManager.setBoolean(context, "BlueTooth_Connect", false);

        self_diagnosis = findViewById(R.id.self_diagnosis);
        self_diagnosis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataFrame.Scaner_connect){
                    Intent intent = new Intent(MainActivity.this, Self_Diagnosis.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(context, "먼저 스캐너를 연결해 주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void onResume() {
        super.onResume();
        //Log.i("BlueTooth_Connect", String.valueOf(PreferenceManager.getBoolean(context,"BlueTooth_Connect")));
        if (PreferenceManager.getBoolean(context,"BlueTooth_Connect")) {

            Connect.sendEmptyMessage(MESSAGE_SPEED);
        }
        dataFrame.Terminal_code_boolean = false;
        dataFrame.stopThread = false;
        //계산 루틴으로 전환
        dataFrame.firststopThread = true;
    }

    class Connect extends Handler {
        Context context;

        public Connect(Context context){
            this.context = context;
        }

        public void handleMessage(Message mgs) {
            //Log.i("Connect", "Run Connect");

            switch (mgs.what){
                case MESSAGE_SPEED:
                    // 차량 속도
                    //Log.i("car data_recieve", "car data_recieve");
                    if (dataFrame.Speed != null&&dataFrame.Rpm != null&&dataFrame.Engine_temperature != null&&dataFrame.Intake_air_temperature != null
                            &&dataFrame.Coolant_temperature != null) {

                        /*Log.i("handleMessage", "setText");
                        Log.i("dataFrame.Speed", dataFrame.Speed);
                        Log.i("dataFrame.Rpm", dataFrame.Rpm);
                        Log.i("dataFrame.Coolant_temperature", dataFrame.Coolant_temperature);
                        Log.i("dataFrame.Engine_temperature", dataFrame.Engine_temperature);
                        Log.i("dataFrame.Intake_air_temperature", dataFrame.Intake_air_temperature);*/
                        float Engine_temperature = Float.parseFloat(dataFrame.Engine_temperature);
                        float Intake_air_temperature = Float.parseFloat(dataFrame.Intake_air_temperature);
                        //Log.i("dataFrame.Rpm", dataFrame.Rpm);
                        dataFrame.Rpm_int = Integer.parseInt(dataFrame.Rpm);
                            speed.setText(dataFrame.Speed);
                            rpm.setText(dataFrame.Rpm);
                            coolant_temperature.setText(dataFrame.Coolant_temperature);
                            engine_temperature.setText(dataFrame.Engine_temperature);
                            intake_air_temperature.setText(dataFrame.Intake_air_temperature);
                            speed_progress_bar.setProgress(Integer.parseInt(dataFrame.Speed));
                            //Log.i("dataFrame.Rpm", String.valueOf(dataFrame.Rpm_int));
                            rpm_progress_bar.setProgress(dataFrame.Rpm_int);
                            intake_air_progress_bar.setProgress((int) Intake_air_temperature);
                            cooling_water_progress_bar.setProgress(Integer.parseInt(dataFrame.Coolant_temperature));
                            inspiration_temperature_progress_bar.setProgress((int) Engine_temperature);

                      /*      // 로그 데이터 파일 저장
                        write(logDataFrame.savefile_Log_list);*/
                    }
                    sendEmptyMessage(MESSAGE_SPEED);
                    break;
            }

        }
    }

    protected void onDestroy() {
        super.onDestroy();
        dataFrame.Terminal_code_boolean = false;

        // 소켓, 스트림 연결끊는 전역 변수
        dataFrame.ShutDownApp = true;

        // 앱이 꺼지면 소켓 연결 상태도 비연결로 바꾼다.
        dataFrame.Scaner_connect = false;

        // 처음 데이터 저장할때는 insert로 한다.
        if (logDataFrame.first_daylist.get(0) == null){
            // 앱이 꺼질때 sqlite에 로그 데이터 저장
            boolean insertData_boolean = logDatabaseHelper.insertData(String.join("@@@", logDataFrame.daylist),
                    String.join("@@@", logDataFrame.INFO_Log),
                    String.join("@@@", logDataFrame.ERROR_Log),
                    String.join("@@@", logDataFrame.INFO_nowTimelist),
                    String.join("@@@", logDataFrame.ERROR_nowTimelist));

            if (insertData_boolean == true){
                Toast.makeText(MainActivity.this,"데이터추가 성공",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(MainActivity.this,"데이터추가 실패",Toast.LENGTH_LONG).show();
            }
        }else {

        // 오늘 날짜와 같은 데이터는 업데이트 해주고 지난 데이터들은 냅둔다.
        for (int i=0; i<logDataFrame.first_daylist.size(); i++){
            if (nowTime_day.equals(logDataFrame.first_daylist.get(logDataFrame.first_daylist.size()-1))){
                // 앱 데이터를 저장한 이력이 있으면 updateData로 하려 했으나 자꾸 데이터가 더해져서 한번 데이터를 지우고 insert 하는 과정을 해본다.

                //todo 이미 있는 데이터는 update
                insertData_boolean = logDatabaseHelper.updateData(String.join("@@@", logDataFrame.daylist),
                        String.join("@@@", logDataFrame.INFO_Log),
                        String.join("@@@", logDataFrame.ERROR_Log),
                        String.join("@@@", logDataFrame.INFO_nowTimelist),
                        String.join("@@@", logDataFrame.ERROR_nowTimelist));
            }
        }
            if (insertData_boolean == true){
                Toast.makeText(MainActivity.this,"데이터 업데이트 성공",Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(MainActivity.this,"데이터 업데이트 실패",Toast.LENGTH_LONG).show();
            }
        }

        logDataFrame.reset();
    }


    public void setSaveText(){
        try {
            buf.close();

            saveStorage = String.valueOf(storageDir+textFileName); //TODO 경로 저장 /storage 시작
            //saveStorage = String.valueOf(storageDir.toURI()+textFileName); //TODO 경로 저장 file:/ 시작
            PreferenceManager.setString(getApplication(), "saveStorage", String.valueOf(saveStorage)); //TODO 프리퍼런스에 경로 저장한다
            PreferenceManager.setString(getApplication(), "saveStorage", String.valueOf(saveStorage)); //TODO 프리퍼런스에 경로 저장한다

            Log.d("---","---");
            Log.w("//===========//","================================================");
            Log.d("","\n"+"[A_TextFile > 저장한 텍스트 파일 확인 실시]");
            Log.d("","\n"+"[경로 : "+String.valueOf(saveStorage)+"]");
            Log.d("","\n"+"[제목 : "+String.valueOf(nowTime2)+"]");
            Log.d("","\n"+"[내용 : "+String.join("------------------------------------\n", logDataFrame.savefile_Log_list));
            Log.w("//===========//","================================================");
            Log.d("---","---");

            Toast.makeText(getApplication(),"텍스트 파일이 저장되었습니다",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void write(ArrayList<String> datalist){
        try {
            //saveData = datalist; //TODO 변수에 값 대입

            long now2 = System.currentTimeMillis(); //TODO 현재시간 받아오기
            Date date2 = new Date(now2); //TODO Date 객체 생성
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            nowTime2 = sdf2.format(date2);

            textFileName = "/"+nowTime2+".txt";
            //TODO 파일 생성
            storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/OBD2_LOG"); //TODO 저장 경로
            //TODO 폴더 생성
            if(!storageDir.exists()){ //TODO 폴더 없을 경우
                storageDir.mkdir(); //TODO 폴더 생성
            }

            long now = System.currentTimeMillis(); //TODO 현재시간 받아오기
            Date date = new Date(now); //TODO Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            String nowTime = sdf.format(date);

            buf = new BufferedWriter(new FileWriter(storageDir+textFileName, true)); //TODO 다중으로 내용적음 (TRUE)
            //buf = new BufferedWriter(new FileWriter(storageDir+textFileName, false)); //TODO 한개 내용만 표시됨 (FALSE)
            buf.append(String.join("------------------------------------\n", datalist)); //TODO 날짜 쓰기
            buf.newLine(); //TODO 개행
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO ==== 텍스트 호출 메소드 ====
    public void getSaveText(){
        try {
            saveStorage = PreferenceManager.getString(getApplication(), "saveStorage"); //특정 데이터 가져온다
            if(saveStorage != null && saveStorage.length() > 0){

                String data = "";
                String line = ""; //TODO 한줄씩 읽기

                File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SaveStorage"); //TODO 저장 경로
                //TODO 폴더 생성
                if(!storageDir.exists()){ //TODO 폴더 없을 경우
                    storageDir.mkdir(); //TODO 폴더 생성
                }
                try {
                    BufferedReader buf = new BufferedReader(new FileReader(saveStorage));
                    while((line=buf.readLine())!=null){
                        data += line;
                        data += "\n";
                    }
                    buf.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("---","---");
                Log.w("//===========//","================================================");
                Log.d("","\n"+"[A_TextFile > 저장된 텍스트 파일 확인 실시]");
                Log.d("","\n"+"[경로 : "+String.valueOf(saveStorage)+"]");
                Log.d("","\n"+"[내용 : "+String.valueOf(data)+"]");
                Log.w("//===========//","================================================");
                Log.d("---","---");
            }
            else {
                Log.d("---","---");
                Log.e("//===========//","================================================");
                Log.d("","\n"+"[A_TextFile > 저장된 텍스트 파일 확인 실시]");
                Log.d("","\n"+"[경로 : "+""+"]");
                Log.e("//===========//","================================================");
                Log.d("---","---");
                Toast.makeText(getApplication(),"저장된 텍스트가 없습니다. 텍스트를 저장해주세요",Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){
            Toast.makeText(getApplication(),"저장된 텍스트가 없습니다. 텍스트를 저장해주세요",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}