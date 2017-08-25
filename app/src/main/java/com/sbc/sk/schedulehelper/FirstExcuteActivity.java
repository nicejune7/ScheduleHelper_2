package com.sbc.sk.schedulehelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class FirstExcuteActivity extends AppCompatActivity {
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    ViewPager vp;

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

        vp = (ViewPager)findViewById(R.id.vp);

        vp.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        vp.setCurrentItem(0);
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

    View.OnClickListener movePageListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int tag = (int) v.getTag();
            vp.setCurrentItem(tag);
        }
    };

    private class pagerAdapter extends FragmentStatePagerAdapter
    {
        public pagerAdapter(android.support.v4.app.FragmentManager fm)
        {
            super(fm);
        }
        @Override
        public android.support.v4.app.Fragment getItem(int position)
        {
            switch(position)
            {
                case 0:
                    return new FirstExcute1Fragment();
                case 1:
                    return new FirstExcute2Fragment();
                case 2:
                    return new FirstExcute3Fragment();
                default:
                    return null;
            }
        }
        @Override
        public int getCount()
        {
            return 3;
        }
    }
}
