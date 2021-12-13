package com.example.obd_kkb01;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatCodePointException;

public class PID_calculate {

    String PID_Second_DTC_1, PID_Second_DTC_2, PID_DTC;
    ArrayList<String> PID2_result, PID3_result;
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

    //TODO 헤더가 7E8일때 실행
    public ArrayList<String> second_PID_calculate_7E8(String data){

        // data안에 있는 \r를 기준으로 나누고 다시 합친다.
        if(data.contains("\r")){
            String[] datalist = data.split("\r");
            data = String.join("", datalist);
            Log.i("data에 특수문자가 있다.", "있다.");
        }
        // 7E8을 기준으로 일단 나눈다.
        String[] PID_split = data.split("7E8");
        String[] PID_split_7E9;
        String[] PID_split_7EA;
        // 각 헤더마다 마지막 데이터 00000000인지 아닌지 확인
        String[] split_7E8_lastdata;
        String[] split_7E8_7E9_lastdata;
        String[] split_7E8_7E9_7EA_lastdata;
        String[] PID_split_7E8_split, PID_split_7E9_split;

        // 헤더가 하나고 7E8도 하나면 바로 결과값이 나온다.
        // 뒷자리 기준으로 8자리만 가져온다.
        //TODO 처음 7E8 싱글라인
        if (PID_split.length<=2){
            // 싱글라인일 경우 그냥 7E8 스플릿 하면 [1]에 다음 데이터들이 있을수 있기에 예외처리로 자른다.
            if(PID_split[1].contains("7E9")){
                    PID_split_7E8_split = PID_split[1].split("7E9")[0].split("");
            }else {
                PID_split_7E8_split = PID_split[1].split("");
            }
            PID2_result.add(array_add(PID_split_7E8_split));

            // 만약 뒤에 헤더가 더 있으면 추가로 계산한다.
            if (PID_split[1].contains("7E9")){
                //TODO 7E8 - 7E9 싱글라인
                PID_split_7E9 = PID_split[1].split("7E9");
                if (PID_split_7E9.length<=2){
                    // 싱글라인이여도 뒤에 데이터가 더 있을 수 있으니 미리 잘라준다.
                    if(PID_split_7E9[PID_split_7E9.length-1].contains("7EA")){
                        String PID_split_7E9_join = String.join("", PID_split_7E9);
                        PID_split_7E9 = PID_split_7E9_join.split("7EA");
                        PID_split_7E9_split = PID_split_7E9[0].split("");
                    }else {
                        PID_split_7E9_split = PID_split_7E9[1].split("");
                    }
                    PID2_result.add(array_add(PID_split_7E9_split));

                    // 뒤에 데이터가 더 있으면 더해준다.
                    if(PID_split_7E9[PID_split_7E9.length-1].contains("7EA")){
                        String[] PID_split_7E8_7E9_7EA = PID_split_7E9[PID_split_7E9.length-1].split("7EA");
                        //TODO 싱글라인이면
                        if(PID_split_7E8_7E9_7EA.length <= 2){

                            PID2_result.add(array_add(PID_split_7E8_7E9_7EA[1].split("")));

                        //TODO 멀티라인이면
                        }else {
                            boolean PID_7E8_7E9_7EA_1 = false;
                            PID_split_7EA = PID_split[PID_split.length-1].split("7EA");
                            String[] split_7E8_7EA_lastdata = PID_split_7EA[PID_split_7EA.length-1].substring(2).split("");
                            String[] PID_split_7E8_7EA_split = PID_split_7EA[1].split("");
                            Log.i("PID_split[PID_split.length-1].substring(2)", PID_split[PID_split.length-1].substring(2));
                            // 맨 마지막 데이터가 모두 000000000 이런식으로 0만 나올수 있기 때문에 아래와 같은 for문으로 한글자씩 비교를 한다.
                            for (int i=0; i<split_7E8_7EA_lastdata.length-1; i++){
                                if (!split_7E8_7EA_lastdata[i].equals("0")){
                                    Log.i("마지막 데이터 0이 아닌가?", split_7E8_7EA_lastdata[i]);
                                    PID_7E8_7E9_7EA_1 = true;
                                }else {
                                    Log.i("마지막 데이터 0뿐인가?", split_7E8_7EA_lastdata[i]);
                                }
                            }
                            // 첫번째 헤더 데이터는 방식이 다르게 나오기 때문에 먼저 구분해서 계산하고 add한고 그 다음에 나머지 헤더 데이터를 for문으로 넣는다.
                            PID2_result.add(array_add(split_7E8_7EA_lastdata));
                            Log.i("array_add(PID_split_7E8_7EA_split)", array_add(split_7E8_7EA_lastdata));
                            for (int i=2; i<PID_split_7EA.length-1; i++){
                                PID2_result.add(PID_split_7EA[i].substring(2));
                                Log.i("PID_split_7EA[i].substring(2)", PID_split_7EA[i].substring(2));
                            }

                            // 만약 마지막 데이터가 모두 0이 아니면 기존과 같이 앞 2자리만 제외하고 모두 더한다.
                            if (PID_7E8_7E9_7EA_1){
                                PID2_result.add(PID_split_7EA[PID_split_7EA.length-1].substring(2));
                                Log.i("PID_split_7EA[PID_split_7EA.length-1].split(7EA)[0].substring(2)", PID_split_7EA[PID_split_7EA.length-1].split("7EA")[0].substring(2));
                                // 만약 마지막 데이터가 모두 0이면 마지막으로 00만 붙여준다.
                            }else {
                                PID2_result.add("00");
                                Log.i("00 추가", "00 추가");
                            }
                        }
                    }
                    //TODO 7E8 - 7E9 멀티라인
                }else {
                    PID_split_7E9_split = PID_split[PID_split.length-1].split("7E9");
                    // 첫번째 데이터 추출
                    String[] PID_split_7E9_split_firstdata = PID_split_7E9_split[1].split("");
                    // 7E9 데이터 뒤에 7EA가 있으면 안되니까 미리 잘라준다.
                    if (PID_split_7E9_split[PID_split_7E9_split.length-1].contains("7EA")){
                        // 마지막 데이터 추출
                        split_7E8_7E9_lastdata = PID_split_7E9_split[PID_split_7E9_split.length-1].split("7EA")[0].substring(2).split("");

                    }else {
                        split_7E8_7E9_lastdata = PID_split_7E9_split[PID_split_7E9_split.length-1].split("");
                    }
                    // 맨 마지막 데이터가 모두 000000000 이런식으로 0만 나올수 있기 때문에 아래와 같은 for문으로 한글자씩 비교를 한다.
                    for (int i=0; i<split_7E8_7E9_lastdata.length-1; i++){
                        if (!split_7E8_7E9_lastdata[i].equals("0")){
                            Log.i("마지막 데이터 0이 아닌가?", split_7E8_7E9_lastdata[i]);
                            PID_7E9 = true;
                        }else {
                            Log.i("마지막 데이터 0뿐인가?", split_7E8_7E9_lastdata[i]);
                        }
                    }
                    // 첫번째 데이터 삽입
                    PID2_result.add(array_add(PID_split_7E9_split_firstdata));
                    Log.i("array_add(PID_split_7E9_split_firstdata)", array_add(PID_split_7E9_split_firstdata));
                    // 2번째 데이터부터 삽입하는 부분
                    for (int i=2; i<PID_split_7E9_split.length-1; i++){
                        PID2_result.add(PID_split_7E9_split[i].substring(2));
                        Log.i("PID_split_7E9_split[i].substring(2)", PID_split_7E9_split[i].substring(2));
                    }

                    // 만약 마지막 데이터가 모두 0이 아니면 기존과 같이 앞 2자리만 제외하고 모두 더한다.
                    if (PID_7E9){
                        PID2_result.add(PID_split_7E9_split[PID_split_7E9_split.length-1].substring(2));
                        Log.i("PID_split[PID_split.length-1].split(7E9)[0].substring(2)", PID_split[PID_split.length-1].split("7E9")[0].substring(2));
                        // 만약 마지막 데이터가 모두 0이면 마지막으로 00만 붙여준다.
                    }else {
                        PID2_result.add("00");
                        Log.i("00 추가", "00 추가");
                    }

                    // 만약 뒤에 헤더가 더 있으면 추가로 계산한다.
                  if (PID_split[PID_split.length-1].contains("7EA")){
                             //TODO 7E8 - 7E9 - 7EA 싱글라인
                        PID_split_7EA = PID_split[PID_split.length-1].split("7EA");
                        if (PID_split_7EA.length<=2){
                            String[] PID_split_7EA_split = PID_split_7EA[1].split("");
                            PID2_result.add(array_add(PID_split_7EA_split));
                            Log.i("PID_split_7EA_싱글라인",PID2_result.toString());
                            //TODO 7E8 - 7E9 - 7EA 멀티라인
                        }else {
                            boolean PID_7E8_7E9_7EA = false;
                            PID_split_7EA = PID_split[PID_split.length-1].split("7EA");
                            String[] split_7E8_7EA_lastdata = PID_split_7EA[PID_split_7EA.length-1].substring(2).split("");
                            String[] PID_split_7E8_7EA_split = PID_split_7EA[1].split("");
                            Log.i("PID_split[PID_split.length-1].substring(2)", PID_split[PID_split.length-1].substring(2));
                            // 맨 마지막 데이터가 모두 000000000 이런식으로 0만 나올수 있기 때문에 아래와 같은 for문으로 한글자씩 비교를 한다.
                            for (int i=0; i<split_7E8_7EA_lastdata.length-1; i++){
                                if (!split_7E8_7EA_lastdata[i].equals("0")){
                                    Log.i("마지막 데이터 0이 아닌가?", split_7E8_7EA_lastdata[i]);
                                    PID_7E8_7E9_7EA = true;
                                }else {
                                    Log.i("마지막 데이터 0뿐인가?", split_7E8_7EA_lastdata[i]);
                                }
                            }
                            // 첫번째 헤더 데이터는 방식이 다르게 나오기 때문에 먼저 구분해서 계산하고 add한고 그 다음에 나머지 헤더 데이터를 for문으로 넣는다.
                            PID2_result.add(array_add(split_7E8_7EA_lastdata));
                            Log.i("array_add(PID_split_7E8_7EA_split)", array_add(split_7E8_7EA_lastdata));
                            for (int i=2; i<PID_split_7EA.length-1; i++){
                                PID2_result.add(PID_split_7EA[i].substring(2));
                                Log.i("PID_split_7EA[i].substring(2)", PID_split_7EA[i].substring(2));
                            }

                            // 만약 마지막 데이터가 모두 0이 아니면 기존과 같이 앞 2자리만 제외하고 모두 더한다.
                            if (PID_7E8_7E9_7EA){
                                PID2_result.add(PID_split_7EA[PID_split_7EA.length-1].substring(2));
                                Log.i("PID_split_7EA[PID_split_7EA.length-1].split(7EA)[0].substring(2)", PID_split_7EA[PID_split_7EA.length-1].split("7EA")[0].substring(2));
                                // 만약 마지막 데이터가 모두 0이면 마지막으로 00만 붙여준다.
                            }else {
                                PID2_result.add("00");
                                Log.i("00 추가", "00 추가");
                            }
                        }
                    }
                }

            }else if (PID_split[1].contains("7EA")){
                // 7E9도 하나면 싱글라인
                PID_split_7EA = PID_split[1].split("7EA");
                if (PID_split_7EA.length<=2){
                    String[] PID_split_7EA_split = PID_split_7EA[1].split("");
                    PID2_result.add(array_add(PID_split_7EA_split));
                    // 둘 이상이면 멀티라인
                }else {

                }
            }
            //TODO 처음 7E8 멀티라인
        }else {
            PID_split_7E8_split = PID_split[1].split("");
            if (PID_split[PID_split.length-1].contains("7E9")){
                split_7E8_lastdata = PID_split[PID_split.length-1].split("7E9")[0].substring(2).split("");
            }else if (PID_split[PID_split.length-1].contains("7EA")){
                split_7E8_lastdata = PID_split[PID_split.length-1].split("7EA")[0].substring(2).split("");
            }else {
                split_7E8_lastdata = PID_split[PID_split.length-1].split("");
            }
            // 맨 마지막 데이터가 모두 000000000 이런식으로 0만 나올수 있기 때문에 아래와 같은 for문으로 한글자씩 비교를 한다.
            for (int i=0; i<split_7E8_lastdata.length-1; i++){
                if (!split_7E8_lastdata[i].equals("0")){
                    Log.i("마지막 데이터 0이 아닌가?", split_7E8_lastdata[i]);
                    PID_7E8 = true;
                }else {
                    Log.i("마지막 데이터 0뿐인가?", split_7E8_lastdata[i]);
                }
            }
            // 첫번째 데이터 삽입
            PID2_result.add(array_add(PID_split_7E8_split));
            Log.i("array_add(PID_split_7E8_split)", array_add(PID_split_7E8_split));
            // 두번째 이후 데이터 삽입
            for (int i=2; i<PID_split.length-1; i++){
                PID2_result.add(PID_split[i].substring(2));
                Log.i("PID_split[i].substring(2)", PID_split[i].substring(2));
            }

            // 만약 마지막 데이터가 모두 0이 아니면 기존과 같이 앞 2자리만 제외하고 모두 더한다.
            if (PID_7E8){
                PID2_result.add(PID_split[PID_split.length-1].substring(2));
                Log.i("PID_split[PID_split.length-1].split(7E9)[0].substring(2)", PID_split[PID_split.length-1].split("7E9")[0].substring(2));
                // 만약 마지막 데이터가 모두 0이면 마지막으로 00만 붙여준다.
            }else {
                PID2_result.add("00");
                Log.i("00 추가", "00 추가");
            }

            // 만약 뒤에 헤더가 더 있으면 추가로 계산한다.
            //TODO 그 다음 7E9면
            if (PID_split[PID_split.length-1].contains("7E9")){
                // 만약 뒤에 헤더가 더 있으면 추가로 계산한다.
                //TODO 7E9 싱글 라인이면
                PID_split_7E9 = PID_split[PID_split.length-1].split("7E9");
                if (PID_split_7E9.length<=2){
                    PID_split_7E9_split = PID_split_7E9[1].split("");
                    PID2_result.add(PID_split_7E9[1].substring(PID_split_7E9_split.length-7, PID_split_7E9_split.length-1));
                    Log.i("PID_split_7E9_싱글라인",PID2_result.toString());
                    if (PID_split_7E9[PID_split_7E9.length-1].contains("7EA")) {

                    }
                    //TODO 7E9 멀티 라인이면
                }else {

                }
                //TODO 그 다음 7EA면
            }else if (PID_split[PID_split.length-1].contains("7EA")){
                //TODO 7EA 싱글 라인이면
                PID_split_7EA = PID_split[PID_split.length-1].split("7EA");
                if (PID_split_7EA.length<=2){
                    String[] PID_split_7EA_split = PID_split_7EA[1].split("");
                    PID2_result.add(array_add(PID_split_7EA_split));
                    Log.i("PID_split_7EA_싱글라인",PID2_result.toString());
                    //TODO 7EA 멀티 라인이면
                }else { // TODO 이구간 작업중!!!!!!!!!!
                    boolean PID_7E8_7EA = false;
                    PID_split_7EA = PID_split[PID_split.length-1].split("7EA");
                    String[] split_7E8_7EA_lastdata = PID_split_7EA[PID_split_7EA.length-1].substring(2).split("");
                    String[] PID_split_7E8_7EA_split = PID_split_7EA[1].split("");
                    Log.i("PID_split[PID_split.length-1].substring(2)", PID_split[PID_split.length-1].substring(2));
                    // 맨 마지막 데이터가 모두 000000000 이런식으로 0만 나올수 있기 때문에 아래와 같은 for문으로 한글자씩 비교를 한다.
                    for (int i=0; i<split_7E8_7EA_lastdata.length-1; i++){
                        if (!split_7E8_7EA_lastdata[i].equals("0")){
                            Log.i("마지막 데이터 0이 아닌가?", split_7E8_7EA_lastdata[i]);
                            PID_7E8_7EA = true;
                        }else {
                            Log.i("마지막 데이터 0뿐인가?", split_7E8_7EA_lastdata[i]);
                        }
                    }
                    // 첫번째 헤더 데이터는 방식이 다르게 나오기 때문에 먼저 구분해서 계산하고 add한고 그 다음에 나머지 헤더 데이터를 for문으로 넣는다.
                    PID2_result.add(array_add(PID_split_7E8_7EA_split));
                    Log.i("array_add(PID_split_7E8_7EA_split)", array_add(PID_split_7E8_7EA_split));
                    for (int i=2; i<PID_split_7EA.length-1; i++){
                        PID2_result.add(PID_split_7EA[i].substring(2));
                        Log.i("PID_split_7EA[i].substring(2)", PID_split_7EA[i].substring(2));
                    }

                    // 만약 마지막 데이터가 모두 0이 아니면 기존과 같이 앞 2자리만 제외하고 모두 더한다.
                    if (PID_7E8){
                        PID2_result.add(PID_split_7EA[PID_split_7EA.length-1].substring(2));
                        Log.i("PID_split_7EA[PID_split_7EA.length-1].split(7EA)[0].substring(2)", PID_split_7EA[PID_split_7EA.length-1].split("7EA")[0].substring(2));
                        // 만약 마지막 데이터가 모두 0이면 마지막으로 00만 붙여준다.
                    }else {
                        PID2_result.add("00");
                        Log.i("00 추가", "00 추가");
                    }
                }
            }
        }
        Log.i("최종 데이터", PID2_result.toString());
        //TODO 최종 데이터 계산
        ArrayList<String> PID_result = PID(String.join("", PID2_result));
        for (int i=0; i<PID_result.size(); i++){
            Log.i("계산된 에러 데이터", PID_result.get(i));
        }
        return PID_result;
    }

    //TODO 헤더가 7E9일때 실행
   /* public ArrayList<String> second_PID_calculate_7E9(String data){



        Log.i("최종 데이터", PID2_result.toString());
        //TODO 최종 데이터 계산
        ArrayList<String> PID_result = PID(String.join("", PID2_result));
        for (int i=0; i<PID_result.size(); i++){
            Log.i("계산된 에러 데이터", PID_result.get(i));
        }
        return PID_result;
    }*/

    //TODO 헤더가 7EA일때 실행
    /*public ArrayList<String> second_PID_calculate_7EA(String data){



        return "ASD";
    }*/


    //todo 문자열을 한글자씩 쪼개서 가져온 배열을 맨 뒤부터 8자리만 추출해서 문자열로 바꿔주는 메소드
    public String array_add(String[] array){

        for (int i = 0; i < array.length-1; i++){
            Log.i("뒤 8자리 추출 과정 길이", String.valueOf(array.length));
            Log.i("뒤 8자리 추출 과정 값", array[i]);
        }

        String array_add = array[array.length-8]+array[array.length-7]
                +array[array.length-6]+array[array.length-5]
                +array[array.length-4]+array[array.length-3]
                +array[array.length-2]+array[array.length-1];

        return array_add;
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
