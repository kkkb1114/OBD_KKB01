package com.example.obd_kkb01;

import java.util.ArrayList;
import java.util.HashMap;

public class LogDataFrame {
    // 로그 화면에 계속 띄워줄 데이터
    static ArrayList<String> Log_d = new ArrayList<>();
    static ArrayList<String> Log_e = new ArrayList<>();
    static ArrayList<String> Log_i = new ArrayList<>();
    static ArrayList<String> Log_v = new ArrayList<>();
    static ArrayList<String> Log_w = new ArrayList<>();
    static ArrayList<String> Log = new ArrayList<>();
    // 해쉬맵에 넣어보려 했으나 3일치만 저장하려고 했을때 첫번째 데이터를 지우기 위해선 그 키값을 또 구해내야 하기에 그냥 어레이리스트로 간단하게 처리하기로함.
    static ArrayList<Integer> Log_id = new ArrayList<>();
    static ArrayList<String> daylist = new ArrayList<>();
    static ArrayList<String> INFO_Log = new ArrayList<>();
    static ArrayList<String> ERROR_Log = new ArrayList<>();
    static ArrayList<String> INFO_nowTimelist = new ArrayList<>();
    static ArrayList<String> ERROR_nowTimelist = new ArrayList<>();
    static ArrayList<String> savefile_Log_list = new ArrayList<>();

    // 처음 앱 켰을 때 데이터들을 담을 어레이 리스트 추가
    static ArrayList<Integer> first_Log_id = new ArrayList<>();
    static ArrayList<String> first_daylist = new ArrayList<>();
    static ArrayList<String> first_INFO_Log = new ArrayList<>();
    static ArrayList<String> first_ERROR_Log = new ArrayList<>();
    static ArrayList<String> first_INFO_nowTimelist = new ArrayList<>();
    static ArrayList<String> first_ERROR_nowTimelist = new ArrayList<>();

    // 로그 화면 날짜 선택 변수
    static String Log_day = "null";
    static String Log_search = "null";

    public void reset(){
        daylist = null;
        INFO_Log = null;
        ERROR_Log = null;
        INFO_nowTimelist = null;
        ERROR_nowTimelist = null;
        first_Log_id = null;
        first_daylist = null;
        first_INFO_Log = null;
        first_ERROR_Log = null;
        first_INFO_nowTimelist = null;
        first_ERROR_nowTimelist = null;
        Log_day = "null";
        Log_search = "null";
    }

}
