package com.sbc.sk.schedulehelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddLocationDialogActivity extends AppCompatActivity {
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_dialog);

        // CalendarFragment로부터 sc_id, event_name 전달받기
        Intent intent = getIntent();
        long sc_id = intent.getLongExtra("sc_id", 1000);
        final String sc_id_str = String.valueOf(sc_id);
        final String event_name = intent.getStringExtra("event_name");

        dbHelper = new DatabaseHelper(getApplicationContext(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();

        Button btn_save = (Button) findViewById(R.id.btn_add_location_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText로부터 위도, 경도 값 전달받기
                EditText et_latitude = (EditText) findViewById(R.id.et_add_location_latitude);
                String latitude_str = et_latitude.getText().toString();

                EditText et_longitude = (EditText) findViewById(R.id.et_add_location_longitude);
                String longitude_str = et_longitude.getText().toString();

                if (latitude_str.matches("") | longitude_str.matches("")) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "위도 또는 경도 값이 NULL 입니다!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    String UPDATE_SQL = "update " + Const.TABLE_NAME
                            + " set latitude = ?, longitude = ?"
                            + " where scid = ?";
                    String[] args1 = {latitude_str, longitude_str, sc_id_str};
                    db.execSQL(UPDATE_SQL, args1);
                    Toast.makeText(AddLocationDialogActivity.this, "일정 " + event_name + " 위치정보 저장!", Toast.LENGTH_LONG).show();
                /*Snackbar.make(getWindow().getDecorView().getRootView(), "일정 " + event_name + " 위치정보 저장!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                */
                    finish();
                }
            }
        });

        Button btn_cancel = (Button) findViewById(R.id.btn_add_location_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
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

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
