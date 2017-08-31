package com.sbc.sk.schedulehelper;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Service2 extends Service {
    Location location;
    protected LocationManager locationManager;
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    private GPS gps2;
    private boolean isStop;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private  Context mContext;
    double lat1;
    double lon1;
    long Stime ;
    Location locationA = new Location("point A");
    double distance;
    Location locationB = new Location("point B");
    String[] projection = {



    };




    // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1;

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        gps2 = new GPS(this);


        mContext = this;
        Log.d("test", "서비스의 onCreate");

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);












    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");

        isStop=false;
        dbHelper = new DatabaseHelper(getApplicationContext(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getReadableDatabase();

        Thread counter = new Thread(new Counter());
        counter.start();


        return super.onStartCommand(intent, flags, startId);
    }

    private class  Counter implements Runnable {

        private int count;
        private Handler handler = new Handler();

        //노티피케이션 설정

        Intent ii2= new Intent(getApplicationContext(),saveSchedule.class);

        PendingIntent pi = PendingIntent.getService(getApplicationContext(),0,ii2,PendingIntent.FLAG_CANCEL_CURRENT);



        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("스케줄자동저장기능")
                        .setContentText("같은장소에 계속머물렀습니다 스케줄로 저장하시겠습니까?")
                        .addAction(R.drawable.ic_launcher,"Yes",pi)
                        .setAutoCancel(true);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        byte [] buffer = new byte[6];
        byte [] buffer2 = new byte[2];
        byte [] buffer3 = new byte[2];



        @Override public void run() { for (count = 0; count < 6; count++) { // STOP 버튼을 눌렀다면 종료한다.


            if (isStop) { break; } /** * Thread 안에서는 UI와 관련된 Toast를 쓸 수 없습니다. * 따라서, Handler를 통해 이용할 수 있도록 만들어줍니다. */
            handler.post(new Runnable() { @Override public void run() {
                // Toast로 Count 띄우기
                Log.d("tread", "thread");

                if(count==0) {
                    lat1 = gps2.lat;
                    lon1 = gps2.lon;
                    locationA.setLatitude(lat1);
                    locationA.setLongitude(lon1);
                    Stime = System.currentTimeMillis();

                }



                locationB.setLatitude(gps2.lat);
                locationB.setLongitude(gps2.lon);

                distance = locationA.distanceTo(locationB);
                if(distance<100) {
                    //  Toast.makeText(getApplicationContext(), distance + "m," + gps2.lat + "," + gps2.lon + " "+lat1+","+lon1, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "LocationSevice를 종료합니다.", Toast.LENGTH_SHORT).show();
                    isStop=true;
                    db.close();
                }


                Cursor c = db.rawQuery("SELECT*FROM "+Const.TABLE_NAME,null);
                c.moveToFirst();
                SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
                long gmt = 9*60*60*1000;

                if(c.getCount()!=0)
                {

/*
                String a1 = new String(""+c.getInt(4));
                if(c.getInt(4)<10) {a1 = new String("0"+c.getInt(4));}
                String a2 = new String(""+c.getInt(5));
                if(c.getInt(5)<10) {a2 = new String("0"+c.getInt(5));}
                String a3 = new String(""+c.getInt(6));
                if(c.getInt(6)<10) {a3 = new String("0"+c.getInt(6));}
                String a4 = new String(""+c.getInt(7));
                if(c.getInt(7)<10) {a4 = new String("0"+c.getInt(7));}
                String A = new String(c.getInt(3)+"-"+a1+"-"+a2+"-"+a3+":"+a4);
                String b1 = new String(""+c.getInt(9));
                if(c.getInt(9)<10) {b1 = new String("0"+c.getInt(9));}
                String b2 = new String(""+c.getInt(10));
                if(c.getInt(10)<10) {b2 = new String("0"+c.getInt(10));}
                String b3 = new String(""+c.getInt(11));
                if(c.getInt(11)<10) {b3 = new String("0"+c.getInt(11));}
                String b4 = new String(""+c.getInt(12));
                if(c.getInt(12)<10) {b4 = new String("0"+c.getInt(12));}
                String B = new String(c.getInt(8)+"-"+b1+"-"+b2+"-"+b3+":"+b4);
   */
                    String A = new String(c.getInt(3)+"-"+c.getInt(4)+"-"+c.getInt(5)+"-"+c.getInt(6)+":"+c.getInt(7));
                    String B = new String(c.getInt(8)+"-"+c.getInt(9)+"-"+c.getInt(10)+"-"+c.getInt(11)+":"+c.getInt(12));
                    try {
                        Date begintime = time.parse(A);
                        Date endtime = time.parse(B);
                        //   Date stime = time.parse("2017-08-31-20:42");

                        //  long diff = endtime.getTime()-begintime.getTime();

                        // Toast.makeText(getApplicationContext(),""+(System.currentTimeMillis()+gmt)+" "+stime.getTime(),Toast.LENGTH_LONG).show();
                        //  Toast.makeText(getApplicationContext(),""+time.format(begintime),Toast.LENGTH_LONG).show();
                        // Toast.makeText(getApplicationContext(),""+diff,Toast.LENGTH_LONG).show();
                        if(begintime.getTime()<(Stime+gmt)&&(System.currentTimeMillis()+gmt)<endtime.getTime())
                        {
                            isStop = true;
                            Log.d("2", "2");
                            db.close();
                        }
                        if(begintime.getTime()<(Stime+gmt+50*60*1000)&&(System.currentTimeMillis()+gmt)<endtime.getTime())
                        {
                            isStop = true;
                            Log.d("3", "3");
                            db.close();
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(),c.getInt(3)+" "+c.getInt(4)+" "+c.getInt(5)+" "+c.getInt(6)+" "+c.getInt(7)+" "+c.getInt(8)+" "+c.getInt(9)+" "+c.getInt(10)+" "+c.getInt(11)+" "+c.getInt(12),Toast.LENGTH_LONG).show();
                }



                while(c.moveToNext())
                {
                    String A = new String(c.getInt(3)+"-"+c.getInt(4)+"-"+c.getInt(5)+"-"+c.getInt(6)+":"+c.getInt(7));
                    String B = new String(c.getInt(8)+"-"+c.getInt(9)+"-"+c.getInt(10)+"-"+c.getInt(11)+":"+c.getInt(12));
                    try {
                        Date begintime = time.parse(A);
                        Date endtime = time.parse(B);
                        //   Date stime = time.parse("2017-08-31-20:42");

                        long diff = endtime.getTime()-begintime.getTime();


                        if(begintime.getTime()<(Stime+gmt)&&(System.currentTimeMillis()+gmt)<endtime.getTime())
                        {
                            Log.d("4", "4");
                            db.close();
                            isStop = true;
                        }
                        if(begintime.getTime()<(Stime+gmt+50*60*1000)&&(System.currentTimeMillis()+gmt)<endtime.getTime())
                        {
                            Log.d("5", "5");
                            db.close();
                            isStop = true;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(),c.getInt(3)+" "+c.getInt(4)+" "+c.getInt(5)+" "+c.getInt(6)+" "+c.getInt(7)+" "+c.getInt(8)+" "+c.getInt(9)+" "+c.getInt(10)+" "+c.getInt(11)+" "+c.getInt(12),Toast.LENGTH_LONG).show();
                }
                if(count==5)
                {

                    //FileInputStream is = null;
                    // try {
                    //  is = new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath()+ "/Documents/" + "schedule" + ".txt"));
                    // } catch (FileNotFoundException e) {
                    //   e.printStackTrace();
                    // }
                    //  ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    // String dr;
                    // int dr_i=0;

                    try{
                        // int a =is.read(buffer);
                        // String ymd = new String(buffer,0,a);
                        // int ymd_i = Integer.parseInt(ymd);
                        // is.read();

                        //  int b = is.read(buffer2);
                        //  String hour = new String(buffer2,0,b);
                        //  int hour_i = Integer.parseInt(hour);
                        // int c = is.read(buffer3);
                        //  String minute = new String(buffer3,0,c);
                        // int minute_i = Integer.parseInt(minute);
                        // is.read();

                        // int d;
                        //  try{
                        //     d = is.read();
                        // while (d != -1) {
                        //    byteArrayOutputStream.write(d);
                        //    d = is.read();
                        //  }
                        //    dr = new String(byteArrayOutputStream.toByteArray(),"MS949");
                        //     dr_i = Integer.parseInt(dr);
                        //    is.close();
                        // }catch(IOException e)
                        //    {
                        //       e.printStackTrace();
                        //   }

                        //    Toast.makeText(getApplicationContext(),ymd_i+" "+hour_i+" "+dr_i,Toast.LENGTH_SHORT).show();
                        long now =System.currentTimeMillis();
                        Date date =new Date(now);
                        SimpleDateFormat ymd_now = new SimpleDateFormat("yyMMdd");
                        String ymd_s =ymd_now.format(date);
                        SimpleDateFormat hour_now = new SimpleDateFormat("HH");
                        String hour_s = hour_now.format(date);
                        SimpleDateFormat minute_now = new SimpleDateFormat("mm");
                        String minute_s = minute_now.format(date);


                        // if(ymd_i==Integer.parseInt(ymd_s) && Math.abs(hour_i*60+minute_i-Integer.parseInt(hour_s)*60-Integer.parseInt(minute_s))<=dr_i)
                        // {
                        //   Toast.makeText(getApplicationContext(),"스케쥴이 겹칩니다.",Toast.LENGTH_SHORT).show();

                        // }
                        // else
                        //  {
                        //      Toast.makeText(getApplicationContext(),"기존에 있는 스케쥴의 날짜 시간 스케쥴이 지속되는시간은(분)"+ymd_i+" "+hour_i+" "+minute_i+" "+dr_i+"이고 현재날짜와 시간은 "+ymd_s+" "+hour_s+" "+minute_s+"이므로 스케쥴이 안겹칩니다",Toast.LENGTH_SHORT).show();
                        pi.send();
                        mNotificationManager.notify(0,mBuilder.build());
                        db.close();

                        //  }

                    }catch (Exception e){e.printStackTrace();}





                    //TODO 저장할건지 호출하는거 넣는위치
                }

                // Log로 Count 찍어보기

                Log.d("COUNT,", count + ""); } });
            // Sleep을 통해 1초씩 쉬도록 한다.
            try { Thread.sleep(1000*3); } catch (InterruptedException e) { e.printStackTrace(); } } handler.post(new Runnable() { @Override public void run() { Toast.makeText(getApplicationContext(), "서비스가 종료되었습니다.", Toast.LENGTH_SHORT).show(); } }); } }




    @Override
    public void onDestroy() {


        Log.d("test", "서비스의 onDestroy");
        super.onDestroy();
        db.close();
        // 서비스가 종료될 때 실행
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
