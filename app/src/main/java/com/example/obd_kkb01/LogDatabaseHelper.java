package com.example.obd_kkb01;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class LogDatabaseHelper extends SQLiteOpenHelper {

    public static final String Database_name = "Log_Database.db"; // 데이터베이스 명
    public static final String Table_name = "Log_DataTable"; // 테이블 명

    // 테이블 항목
    public static final String COL_daylist = "daylist";
    public static final String COL_INFO_Log = "INFO_Log";
    public static final String COL_ERROR_Log = "ERROR_Log";
    public static final String COL_INFO_nowTimelist = "INFO_nowTimelist";
    public static final String COL_ERROR_nowTimelist = "ERROR_nowTimelist";

    public LogDatabaseHelper(@Nullable Context context) {
        super(context, Database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + Table_name + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "daylist TEXT, INFO_Log TEXT, ERROR_Log TEXT, INFO_nowTimelist TEXT, ERROR_nowTimelist TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ Table_name);
        onCreate(db);
    }

    // 데이터베이스 추가하기 insert
    // 로그 데이터 저장할때 사용할 예정
    public boolean insertData(String daylist, String INFO_Log, String ERROR_Log, String INFO_nowTimelist, String ERROR_nowTimelist){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_daylist, daylist);
        contentValues.put(COL_INFO_Log, INFO_Log);
        contentValues.put(COL_ERROR_Log, ERROR_Log);
        contentValues.put(COL_INFO_nowTimelist, INFO_nowTimelist);
        contentValues.put(COL_ERROR_nowTimelist, ERROR_nowTimelist);
        long result = db.insert(Table_name, null,contentValues);
        if (result == -1){

            return false;
        }else {

            return true;
        }
    }

    //데이터베이스 항목 읽어오기 Read
    //로그 데이터 읽어올때 사용할 예정
    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+Table_name, null);
        return res;
    }

    //데이터베이스 항목 읽어오기 Read
    //로그 데이터 선택해서 읽어올때 사용할 예정
    public Cursor getSelectData(String daylist){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+Table_name+" WHERE daylist="+daylist, null);
        return res;
    }

    // 데이터베이스 삭제하기
    // 로그 데이터가 3일치가 초과하면 이 메소드를 사용할 예정
    public Integer deleteData(String daylist){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(Table_name, "daylist = ? ", new String[]{daylist});
    }

    //데이터베이스 수정하기
    // 이 메소드는 아직 쓸 곳을 못찾음
    public boolean updateData(String daylist, String INFO_Log, String ERROR_Log, String INFO_nowTimelist, String ERROR_nowTimelist){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_daylist, daylist);
        contentValues.put(COL_INFO_Log, INFO_Log);
        contentValues.put(COL_ERROR_Log, ERROR_Log);
        contentValues.put(COL_INFO_nowTimelist, INFO_nowTimelist);
        contentValues.put(COL_ERROR_nowTimelist, ERROR_nowTimelist);
        db.update(Table_name, contentValues,"daylist = ?", new String[]{daylist});
        return true;
    }
}
