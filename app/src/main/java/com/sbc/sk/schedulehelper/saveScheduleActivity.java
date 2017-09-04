package com.sbc.sk.schedulehelper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017-07-23.
 */

public class saveScheduleActivity extends Activity {



    private Button btn1;


    private EditText editText1;
    private EditText editText2;
    private EditText editText3;

    public SQLiteDatabase db;
    public DatabaseHelper dbHelper;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saveschedule);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        //Date date2 = new Date(now+7*24*60*60*1000);
        SimpleDateFormat hour_now = new SimpleDateFormat("yy/MM/dd HH:mm");
        String hour_s = hour_now.format(date);
        editText1 = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        btn1 = (Button) findViewById(R.id.button);



        editText2.setText("50");
        editText3.setText(hour_s);




        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String title = editText1.getText().toString();
                String et2 = editText2.getText().toString();
                String et3 = editText3.getText().toString();




               // Toast.makeText(getApplicationContext(),et3,Toast.LENGTH_LONG).show();


                int year =  Integer.parseInt(et3.substring(0,2));
                int month = Integer.parseInt(et3.substring(3,5));
                int day = Integer.parseInt(et3.substring(6,8));
                int hour = Integer.parseInt(et3.substring(9,11));
                int minute = Integer.parseInt(et3.substring(12,14));


               // Toast.makeText(getApplicationContext(),year+""+month+""+day+""+hour+""+minute++et2,Toast.LENGTH_LONG).show();
                dbHelper = new DatabaseHelper(getApplicationContext(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
                db = dbHelper.getWritableDatabase();
                insertRecord((String) title, (year+2000), month, day, hour, minute, (year+2000), month, day, (hour+1), minute);
                insertRecord((String) title, (year+2000), month, day+7, hour, minute, (year+2000), month, day+7, (hour+1), minute);
                insertRecord((String) title, (year+2000), month, day+14, hour, minute, (year+2000), month, day+14, (hour+1), minute);
                Toast.makeText(getApplicationContext(),"저장이 완료되었습니다",Toast.LENGTH_SHORT).show();


              //  ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              //  byteArrayOutputStream.write(b1,0,b1.length);
              //  byteArrayOutputStream.write(year);
              //  byte[] a =  byteArrayOutputStream.toByteArray();
              //  String a2b = new String(a,0,a.length);



                //Toast.makeText(getApplicationContext(),a2b,Toast.LENGTH_LONG).show();


           }
        });

    }
    public void insertRecord(
            String sctitle,
            int startyear,
            int startmonth,
            int startdate,
            int starthour,
            int startminute,
            int endyear,
            int endmonth,
            int enddate,
            int endhour,
            int endminute
    ) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        int sc_id = pref.getInt("sc_id", 0);
        int sc_account = pref.getInt("sc_account", 0);

        String INSERT_SQL = "insert into " + Const.TABLE_NAME + "("
                + "scid, "
                + "sctitle, "
                + "startyear, "
                + "startmonth, "
                + "startdate, "
                + "starthour, "
                + "startminute, "
                + "endyear, "
                + "endmonth, "
                + "enddate, "
                + "endhour, "
                + "endminute"
                + ") "
                + "values ("
                + sc_id + ", "
                + "'" + sctitle + "', "
                + startyear + ", "
                + startmonth + ", "
                + startdate + ", "
                + starthour + ", "
                + startminute + ", "
                + endyear + ", "
                + endmonth + ", "
                + enddate + ", "
                + endhour + ", "
                + endminute + ");";
        db.execSQL(INSERT_SQL);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("sc_id", sc_id+1);
        editor.putInt("sc_account", sc_account+1);
        editor.commit();
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }










}
