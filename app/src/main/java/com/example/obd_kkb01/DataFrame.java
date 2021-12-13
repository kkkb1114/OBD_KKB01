package com.example.obd_kkb01;

import android.bluetooth.BluetoothSocket;

public class DataFrame {
    // 전역 변수로 데이터를 갱신,사용 하는데 쓰이는 클래스
    // 메인화면에서 실시간 데이터 갱신, 표시해주는 전역 변수
    static String Speed = "0";
    static String Rpm = "0";
    static String Engine_temperature = "0";
    static String Intake_air_temperature = "0";
    static String Coolant_temperature = "0";
    static boolean Scaner_connect = false;

    static int Rpm_int = 0;

    // 터미널 화면에서 PID코드 날릴리고 받을 때 스레드에서 텍스트를 받고 날리기 위한 전역 변수들.
    static String Terminal_Write_code = "0";
    static String Terminal_Read_code = "0";
    // 터미널 화면에 들어오면 잠깐 스레드를 멈출 동작.
    static boolean Terminal_code_boolean = false;
    static boolean stopThread = false;
    static boolean firststopThread = false;

    //앱이 꺼지면 알려줄 전역 변수
    static boolean ShutDownApp = false;

    //이미 소켓이 연결된 상태에서 다른 소켓을 연결 시도할 경우 사용될 전역 변수
    static boolean Diffirent_Socket = false;
}
