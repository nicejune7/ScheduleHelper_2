package com.sbc.sk.schedulehelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class SettingActivity extends Activity  {



    private Button btnShowLocation;
  //  private Button change;
    private EditText et_previousschuedule;

    private TextView txtLat;

    private TextView txtLon;



    // GPSTracker class

    private GPS gps;



    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_layout);


        btnShowLocation = (Button) findViewById(R.id.btn_start);
       // change = (Button)findViewById(R.id.button_change);
        txtLat = (TextView) findViewById(R.id.Latitude);
        txtLon = (TextView) findViewById(R.id.Longitude);
       // et_previousschuedule = (EditText)findViewById(R.id.et_previousschedule);





/*
        change.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){
                String ps = et_previousschuedule.getText().toString();
                int year =  Integer.parseInt(ps.substring(0,2));
                int month = Integer.parseInt(ps.substring(3,5));
                int day = Integer.parseInt(ps.substring(6,8));
                int hour = Integer.parseInt(ps.substring(9,11));
                int minute = Integer.parseInt(ps.substring(12,14));
                int duration = Integer.parseInt(ps.substring(15,17));
                Toast.makeText(getApplicationContext(),year+" "+month+" "+day+" "+hour+" "+minute,Toast.LENGTH_LONG).show();
                FileOutputStream fo = null;
                File file = new File(Environment.getExternalStorageDirectory().getPath()+ "/Documents/" + "schedule" + ".txt");
                try {
                    fo = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String a = new String(ps.substring(0,2)+ps.substring(3,5)+ps.substring(6,8)+" "+ps.substring(9,11)+ps.substring(12,14)+" "+ps.substring(15,17));
                try {
                    fo.write(a.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        */




        // GPS 정보를 보여주기 위한 이벤트 클래스 등록


        btnShowLocation.setOnClickListener(new View.OnClickListener() {



            public void onClick(View arg0) {
                gps = new GPS(SettingActivity.this);
                gps.getLocation();

                if (gps.isGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    txtLat.setText(String.valueOf(latitude));
                    txtLon.setText(String.valueOf(longitude));

                    Toast.makeText(getApplicationContext(),"당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude, Toast.LENGTH_LONG).show();


                    Intent intent = new Intent(
                            getApplicationContext(),//현재제어권자
                            Service2.class); // 이동할 컴포넌트
                    startService(intent); // 서비스 시작

                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }




            }


        });






    }


}

