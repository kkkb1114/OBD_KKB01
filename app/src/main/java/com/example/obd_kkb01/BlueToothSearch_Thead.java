package com.example.obd_kkb01;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.icu.lang.UCharacter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.PortUnreachableException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BlueToothSearch_Thead extends Thread{

    DataFrame dataFrame = new DataFrame();
    LogDataFrame logDataFrame = new LogDataFrame();
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private InputStream inputStream2 = null;
    private OutputStream outputStream2 = null;
    Context context;
    boolean cancel = true;
    //boolean stopThread = false;
    StringBuilder read_data_Builder = new StringBuilder();
    StringBuilder read_data_Builder_Terminal = new StringBuilder();
    Terminal terminal;
    String[] read_data, read_data_7E, read_data_rpm;
    String speed = "010D";
    String rpm = "010C";
    String coolant_temperature = "0105";
    String engine_temperature = "0110";
    String intake_air_temperature = "010F";
    String next_data, nowTime, nowTime_day;
    String[] STOPPED;
    // 에러코드 계산 클래스 객체
    PID_calculate pid_calculate;
    BlueToothSearch blueToothSearcH;

    // AT코드 순서
    public final String[] DefaultATCommandArray = new String[]{"ATZ","ATE0","ATD0","ATSP0","ATH1","ATM0","ATS0","ATAT1","ATST64"};

    public BlueToothSearch_Thead(Context context, BluetoothSocket bluetoothSocket){
        this.bluetoothSocket = bluetoothSocket;
        this.context = context;

        pid_calculate = new PID_calculate();
        terminal = new Terminal();
    }

    public void run(){

        byte temp;
        int buffersize = 1024;
        final byte[] buffer = new byte[buffersize];
        try {
            // 데이터 송수신을 위한 스트림 연결
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream2 = bluetoothSocket.getInputStream();
            outputStream2 = bluetoothSocket.getOutputStream();
            //연결하는 동시에 데이터 갱신시킬 DataFrame 객체 생성
            dataFrame.Scaner_connect = true;

            // 소켓 연결 끊는 동작을 했으니 새로운 소켓 연결 후엔 다시 변수 리셋
            dataFrame.Diffirent_Socket = false;

            //연결 현황 쉐어드 (OBD2) ECU는 좀더 검토 해야함
            PreferenceManager.setBoolean(context, "BlueTooth_Connect_OBD2", true);
            Log.i("BlueTooth_Connect_ECU_boolean_set", "true");
            //Log.i("Stream_connect", "Stream_connect");

            for (int i=0; i<DefaultATCommandArray.length; i++){

                //Log.i("DefaultATCommandArray i: ", String.valueOf(i));
                //Log.i("DefaultATCommandArray.length", String.valueOf(DefaultATCommandArray.length));
                //Log.i("for_DefaultATCommandArray", DefaultATCommandArray[i]);
                write(DefaultATCommandArray[i]+"\r");

                if (inputStream != null) {
                    try {
                        int read = inputStream.read(buffer, 0, buffersize);
                        temp = (byte) read;

                        //Log.i("int_read", String.valueOf(read));
                        //Log.i("temp_read", String.valueOf(temp));

                        String strBuffer = new String(buffer, 0, temp, StandardCharsets.UTF_8);
                        //Log.i("readString_strBuffer", strBuffer);
                        if (strBuffer.equals(">")){
                            read_data_Builder.append(strBuffer);
                            String read_data_Builder_result = read_data_Builder.toString();
                            //Log.i("read_data_Builder_result", read_data_Builder_result);
                            terminal.send_data_list.add(read_data_Builder_result);
                            terminal.Type_list.add(1);
                            terminal.terminal_adapter.notifyDataSetChanged();
                            read_data_Builder.delete(0,read_data_Builder.length());
                        }else {
                            if (strBuffer.contains(">")){
                                String read_data_Builder_result = read_data_Builder.toString();
                                //Log.i("read_data_Builder_result", read_data_Builder_result);
                                read_data_Builder.delete(0,read_data_Builder.length());
                            }else {

                                //Log.i("readString", strBuffer);
                                read_data_Builder.append(strBuffer);
                            }
                        }
                    } catch (Exception e) {
                        //Log.i("int_read", "fail");
                        e.printStackTrace();
                    }
                    //Log.i("int_read_while", "end");
                }

                Log.i("AT set", "AT set");
                if (i >=DefaultATCommandArray.length-1) {
                    Log.i("read if", "read if");
                    read();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void write(String send){
        try {
            if (outputStream != null){
                Log.i("write_data", send);
                //Log.i("write_data_getBytes", String.valueOf(send.getBytes(StandardCharsets.UTF_8)));
                // 문자열 바이트로 변환하여 전송
                outputStream.write(send.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(){

        Log.i("read_run", "read_run");
        int self_Diagnosis=0;
        byte temp;
        final int buffersize = 1024;
        final byte[] buffer = new byte[buffersize];
        byte temp2;
        final int buffersize2 = 1024;
        final byte[] buffer2 = new byte[buffersize2];
        String data_calculate = null;
        String strBuffer2 = null;
        next_data = speed;
        if (inputStream != null) {
            cancel = true;
            //Log.i("inputStream !", "inputStream !");
            while (cancel) {
                if (dataFrame.ShutDownApp||dataFrame.Diffirent_Socket) {
                    // 소켓, 스트림 전부 연결 끊는 메소드
                    cancel();
                }else {
                //Log.i("inputStream !", "inputStream !");
                try {
                    //Log.i("inputStream ! try", "inputStream ! try");

                    // 터미널 화면에서 전송한 데이터가 있으면 동작
                    if (dataFrame.stopThread) {
                        //Log.i("inputStream ! stopThread", "inputStream ! stopThread");
                        int read = 0;
                        //Log.i("terminal_data.Terminal_Write_code", dataFrame.Terminal_Write_code);
                        write(dataFrame.Terminal_Write_code + "\r");
                        read = inputStream.read(buffer2, 0, buffersize2);
                        if (read == 0) {
                            //Log.i("terminal_read_data_null", "null");
                        } else {
                            //Log.i("terminal_read_data_notnull", "notnull");
                        }
                        //sleep(1000);
                        temp2 = (byte) read;
                        String strBuffer = new String(buffer2, 0, temp2, StandardCharsets.UTF_8);
                        Log.i("readString_strBuffer_Terminal", strBuffer);
                        if (strBuffer.equals(">")) {
                            read_data_Builder_Terminal.append(strBuffer);
                            String read_data_Builder_result = read_data_Builder_Terminal.toString();
                            Log.i("read_data_Builder_result_Terminal", read_data_Builder_result);
                            terminal.send_data_list.add(read_data_Builder_result);
                            terminal.Type_list.add(1);
                            terminal.terminal_adapter.notifyDataSetChanged();

                            read_data_Builder_Terminal.delete(0, read_data_Builder_Terminal.length());
                            if (read_data_Builder_result.contains("41" + dataFrame.Terminal_Write_code.split("01")[1]) ||
                                    read_data_Builder_result.contains("01" + dataFrame.Terminal_Write_code.split("01")[1])) {

                                //Log.i("dataFrame.Terminal_Read_code_read_data_Builder_result", read_data_Builder_result);
                                // 결과값을 나눠서 7E8이후의 데이터는 모두 자르고 7E8데이터만 받도록 한다.
                                if (read_data_Builder_result.contains("7E8")||read_data_Builder_result.contains("7EA")||read_data_Builder_result.contains("7E9")) {

                                    dataFrame.Terminal_Read_code = read_data_Builder_result;
                                    dataFrame.Terminal_code_boolean = false;
                                    dataFrame.stopThread = false;
                                    //Log.i("split_read_data_Builder_result", read_data_Builder_result);
                                }
                            } else if (read_data_Builder_result.contains("STOPPED")){

                                Log.i("터미널 화면", "no data");
                                dataFrame.Terminal_Read_code = "NO DATA";
                                dataFrame.Terminal_code_boolean = false;
                                dataFrame.stopThread = false;
                            } else {
                                write(dataFrame.Terminal_Write_code + "\r");
                            }
                        } else {
                            Log.i("strBuffer", strBuffer);
                            if (strBuffer.contains(">")) {
                                if (strBuffer.contains("STOPPED")) {
                                    STOPPED = strBuffer.split("STOPPED");
                                    Log.i("STOPPED_strBuffer", strBuffer);
                                    if (strBuffer.contains("No_DATA")) {
                                        STOPPED = strBuffer.split("No DATA");
                                        Log.i("No DATA_strBuffer", strBuffer);
                                    }
                                    read_data_Builder_Terminal.append(STOPPED[0]);
                                }

                                String read_data_Builder_result = read_data_Builder_Terminal.toString();
                                Log.i("read_data_Builder_result_Terminal", read_data_Builder_result);
                                read_data_Builder_Terminal.delete(0, read_data_Builder_Terminal.length());

                                //Log.i("???", dataFrame.Terminal_Write_code.split("01")[1]);

                                if (dataFrame.Terminal_Write_code.contains("03")||dataFrame.Terminal_Write_code.contains("0A")||dataFrame.Terminal_Write_code.contains("07")) {

                                    // read_data_Builder_result에 \r이 붙어있어 계속 한칸씩 더 먹고 있다.
                                    read_data_Builder_result.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "");
                                    read_data_Builder_result.replaceAll(System.getProperty("line.separator"), "");
                                    String if_data_result = read_data_Builder_result.substring(0, 5);
                                    if_data_result = if_data_result.split("\r")[0]+if_data_result.split("\r")[1];
                                    if (if_data_result.contains("\r")) {
                                        Log.i("if_data_result에 특수문자가 끈질기게 붙어있다.", "더럽게 안사라지네");
                                        Log.i("asd", if_data_result.split("\r")[0]);
                                    }
                                    Log.i("read_data_Builder_result.substring(0, 5)", if_data_result);

                                    // 앞단이 037E,077E,0A7E 아니면 패스
                                    if (if_data_result.equals("037E")||if_data_result.equals("077E")
                                            ||if_data_result.equals("0A7E")) {

                                        //todo 전송한 코드가 에러 코드 요청 코드면 실행 (받은 데이터에 STOPPED가 있으면 스플릿한다.
                                    if (read_data_Builder_result.contains("STOPPED")) {

                                        Log.i("잘못된 데이터", "자름");
                                        read_data_Builder_result = read_data_Builder_result.split("STOPPED")[0];
                                        Log.i("털었거나 안털었거나", "털었거나 안털었거나");

                                        //TODO 잠시 테스트를 위해 임의의 값을 계산 클래스 생성자에 삽입.
                                        ArrayList<String> pid_calculate_list = pid_calculate.PID2(read_data_Builder_result);
                                        /* ArrayList<String> pid_calculate_list = pid_calculate.PID2("037E8100E4306010002007E821030043008200C17E822000000000000007E904430101017EA0443010101" +
                                                "7EA21030043008200C17EA2200000000000000"); */
                                        Log.i("pid_calculate_list 길이", String.valueOf(pid_calculate_list.size()));
                                        
                                        if (pid_calculate_list.size() <= 1) {

                                            Log.i("pid_calculate_list 길이가 짧다.", String.valueOf(pid_calculate_list.size()));
                                            Log.i("pid_calculate_list(1) 내용", pid_calculate_list.get(1));
                                            //dataFrame.Terminal_Read_code = pid_calculate_list.toString();
                                            dataFrame.Terminal_code_boolean = false;
                                            dataFrame.stopThread = false;

                                        } else {

                                            Log.i("pid_calculate_list 길이가 길다.", String.valueOf(pid_calculate_list.size()));
                                            // @@@@@를 기준으로 join하고 문자열을 인식했을때 if문으로 해당 문자열에 @@@@@이 포함되어 있다면 에러코드로 간주하고 split하여 for문으로
                                            // 리사이클러뷰에 삽입하고 갱신시켜준다.
                                            dataFrame.Terminal_Read_code = String.join("@@@@@", pid_calculate_list);
                                            Log.i("pid_calculate_list 길이가 길다. dataFrame.Terminal_Read_code 조인 하면?", dataFrame.Terminal_Read_code);
                                            dataFrame.Terminal_code_boolean = false;
                                            dataFrame.stopThread = false;

                                        }

                                    } else {

                                        Log.i("털었거나 안털었거나", "털었거나 안털었거나");
                                        ArrayList<String> pid_calculate_list = pid_calculate.PID2(read_data_Builder_result);
                                        /* ArrayList<String> pid_calculate_list = pid_calculate.PID2("037E8100E4306010002007E821030043008200C17E822000000000000007E904430101017EA0443010101" +
                                                "7EA21030043008200C17EA2200000000000000"); */
                                        Log.i("pid_calculate_list 길이", String.valueOf(pid_calculate_list.size()));

                                        if (pid_calculate_list.size() <= 1) {

                                            Log.i("pid_calculate_list 길이가 짧다.", String.valueOf(pid_calculate_list.size()));
                                            Log.i("pid_calculate_list(1) 내용", pid_calculate_list.get(1));
                                            //dataFrame.Terminal_Read_code = pid_calculate_list.toString();
                                            dataFrame.Terminal_code_boolean = false;
                                            dataFrame.stopThread = false;

                                        } else {

                                            Log.i("pid_calculate_list 길이가 길다.", String.valueOf(pid_calculate_list.size()));
                                            // @@@@@를 기준으로 join하고 문자열을 인식했을때 if문으로 해당 문자열에 @@@@@이 포함되어 있다면 에러코드로 간주하고 split하여 for문으로
                                            // 리사이클러뷰에 삽입하고 갱신시켜준다.
                                            dataFrame.Terminal_Read_code = String.join("@@@@@", pid_calculate_list);
                                            Log.i("pid_calculate_list 길이가 길다. dataFrame.Terminal_Read_code 조인 하면?", dataFrame.Terminal_Read_code);
                                            dataFrame.Terminal_code_boolean = false;
                                            dataFrame.stopThread = false;

                                        }
                                    }
                                }else {
                                        Log.i("앞단이 03이 아님", "패스");
                                    }
                                   //todo 전송한 코드가 에러 코드 요청 코드면 실행 여기까지

                                }else {

                                if (read_data_Builder_result.contains("41" + dataFrame.Terminal_Write_code.split("01")[1]) ||
                                        read_data_Builder_result.contains("01" + dataFrame.Terminal_Write_code.split("01")[1])) {

                                    //Log.i("dataFrame.Terminal_Read_code_read_data_Builder_result", read_data_Builder_result);
                                    // 결과값을 나눠서 7E8이후의 데이터는 모두 자르고 7E8데이터만 받도록 한다.
                                    if (read_data_Builder_result.contains("7E8") || read_data_Builder_result.contains("7EA") || read_data_Builder_result.contains("7E9")) {

                                        dataFrame.Terminal_Read_code = read_data_Builder_result;
                                        dataFrame.Terminal_code_boolean = false;
                                        dataFrame.stopThread = false;
                                        //Log.i("split_read_data_Builder_result", read_data_Builder_result);
                                    }
                                    //반환값이 내가 전송한 코드와 동일하면 NO DATA를 표시하고 다음 데이터 코드를 입력 할수 있도록 넘겨준다.
                                } else if (read_data_Builder_result.contains(dataFrame.Terminal_Write_code) || read_data_Builder_result.contains("STOPPED")) {

                                    Log.i("터미널 화면", "no data");
                                    dataFrame.Terminal_Read_code = "NO_DATA";
                                    dataFrame.Terminal_code_boolean = false;
                                    dataFrame.stopThread = false;
                                } else {
                                    write(dataFrame.Terminal_Write_code + "\r");
                                }
                            }
                            } else {

                                //Log.i("readString", strBuffer);
                                read_data_Builder_Terminal.append(strBuffer);
                                strBuffer = null;

                                String read_data_Builder_result = read_data_Builder_Terminal.toString();
                                //Log.i("read_data_Builder_result_Terminal", read_data_Builder_result);
                            }
                        }

                        // 실시간 데이터 계산 표기
                    } else {
                        if (dataFrame.firststopThread) {
                            //Log.i("inputStream ! else", "inputStream ! else");

                            //Log.i("next_data_start1", next_data);
                            //Log.i("read_while_try", "read_while_try");

                            // 로그 기록에 남길 시간 계산
                            long now = System.currentTimeMillis(); //TODO 현재시간 받아오기
                            Date date = new Date(now); //TODO Date 객체 생성
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                            nowTime = sdf.format(date);
                            //nowTime = "2021-08-23 22:22:22";
                            //nowTime = "2021-08-22 22:22:22";

                            // 로그 스피너에 넣을 해쉬맵 데이터 시간을 뺀다.
                            long now_day = System.currentTimeMillis(); //TODO 현재시간 받아오기
                            Date date_day = new Date(now_day); //TODO Date 객체 생성
                            SimpleDateFormat sdf_day = new SimpleDateFormat("yyyy-MM-dd");
                            nowTime_day = sdf_day.format(date_day);

                            PreferenceManager.setBoolean(context, "first_answer", true);
                            int read = inputStream.read(buffer, 0, buffersize);
                            temp = (byte) read;
                            PreferenceManager.setBoolean(context, "first_answer", false);

                            //Log.i("int_read", String.valueOf(read));
                            //Log.i("temp_read", String.valueOf(temp));

                            String strBuffer = new String(buffer, 0, temp, StandardCharsets.UTF_8);

                            //Log.i("readString_strBuffer", strBuffer);
                            if (strBuffer.equals(">")) {
                                read_data_Builder.append(strBuffer);
                                String read_data_Builder_result = read_data_Builder.toString();
                                Log.i("read_data_Builder_result", read_data_Builder_result);
                                terminal.send_data_list.add(read_data_Builder_result);
                                terminal.Type_list.add(1);
                                terminal.terminal_adapter.notifyDataSetChanged();

                                //데이터 계산
                                data_calculate = data_calculate(read_data_Builder_result);
                                Log.i("데이터 계산", data_calculate);
                                Log.i("next_data", next_data);
                                if (dataFrame.Terminal_code_boolean) {

                                    dataFrame.stopThread = true;
                                } else {

                                    write(next_data + "\r");
                                    read_data_Builder.delete(0, read_data_Builder.length());
                                }
                            } else {
                                if (strBuffer.contains(">")) {
                                    String read_data_Builder_result = read_data_Builder.toString();
                                    Log.i("read_data_Builder_result", read_data_Builder_result);

                                    //데이터 계산
                                    data_calculate = data_calculate(read_data_Builder_result);
                                    Log.i("next_data", next_data);
                                    if (dataFrame.Terminal_code_boolean) {

                                        dataFrame.stopThread = true;
                                    } else {

                                        write(next_data + "\r");
                                        read_data_Builder.delete(0, read_data_Builder.length());
                                    }
                                } else {

                                    //Log.i("readString", strBuffer);
                                    read_data_Builder.append(strBuffer);
                                    strBuffer = null;

                                    Log.i("PIDCommandArray[i]else", next_data);
                                    String read_data_Builder_result = read_data_Builder.toString();
                                    Log.i("read_data_Builder_result", read_data_Builder_result);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    //Log.i("int_read", "fail");
                    e.printStackTrace();
                }
            }
            }
            //Log.i("int_read_while", "end");
        }

    }

    public void cancel(){
        try {
            // 역순으로 연결을 제거한다.
            cancel = false;
            inputStream.close();
            outputStream.close();
            bluetoothSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
    // TODO rpm처럼 반반씩 나눠서 계산하는 데이터는 뒤에 0이 6개가 붙고 나머지 하나로 계산하는 데이터는 8개가 나온다. (아직까지 확인된 바로는...)
    public String data_calculate(String PID){

        String data_calculate = null;
        String read_data1 = null;
        String read_data2 = null;

        //차량 속도값
        if (PID.contains("010D")||PID.contains("410D")) {
            // 로그 화면 기록 / 계산 전 데이터
            logDataFrame.INFO_Log.add("I/Speed data: "+PID);
            // 로그 파일 저장 / 계산 전 데이터
            logDataFrame.savefile_Log_list.add("[I/Speed data: "+PID+"]\n");
            
            read_data = PID.split("410D");
            if (read_data[read_data.length - 1].contains("7E")) {

                read_data_7E = read_data[read_data.length - 1].split("7E");
                if (read_data_7E[0].equals("")) {
                    //Log.i("read_data_7E[0]", "null");
                }else {
                    //read_data1 = read_data_7E[0];
                    read_data1 = read_data_7E[0].replace("\r", "");
                    int data_int;
                    //Log.i("데이터 계산_split_7E", read_data1);

                    try {
                        // TODO 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
                        // TODO rpm처럼 반반씩 나눠서 계산하는 데이터는 뒤에 0이 6개가 붙고 나머지 하나로 계산하는 데이터는 8개가 나온다. (아직까지 확인된 바로는...)
                        if(read_data1.length() >= 6){
                            // 처음에는 0, 1로 'FE'문자열을 잘라봤는데 자르고 보니 계속 F만 나오는 것이 문제였다.
                            // 결국 0, 2로 바꿔서 해결했는데 내 예상으로는 문자가 숫자보다 더 많은 크기를 가지고 있어서 그런 것 같다. (이거 분명 자바 기초때 배운건데 다시 한번 봐야겠다.)
                            String split_string = read_data1.substring(0, 2);
                            Log.i("split_string", split_string);
                            data_int = Integer.parseInt(split_string, 16);
                         }else {
                        data_int = Integer.parseInt(read_data1, 16);
                        }
                        Log.i("data_int_7E_speed", String.valueOf(data_int));
                        data_calculate = String.valueOf(data_int);

                        // 로그 화면 기록 / 계산 후 데이터
                        logDataFrame.INFO_Log.add("I/Speed data calculation result: "+data_calculate);
                        // INFO 시간 저장
                        logDataFrame.INFO_nowTimelist.add(nowTime);


                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        // 로그 파일 저장 / 계산 후 데이터
                        logDataFrame.savefile_Log_list.add("[I/Speed data calculation result: "+data_calculate+"]\n");
                        //차량 속도값 전역변수 저장
                        dataFrame.Speed = data_calculate;
                        next_data = rpm;

                    } catch (Exception e) {
                        // 로그 화면 기록 / 스피드 계산에서 에러
                        logDataFrame.ERROR_Log.add("E/Speed data calculation result Error!!: "+data_calculate);
                        // ERROR 시간 저장
                        logDataFrame.ERROR_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        // 로그 파일 저장 / 스피드 계산에서 에러
                        logDataFrame.savefile_Log_list.add("[E/Speed data calculation result Error!!: "+data_calculate+"]\n");
                        
                        //차량 속도값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                        dataFrame.Speed = data_calculate;
                        next_data = rpm;
                        //Log.i("data_int_error", read_data1);
                        e.printStackTrace();
                    }
                }
            } else {

                read_data1 = read_data[read_data.length - 1].replace("\r", "");
                int data_int;
                //Log.i("데이터 계산_split", read_data1);

                try {
                    Log.i("read_data1", read_data1);
                    Log.i("read_data1 길이", String.valueOf(read_data1.length()));
                    // TODO 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
                    // TODO rpm처럼 반반씩 나눠서 계산하는 데이터는 뒤에 0이 6개가 붙고 나머지 하나로 계산하는 데이터는 8개가 나온다. (아직까지 확인된 바로는...)
                    if(read_data1.length() >= 6){
                        // 처음에는 0, 1로 'FE'문자열을 잘라봤는데 자르고 보니 계속 F만 나오는 것이 문제였다.
                        // 결국 0, 2로 바꿔서 해결했는데 내 예상으로는 문자가 숫자보다 더 많은 크기를 가지고 있어서 그런 것 같다. (이거 분명 자바 기초때 배운건데 다시 한번 봐야겠다.)
                        String split_string = read_data1.substring(0, 2);
                        Log.i("split_string", split_string);
                        data_int = Integer.parseInt(split_string, 16);
                    }else {
                        data_int = Integer.parseInt(read_data1, 16);
                    }
                    //data_int = Integer.parseInt(read_data1, 16);
                    Log.i("data_int_speed", String.valueOf(data_int));
                    data_calculate = String.valueOf(data_int);

                    // 로그 화면 기록 / 계산 후 데이터
                    logDataFrame.INFO_Log.add("I/Speed data calculation result: "+data_calculate);
                    // INFO 시간 저장
                    logDataFrame.INFO_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    // 로그 파일 저장 / 계산 후 데이터
                    logDataFrame.savefile_Log_list.add("[I/Speed data calculation result: "+data_calculate+"]\n");
                    //차량 속도값 전역변수 저장
                    dataFrame.Speed = data_calculate;
                    next_data = rpm;

                } catch (Exception e) {
                    // 로그 화면 기록 / 스피드 계산에서 에러
                    logDataFrame.ERROR_Log.add("E/Speed data calculation result Error!!: "+data_calculate);
                    // ERROR 시간 저장
                    logDataFrame.ERROR_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    // 로그 파일 저장 / 스피드 계산에서 에러
                    logDataFrame.savefile_Log_list.add("[E/Speed data calculation result Error!!: "+data_calculate+"]\n");
                    //차량 속도값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                    dataFrame.Speed = data_calculate;
                    next_data = rpm;
                    //Log.i("data_int_error", read_data1);
                    e.printStackTrace();
                }
            }
            //차량 rpm값
        } else if (PID.contains("010C")||PID.contains("410C")) {
            // 로그 화면 기록 / 계산 전 데이터
            logDataFrame.INFO_Log.add("I/Rpm data: "+PID);
            read_data = PID.split("410C");
            if (read_data[read_data.length - 1].contains("7E")) {

                read_data_7E = read_data[read_data.length - 1].split("7E");
                if (read_data_7E[0].equals("")) {
                    //Log.i("read_data_7E[0]", "null");
                }else {
                    read_data1 = read_data_7E[0].replace("\r", "");
                    //Log.i("데이터 계산_split_7E", read_data1);

                    try {
                        read_data_rpm = read_data1.split("");
                        String read_data_rpm_A1 = read_data_rpm[0]+read_data_rpm[1];
                        String read_data_rpm_B2 = read_data_rpm[2]+read_data_rpm[3];
                        int read_data_rpm_A;
                        int read_data_rpm_B;
                        // 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
                        if(read_data_rpm_A1.length() >= 6){
                            read_data_rpm_A = Integer.parseInt(read_data_rpm_A1.substring(read_data_rpm_A1.length()-6, read_data_rpm_A1.length()), 16);
                            read_data_rpm_B = Integer.parseInt(read_data_rpm_B2.substring(read_data_rpm_B2.length()-6, read_data_rpm_B2.length()), 16);
                        }else {
                            read_data_rpm_A = Integer.parseInt(read_data_rpm[0]+read_data_rpm[1], 16);
                            read_data_rpm_B = Integer.parseInt(read_data_rpm[2]+read_data_rpm[3], 16);
                        }

                        //Log.i("read_data_rpm_A,B", String.valueOf(read_data_rpm_A+","+read_data_rpm_B));

                        int rpm = ((read_data_rpm_A*256)+read_data_rpm_B)/4;
                        Log.i("rpm 결과값", String.valueOf(rpm));

                        // 로그 화면 기록 / 계산 후 데이터
                        logDataFrame.INFO_Log.add("I/Rpm data calculation result: "+String.valueOf(rpm));
                        // INFO 시간 저장
                        logDataFrame.INFO_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 rpm값 전역변수 저장
                        dataFrame.Rpm = String.valueOf(rpm);
                        dataFrame.Rpm_int = Integer.parseInt(dataFrame.Rpm);
                        next_data = engine_temperature;

                    } catch (Exception e) {
                        // 로그 화면 기록 / Rpm 계산에서 에러
                        logDataFrame.ERROR_Log.add("E/Rpm data calculation result Error!!: "+String.valueOf(rpm));
                        // ERROR 시간 저장
                        logDataFrame.ERROR_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 rpm값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                        dataFrame.Rpm = String.valueOf(rpm);
                        dataFrame.Rpm_int = Integer.parseInt(dataFrame.Rpm);
                        next_data = engine_temperature;
                        //Log.i("data_int_error", read_data1);
                        e.printStackTrace();
                    }
                }
            } else {

                read_data1 = read_data[read_data.length - 1].replace("\r", "");
                //Log.i("데이터 계산_split", read_data1);

                try {
                    read_data_rpm = read_data1.split("");
                   String read_data_rpm_A1 = read_data_rpm[0]+read_data_rpm[1];
                    String read_data_rpm_B2 = read_data_rpm[2]+read_data_rpm[3];
                     int read_data_rpm_A;
                        int read_data_rpm_B;
                        // 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
                        if(read_data_rpm_A1.length() >= 6){
                            read_data_rpm_A = Integer.parseInt(read_data_rpm_A1.substring(read_data_rpm_A1.length()-6, read_data_rpm_A1.length()), 16);
                            read_data_rpm_B = Integer.parseInt(read_data_rpm_B2.substring(read_data_rpm_B2.length()-6, read_data_rpm_B2.length()), 16);
                        }else {
                            read_data_rpm_A = Integer.parseInt(read_data_rpm_A1, 16);
                            read_data_rpm_B = Integer.parseInt(read_data_rpm_B2, 16);
                        }
                    //Log.i("read_data_rpm_A,B", String.valueOf(read_data_rpm_A+","+read_data_rpm_B));

                    int rpm = ((read_data_rpm_A*256)+read_data_rpm_B)/4;
                    Log.i("rpm 결과값", String.valueOf(rpm));

                    // 로그 화면 기록 / 계산 후 데이터
                    logDataFrame.INFO_Log.add("I/Rpm data calculation result: "+String.valueOf(rpm));
                    // INFO 시간 저장
                    logDataFrame.INFO_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 rpm값 전역변수 저장
                    dataFrame.Rpm = String.valueOf(rpm);
                    dataFrame.Rpm_int = Integer.parseInt(dataFrame.Rpm);
                    next_data = engine_temperature;

                } catch (Exception e) {
                    // 로그 화면 기록 / Rpm 계산에서 에러
                    logDataFrame.ERROR_Log.add("E/Rpm data calculation result Error!!: "+String.valueOf(rpm));
                    // ERROR 시간 저장
                    logDataFrame.ERROR_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 rpm값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                    dataFrame.Rpm = String.valueOf(rpm);
                    dataFrame.Rpm_int = Integer.parseInt(dataFrame.Rpm);
                    next_data = engine_temperature;
                    //Log.i("data_int_error", read_data1);
                    e.printStackTrace();
                }
            }
            //차량 공기 유량값
        } else if (PID.contains("0110")||PID.contains("4110")) {
            // 로그 화면 기록 / 계산 전 데이터
            logDataFrame.INFO_Log.add("I/Engine_temperature data: "+PID);
            read_data = PID.split("4110");
            //Log.i("데이터 계산_split", read_data[read_data.length-1]);

            if (read_data[read_data.length - 1].contains("7E")) {

                read_data_7E = read_data[read_data.length - 1].split("7E");
                if (read_data_7E[0].equals("")) {
                    //Log.i("read_data_7E[0]", "null");
                }else {
                    read_data1 = read_data_7E[0].replace("\r", "");
                    //Log.i("데이터 계산_split_7E", read_data1);
                    String[] read_data_array = read_data1.split("");

                    try {
                    String read_data_rpm_A1 = read_data_rpm[0]+read_data_rpm[1];
                    String read_data_rpm_B2 = read_data_rpm[2]+read_data_rpm[3];
                    int read_data_rpm_A;
                    int read_data_rpm_B;
                    // 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
                    if(read_data_rpm_A1.length() >= 6 && read_data_rpm_B2.length() >= 6 ){
                    read_data_rpm_A = Integer.parseInt(read_data_rpm_A1.substring(read_data_rpm_A1.length()-6, read_data_rpm_A1.length()), 16);
                    read_data_rpm_B = Integer.parseInt(read_data_rpm_B2.substring(read_data_rpm_B2.length()-6, read_data_rpm_B2.length()), 16);
                    }else {
                     read_data_rpm_A = Integer.parseInt(read_data_rpm_A1, 16);
                     read_data_rpm_B = Integer.parseInt(read_data_rpm_B2, 16);
                    }

                        int engine_temperature = (int) ((read_data_rpm_A*256)+read_data_rpm_B)/100;
                        Log.i("data_int_7E_temperature", String.valueOf(engine_temperature));
                        data_calculate = String.valueOf(engine_temperature);

                        // 로그 화면 기록 / 계산 후 데이터
                        logDataFrame.INFO_Log.add("I/Engine_temperature data calculation result: "+data_calculate);
                        // INFO 시간 저장
                        logDataFrame.INFO_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 엔진 온도값 전역변수 저장
                        dataFrame.Engine_temperature = data_calculate;
                        next_data = intake_air_temperature;

                    } catch (Exception e) {
                        // 로그 화면 기록 / Engine_temperature 계산에서 에러
                        logDataFrame.ERROR_Log.add("E/Engine_temperature data calculation result Error!!: "+data_calculate);
                        // ERROR 시간 저장
                        logDataFrame.ERROR_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 엔진 온도값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                        dataFrame.Engine_temperature = data_calculate;
                        next_data = intake_air_temperature;
                        //Log.i("data_int_error", read_data1);
                        e.printStackTrace();
                    }
                }
            } else {

                read_data1 = read_data[read_data.length - 1].replace("\r", "");
                //Log.i("데이터 계산_split", read_data1);
                String[] read_data_array = read_data1.split("");

                try {
                    String read_data_rpm_A1 = read_data_array[0]+read_data_array[1];
                    String read_data_rpm_B2 = read_data_array[2]+read_data_array[3];
                    int read_data_rpm_A;
                    int read_data_rpm_B;
                    if(read_data_rpm_A1.length() >= 6 && read_data_rpm_B2.length() >= 6 ){

                    read_data_rpm_A = Integer.parseInt(read_data_rpm_A1.substring(read_data_rpm_A1.length()-6, read_data_rpm_A1.length()), 16);
                    read_data_rpm_B = Integer.parseInt(read_data_rpm_B2.substring(read_data_rpm_B2.length()-6, read_data_rpm_B2.length()), 16);
                    }else {
                    read_data_rpm_A = Integer.parseInt(read_data_rpm_A1, 16);
                    read_data_rpm_B = Integer.parseInt(read_data_rpm_B2, 16);
                    }
                    int engine_temperature = (int) ((read_data_rpm_A*256)+read_data_rpm_B)/100;
                    Log.i("data_int_7E_engine_temperature", String.valueOf(engine_temperature));
                    data_calculate = String.valueOf(engine_temperature);

                    // 로그 화면 기록 / 계산 후 데이터
                    logDataFrame.INFO_Log.add("I/Engine_temperature data calculation result: "+data_calculate);
                    // INFO 시간 저장
                    logDataFrame.INFO_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 엔진 온도값 전역변수 저장
                    dataFrame.Engine_temperature = data_calculate;
                    next_data = intake_air_temperature;

                } catch (Exception e) {
                    // 로그 화면 기록 / Engine_temperature 계산에서 에러
                    logDataFrame.ERROR_Log.add("E/Engine_temperature data calculation result Error!!: "+data_calculate);
                    // ERROR 시간 저장
                    logDataFrame.ERROR_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 엔진 온도값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                    dataFrame.Engine_temperature = data_calculate;
                    next_data = intake_air_temperature;
                    //Log.i("data_int_error", read_data1);
                    e.printStackTrace();
                }
            }

            //차량 산소1 센서값
        /*} else if (PID.contains("0114")||PID.contains("4114")) {
            read_data = PID.split("4114");*/
        } else if (PID.contains("010F")||PID.contains("410F")) {
            // 로그 화면 기록 / 계산 전 데이터
            logDataFrame.INFO_Log.add("I/intake_air_temperature data: "+PID);
            read_data = PID.split("410F");
            //Log.i("데이터 계산_split", read_data[read_data.length-1]);

            if (read_data[read_data.length - 1].contains("7E")) {

                read_data_7E = read_data[read_data.length - 1].split("7E");
                if (read_data_7E[0].equals("")) {
                    //Log.i("read_data_7E[0]", "null");
                }else {
                    read_data1 = read_data_7E[0].replace("\r", "");

                    try {
                        /*String read_data_A = read_data_array[0]+read_data_array[1];
                        String read_data_B = read_data_array[2]+read_data_array[3];
                        float data_int = Integer.parseInt(read_data_A, 16);
                        float intake_air_temperature = data_int-40;*/

                        // if문 안에서 선언하면 당연하게 밖에서 인식이 안되니 밖에서 선언하고 계산
                        float intake_air_temperature;

                        if(read_data1.length() >= 6) {
                            String split_string = read_data1.substring(0, 2);
                            Log.i("split_string_coolant_temperature", split_string);
                            float data_int = Integer.parseInt(split_string, 16);
                            intake_air_temperature = data_int-40;
                        }else {
                            float data_int = Integer.parseInt(read_data1, 16);
                            intake_air_temperature = data_int-40;
                        }
                        Log.i("data_int_7E_intake_air_temperature", String.valueOf(intake_air_temperature));
                        data_calculate = String.valueOf(intake_air_temperature);

                        // 로그 화면 기록 / 계산 후 데이터
                        logDataFrame.INFO_Log.add("I/intake_air_temperature data calculation result: "+data_calculate);
                        // INFO 시간 저장
                        logDataFrame.INFO_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 산소1 센서값 전역변수 저장1
                        dataFrame.Intake_air_temperature = data_calculate;
                        next_data = coolant_temperature;

                    } catch (Exception e) {
                        // 로그 화면 기록 / intake_air_temperature 계산에서 에러
                        logDataFrame.ERROR_Log.add("E/intake_air_temperature data calculation result Error!!: "+data_calculate);
                        // ERROR 시간 저장
                        logDataFrame.ERROR_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 산소1 센서값 전역변수 저장1 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                        dataFrame.Intake_air_temperature = data_calculate;
                        next_data = coolant_temperature;
                        //Log.i("data_int_error", read_data1);
                        e.printStackTrace();
                    }
                }
            } else {

                read_data1 = read_data[read_data.length - 1].replace("\r", "");
                float data_int;
                //Log.i("데이터 계산_split", read_data1);
                //String[] read_data_array = read_data1.split("");

                try {
                    /*String read_data_A = read_data_array[0]+read_data_array[1];
                    String read_data_B = read_data_array[2]+read_data_array[3];
                    float data_int = Integer.parseInt(read_data_A, 16);
                    float intake_air_temperature = data_int-40;*/
                    // if문 안에서 선언하면 당연하게 밖에서 인식이 안되니 밖에서 선언하고 계산
                    float intake_air_temperature;
                    float data_int2;

                    if(read_data1.length() >= 6) {
                        String split_string = read_data1.substring(0, 2);
                        Log.i("split_string_coolant_temperature", split_string);
                        data_int2 = Integer.parseInt(split_string, 16);
                        intake_air_temperature = data_int2-40;
                    }else {
                        data_int2 = Integer.parseInt(read_data1, 16);
                        intake_air_temperature = data_int2-40;
                    }
                    intake_air_temperature = data_int2-40;
                    Log.i("data_int", String.valueOf(intake_air_temperature));
                    Log.i("data_int_intake_air_temperature", String.valueOf(intake_air_temperature));
                    data_calculate = String.valueOf(intake_air_temperature);

                    // 로그 화면 기록 / 계산 후 데이터
                    logDataFrame.INFO_Log.add("I/intake_air_temperature data calculation result: "+data_calculate);
                    // INFO 시간 저장
                    logDataFrame.INFO_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 산소1 센서값 전역변수 저장1
                    dataFrame.Intake_air_temperature = data_calculate;
                    next_data = coolant_temperature;

                } catch (Exception e) {
                    // 로그 화면 기록 / intake_air_temperature 계산에서 에러
                    logDataFrame.ERROR_Log.add("E/intake_air_temperature data calculation result Error!!: "+data_calculate);
                    // ERROR 시간 저장
                    logDataFrame.ERROR_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 산소1 센서값 전역변수 저장1 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                    dataFrame.Intake_air_temperature = data_calculate;
                    next_data = coolant_temperature;
                    //Log.i("data_int_error", read_data1);
                    e.printStackTrace();
                }
            }
            //차량 냉각수 온도값
        } else if (PID.contains("0105")||PID.contains("4105")) {
            // 로그 화면 기록 / 계산 전 데이터
            logDataFrame.INFO_Log.add("I/Coolant_temperature data: "+PID);
            read_data = PID.split("4105");
            //Log.i("데이터 계산_split", read_data[read_data.length-1]);

            if (read_data[read_data.length - 1].contains("7E")) {

                read_data_7E = read_data[read_data.length - 1].split("7E");
                if (read_data_7E[0].equals("")) {
                    //Log.i("read_data_7E[0]", "null");
                }else {
                    read_data1 = read_data_7E[0].replace("\r", "");
                    int data_int;
                    //Log.i("데이터 계산_split_7E", read_data1);

                    try {
                        // TODO 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
                        // TODO rpm처럼 반반씩 나눠서 계산하는 데이터는 뒤에 0이 6개가 붙고 나머지 하나로 계산하는 데이터는 8개가 나온다. (아직까지 확인된 바로는...)
                        if(read_data1.length() >= 6){
                            String split_string = read_data1.substring(0, 2);
                            Log.i("split_string_coolant_temperature", split_string);
                            data_int = Integer.parseInt(split_string, 16);
                            data_int = data_int-40;
                        }else {
                            Log.i("split_string_coolant_temperature", read_data1);
                            data_int = Integer.parseInt(read_data1, 16);
                        }
                        Log.i("data_int_7E_coolant_temperature", String.valueOf(data_int));
                        data_calculate = String.valueOf(data_int);

                        // 로그 화면 기록 / 계산 후 데이터
                        logDataFrame.INFO_Log.add("I/Coolant_temperature data calculation result: "+data_calculate);
                        // INFO 시간 저장
                        logDataFrame.INFO_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 냉각수 온도값 전역변수 저장
                        dataFrame.Coolant_temperature = data_calculate;
                        next_data = speed;

                    } catch (Exception e) {
                        // 로그 화면 기록 / Coolant_temperature 계산에서 에러
                        logDataFrame.ERROR_Log.add("E/Coolant_temperature data calculation result Error!!: "+data_calculate);
                        // ERROR 시간 저장
                        logDataFrame.ERROR_nowTimelist.add(nowTime);
                        // nowTime_day사이즈가 0이면 바로 add한다.
                        if(logDataFrame.daylist.size() == 0){
                            logDataFrame.daylist.add(nowTime_day);
                        }else {
                            for (int i = 0; i < logDataFrame.daylist.size(); i++){
                                if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                    // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                    if(logDataFrame.daylist.size() >= 5){
                                        logDataFrame.daylist.remove(0);
                                    }else {
                                        // 로그 날짜 스피너에 넣을 어레이
                                        logDataFrame.daylist.add(nowTime_day);
                                    }
                                }
                            }
                        }
                        //차량 냉각수 온도값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                        dataFrame.Coolant_temperature = data_calculate;
                        next_data = speed;
                        //Log.i("data_int_error", read_data1);
                        e.printStackTrace();
                    }
                }
            } else {

                read_data1 = read_data[read_data.length - 1].replace("\r", "");
                int data_int;
                //Log.i("데이터 계산_split", read_data1);

                try {
                    // TODO 데이터 뒤에 0이 여러개 붙어 나올수 있기에 6개 이상이면 뒤 0을 자르고 아니면 그대로 16진수로 바꾼다.
                    // TODO rpm처럼 반반씩 나눠서 계산하는 데이터는 뒤에 0이 6개가 붙고 나머지 하나로 계산하는 데이터는 8개가 나온다. (아직까지 확인된 바로는...)
                    if(read_data1.length() >= 6){
                        String split_string = read_data1.substring(0, 2);
                        Log.i("split_string_coolant_temperature", split_string);
                        data_int = Integer.parseInt(split_string, 16);
                        data_int = data_int-40;
                    }else {
                        data_int = Integer.parseInt(read_data1, 16);
                    }
                    Log.i("data_int_7E_coolant_temperature", String.valueOf(data_int));
                    data_calculate = String.valueOf(data_int);

                    // 로그 화면 기록 / 계산 후 데이터
                    logDataFrame.INFO_Log.add("I/Coolant_temperature data calculation result: "+data_calculate);
                    // INFO 시간 저장
                    logDataFrame.INFO_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 냉각수 온도값 전역변수 저장
                    dataFrame.Coolant_temperature = data_calculate;
                    next_data = speed;

                } catch (Exception e) {
                    // 로그 화면 기록 / Coolant_temperature 계산에서 에러
                    logDataFrame.ERROR_Log.add("E/Coolant_temperature data calculation result Error!!: "+data_calculate);
                    // ERROR 시간 저장
                    logDataFrame.ERROR_nowTimelist.add(nowTime);
                    // nowTime_day사이즈가 0이면 바로 add한다.
                    if(logDataFrame.daylist.size() == 0){
                        logDataFrame.daylist.add(nowTime_day);
                    }else {
                        for (int i = 0; i < logDataFrame.daylist.size(); i++){
                            if(!String.join("@@@", logDataFrame.daylist).contains(nowTime_day)){

                                // 3일치만 저장할 것이기에 크기가 4이상이면 첫번째 날짜를 지운다.
                                if(logDataFrame.daylist.size() >= 5){
                                    logDataFrame.daylist.remove(0);
                                }else {
                                    // 로그 날짜 스피너에 넣을 어레이
                                    logDataFrame.daylist.add(nowTime_day);
                                }
                            }
                        }
                    }
                    //차량 냉각수 온도값 전역변수 저장 (막히더라도 다른 데이터는 실시간으로 갱신되도록 일단 넘어가는 조치)
                    dataFrame.Coolant_temperature = data_calculate;
                    next_data = speed;
                    //Log.i("data_int_error", read_data1);
                    e.printStackTrace();
                }
            }

        }

        return data_calculate;
    }

}
