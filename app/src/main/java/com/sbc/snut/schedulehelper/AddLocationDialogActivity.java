package com.sbc.snut.schedulehelper;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AddLocationDialogActivity extends AppCompatActivity {
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    String sc_id_str = "";
    String event_name = "";

    String responseStringBuilder_str = "";
    String status = "";
    Double latitude = 999.9;
    Double longitude = 999.9;

    String goal_time_str = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location_dialog);

        // CalendarFragment로부터 sc_id, event_name 전달받기
        Intent intent = getIntent();
        long sc_id = intent.getLongExtra("sc_id", 1000);
        sc_id_str = String.valueOf(sc_id);
        event_name = intent.getStringExtra("event_name");

        dbHelper = new DatabaseHelper(getApplicationContext(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();

        Button btn_save = (Button) findViewById(R.id.btn_add_location_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Google MAP API Geolocation로부터 위도, 경도 값 전달받기
                EditText et_place = (EditText) findViewById(R.id.et_add_location_place);
                String place = et_place.getText().toString();
                final String UrlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + place + "&key=AIzaSyCHrJauJnKPa4zB7rX1zwn4Ca2P478ZUNI";

                new Thread() {
                    public void run() {
                        URL url = null;
                        try {
                            url = new URL(UrlString);
                            url.openConnection();
                            trustAllHosts();
                            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                            httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            });

                            httpsURLConnection.setRequestMethod("GET");
                            httpsURLConnection.setDoInput(true);
                            httpsURLConnection.setDoOutput(true);

                            httpsURLConnection.connect();

                            StringBuilder responseStringBuilder = new StringBuilder();
                            if (httpsURLConnection.getResponseCode() == httpsURLConnection.HTTP_OK) {
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                                for ( ; ; ) {
                                    String stringLine = bufferedReader.readLine();
                                    if (stringLine == null) {
                                        break;
                                    }
                                    responseStringBuilder.append(stringLine + '\n');
                                }
                                bufferedReader.close();
                            }
                            httpsURLConnection.disconnect();

                            responseStringBuilder_str = responseStringBuilder.toString();

                            JSONObject jsonObject1 = new JSONObject(responseStringBuilder.toString());
                            JSONArray jsonArray1 = jsonObject1.getJSONArray("results");
                            JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
                            JSONObject jsonObject3 = jsonObject2.getJSONObject("geometry");
                            JSONObject jsonObject4 = jsonObject3.getJSONObject("location");

                            status = jsonObject1.getString("status");
                            latitude = jsonObject4.getDouble("lat");
                            longitude = jsonObject4.getDouble("lng");

                            handler.sendMessage(handler.obtainMessage());
                        } catch (MalformedURLException e1) {
                            e1.printStackTrace();
                            handler.sendMessage(handler.obtainMessage());
                        } catch (ProtocolException e1) {
                            e1.printStackTrace();
                            handler.sendMessage(handler.obtainMessage());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            handler.sendMessage(handler.obtainMessage());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            try {
                                JSONObject jsonObject5 = new JSONObject(responseStringBuilder_str);
                                status = jsonObject5.getString("status");
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                Snackbar.make(getWindow().getDecorView().getRootView(), "두번째 try문에서 오류발생!", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                            handler.sendMessage(handler.obtainMessage());
                        }
                    }
                }.start();
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            if (latitude.toString().matches("999.9") | longitude.toString().matches("999.9")) {
                if (status.matches("ZERO_RESULTS")) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "입력하신 약속장소를 찾을 수 없습니다!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (!status.matches("OK")) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "인터넷연결 또는 인증상태를 확인하세요!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "위도 또는 경도 값이 NULL 입니다!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            } else {
                String UPDATE_SQL = "update " + Const.TABLE_NAME
                        + " set latitude = ?, longitude = ?"
                        + " where scid = ?";
                String[] args1 = {latitude.toString(), longitude.toString(), sc_id_str};
                db.execSQL(UPDATE_SQL, args1);
                Toast.makeText(AddLocationDialogActivity.this, "일정 " + event_name + " 위치정보 저장!", Toast.LENGTH_LONG).show();
                /*Snackbar.make(getWindow().getDecorView().getRootView(), "일정 " + event_name + " 위치정보 저장!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                */

                Intent intent = new Intent(
                        getApplicationContext(),//현재제어권자
                        PunctualService.class); // 이동할 컴포넌트
                intent.putExtra("place_latitude", latitude);
                intent.putExtra("place_longitude", longitude);

                intent.putExtra("goal_time", goal_time_str);

                startService(intent);

                finish();
            }
        }
    };

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain,
                    String authType)
                    throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub

            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
