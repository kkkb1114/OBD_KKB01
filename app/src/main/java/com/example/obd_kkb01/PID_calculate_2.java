package com.example.obd_kkb01;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class PID_calculate_2 {

    String PID_Second_DTC_1, PID_Second_DTC_2, PID_DTC;
    ArrayList<String> PID2_result, PID3_result, PID_result;
    boolean PID_7E8 = false;
    boolean PID_7E8_7E9 = false;
    boolean PID_7E8_7E9_7EA = false;
    boolean PID_7E9 = false;
    boolean PID_7E9_7EA = false;
    boolean PID_7EA = false;


    /**
     *
     * Single Line 과 Multi Line 처리 내용
     * 1. 첫번째 배열은 처리하지않고 append
     * 2. 두번째 배열부터 앞에 5자리 없애고 append
     * 3. single Line은 처리되지 않고 그대로 출력
     * 4. Multi Line은 두번째부터 처리되고, 7E8 10 은 그대로 남아있음.
     *
     * */

    public ArrayList<String> PID2(String data){

        Log.i("PID2 시작", "PID2 시작");
        Log.i("PID2 시작", data);
        PID2_result = new ArrayList<String>();
        PID3_result = new ArrayList<String>();

        // 띄어쓰기가 붙어있는 것 같다. 변환 시켜주자.
        data.replaceAll("\r", "");
        data.replaceAll(" ", "");
        data.trim();
        data.replaceAll("(^\\p{Z}+|\\p{Z}+$)", "");

        if (data.contains("7E8")){
            Log.i("PID2 시작", "PID2 시작");
            first_PID_calculate(data,"7E8");
        }else if (data.contains("7E9")){
            Log.i("PID2 시작", "PID2 시작");
            first_PID_calculate(data,"7E9");
        }else if (data.contains("7EA")){
            Log.i("PID2 시작", "PID2 시작");
            first_PID_calculate(data,"7EA");
        }

        return PID3_result;
    }


    //todo 처음 계산이 이루어 지는 곳으로 header: 헤더, data: 계산할 데이터이다.
    public ArrayList<String> first_PID_calculate(String data, String header){

        Log.i("PID2 첫 계산", "PID2 첫 계산");
        if (header.equals("7E8")){
            PID3_result = second_PID_calculate_7E8(data);
        }else if (header.equals("7E9")){
            //PID3_result = second_PID_calculate_7E9(data);
        }else if (header.equals("7EA")){
            //PID3_result = second_PID_calculate_7EA(data);
        }
        return PID3_result;
    }

    //037E8100E4306010002007E821030043008200C17E822000000000000007E90443010101
    //TODO 헤더가 7E8일때 실행
    public ArrayList<String> second_PID_calculate_7E8(String data){



        return PID_result;
    }


    //todo 여기서 에러코드에 사용할 PID 계산을 하고 결과값을 내보낸다.
    public ArrayList<String> PID(String data) {

        String PID_data = data;
        int count = 0;
        int count2 = 0;
        // 가끔 STOPPED이 끼어서 들어오는 경우가 있어 예외 처리 함.
        if (data.contains("STOPPED")){
            PID_data = data.split("STOPPED")[0];
        }
        Log.e("eee", PID_data.contains("\r") + "");

        String[] data_Split = PID_data.replace("\r", "").split("");
        ArrayList<String> data_list = new ArrayList<String>(Arrays.asList(data_Split));
        Log.i("PID_data 길이", String.valueOf(data_list.size()));
        ArrayList<String> data_list_add = new ArrayList<String>();
        ArrayList<String> data_list_add2 = new ArrayList<String>();
        ArrayList<String> data_result = new ArrayList<String>();

        // 리스트는 계산식에서 remove로 인해 크기가 줄어들 예정이라 크기값을 따로 빼놓는다.
        int count3 = data_list.size();
        // 반복문으로 4번째 문자마다 결합하고 결합한 문자는 ArrayList에서 삭제해 버리는 동작은 반복한다.
        for (int i = 0; i < count3; i++) {
            if (count == 3) {
                count = 0;
                if (data_list.get(0).equals("")){
                    Log.i("data_list.get(0) 빔", data_list.get(0));
                    data_list.remove(0);
                }else if (data_list.get(1).equals("")){
                    Log.i("data_list.get(1) 빔", data_list.get(1));
                    data_list.remove(1);
                }else if (data_list.get(2).equals("")){
                    Log.i("data_list.get(2) 빔", data_list.get(2));
                    data_list.remove(2);
                }else if (data_list.get(3).equals("")){
                    Log.i("data_list.get(3) 빔", data_list.get(3));
                    data_list.remove(3);
                }
                String data_join = data_list.get(0)+data_list.get(1)+data_list.get(2)+data_list.get(3);
                Log.i("data_join 길이", String.valueOf(data_join.split("").length));
                data_list_add.add(data_join);
                Log.i("PID 계산 4글자 추출 부분", data_join);
                // 리스트에 있는 데이터를 4번 지워서 다음 4자리 문자열을 얻을 수 있게 준비한다.
                while (count2 <= 3) {
                    data_list.remove(0);
                    count2++;
                }
                count2 = 0;
            } else {
                count++;
            }
        }

        for (int i = 0; i < data_list_add.size(); i++) {
            if (data_list_add.get(i).split("")[0].equals("0")) {
                PID_Second_DTC_1 = "00";
                PID_Second_DTC_2 = "00";
                PID_DTC = "P0"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("1")) {
                PID_Second_DTC_1 = "00";
                PID_Second_DTC_2 = "01";
                PID_DTC = "P1"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("2")) {
                PID_Second_DTC_1 = "00";
                PID_Second_DTC_2 = "10";
                PID_DTC = "P2"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("3")) {
                PID_Second_DTC_1 = "00";
                PID_Second_DTC_2 = "11";
                PID_DTC = "P3"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("4")) {
                PID_Second_DTC_1 = "01";
                PID_Second_DTC_2 = "00";
                PID_DTC = "C0"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("5")) {
                PID_Second_DTC_1 = "01";
                PID_Second_DTC_2 = "01";
                PID_DTC = "C1"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("6")) {
                PID_Second_DTC_1 = "01";
                PID_Second_DTC_2 = "10";
                PID_DTC = "C2"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("7")) {
                PID_Second_DTC_1 = "01";
                PID_Second_DTC_2 = "11";
                PID_DTC = "C3"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("8")) {
                PID_Second_DTC_1 = "10";
                PID_Second_DTC_2 = "00";
                PID_DTC = "B0"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("9")) {
                PID_Second_DTC_1 = "10";
                PID_Second_DTC_2 = "01";
                PID_DTC = "B1"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("A")) {
                PID_Second_DTC_1 = "10";
                PID_Second_DTC_2 = "10";
                PID_DTC = "B2"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("B")) {
                PID_Second_DTC_1 = "10";
                PID_Second_DTC_2 = "11";
                PID_DTC = "B3"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("C")) {
                PID_Second_DTC_1 = "11";
                PID_Second_DTC_2 = "00";
                PID_DTC = "U0"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("D")) {
                PID_Second_DTC_1 = "11";
                PID_Second_DTC_2 = "01";
                PID_DTC = "U1"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("E")) {
                PID_Second_DTC_1 = "11";
                PID_Second_DTC_2 = "10";
                PID_DTC = "U2"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            } else if (data_list_add.get(i).split("")[0].equals("F")) {
                PID_Second_DTC_1 = "11";
                PID_Second_DTC_2 = "11";
                PID_DTC = "U3"+data_list_add.get(i).split("")[1]
                        +data_list_add.get(i).split("")[2]
                        +data_list_add.get(i).split("")[3];
            }

            data_result.add(PID_DTC);
        }
        return data_result;
    }
}
