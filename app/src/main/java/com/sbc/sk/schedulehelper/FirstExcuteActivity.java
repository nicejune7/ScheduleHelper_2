package com.sbc.sk.schedulehelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class FirstExcuteActivity extends AppCompatActivity {
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_excute);

        // sdcard 사용 권한 확인 및 요청
        int permissionCheckResult = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckResult2 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET);
        int permissionCheckResult3 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheckResult4 = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (permissionCheckResult != PackageManager.PERMISSION_GRANTED||permissionCheckResult2!=PackageManager.PERMISSION_DENIED||permissionCheckResult3!=PackageManager.PERMISSION_DENIED||permissionCheckResult4!=PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET},
                    Const.MY_PERMISSION_REQUEST_STORAGE);
        } else {
            // NULL
        }

        dbHelper = new DatabaseHelper(getApplicationContext(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();

        Button btn_firstexcute_finish = (Button) findViewById(R.id.btn_firstexcute_finish);
        btn_firstexcute_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_SQL = "create table " + Const.TABLE_NAME + "("
                    + " _id integer PRIMARY KEY autoincrement, "
                    + " scid integer, "
                    + " sctitle text, "
                    + " startyear integer, "
                    + " startmonth integer, "
                    + " startdate integer, "
                    + " starthour integer, "
                    + " startminute integer, "
                    + " endyear integer, "
                    + " endmonth integer, "
                    + " enddate integer, "
                    + " endhour integer, "
                    + " endminute integer)";
            db.execSQL(CREATE_SQL);

            String CREATE_MEMO = "create table " + Const.TABLE_MEMO + "("
                    + " _id integer PRIMARY KEY autoincrement, "
                    + " memoid integer, "
                    + " contents text)";
            db.execSQL(CREATE_MEMO);

            String CREATE_LOCATION = "create table " + Const.TABLE_LOCATION + "("
                    + " _id integer PRIMARY KEY autoincrement, "
                    + " locationid integer, "
                    + " location text)";
            //db.execSQL(CREATE_LOCATION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
