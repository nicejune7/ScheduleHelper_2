package com.sbc.sk.schedulehelper;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PunctualService extends Service implements GeoTask.Geo {

    //GPS 변수//
    protected LocationManager locationManager;
    //Location location;
    private GPS gps_lateness;
    private boolean isStop;
    private Context mContext;
    double lati;
    double longi;

    //Noti 진동 type 설정
    private final static long[] vibrate = new long[] { 100, 300, 100, 300, 100 };

    //연산 변수//
    public double time_distance= -10;         //GeoTask 를 이용한 시간
    public double time_appointment= -10;     //약속 시간까지 남은 시간
    private double var_time=-10;               //GeoTask 를 통한 현재 남은 시간 받는 변수.
    //send_distance_time()                       //현재 약속 시간까지 남은 시간
    private double final_time_distance=-10;  //GeoTask 를 통한 최초 예상 시간
    private double time_left=-10;              //최초 남은 시간

    public String str_from;//출발지점:By GPS

    //받아올 변수들
    public String str_to;//약속장소
    public String str_time_appointment;//약속시간


    public PunctualService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PunctualService", "서비스의 onCreate");

        str_time_appointment="20170813003000";           //임의로 설정.
        str_to="seoulstation";                             //임의로 설정.

        time_left=send_distance_time();
        Log.d("onCreate: 최초 남은 시간", Double.toString(time_left));

        isStop=false;
        gps_lateness = new GPS(this);
        mContext = this;

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 서비스가 호출될 때마다 실행
        Log.d("PunctualService", "서비스의 onStartCommand");

        Thread counter = new Thread(new Counter()); counter.start();

        return super.onStartCommand(intent, flags, startId);
    }

    private class  Counter implements Runnable {

        private int count;
        private int count_helper=0;
        private Handler handler = new Handler();

        //노티피케이션 설정
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.mipmap.ic_launcher_schedulehelper)
                        .setContentTitle("<Schedule Helper>")
                        .setContentText("Be Ready To Go :)");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        @Override public void run() { for (count = 0; count < (time_left); count++) { // STOP 버튼을 눌렀다면 종료한다.
/**
     * Thread 안에서는 UI와 관련된 Toast 쓸 수 없습니다.
     * 따라서, Handler를 통해 이용할 수 있도록 만들어줍니다.
**/
            if (isStop) {
                break;
            }

            handler.post(new Runnable() { @Override public void run() {

                //값이 없을 때, 서비스 중지.
                if ((send_distance_time() == -10) || (gps_lateness.location == null)) {
                    Log.d("ㄱ.", "Error");
                    isStop=true;
                }
                //값이 정상일 때, 본격적 서비스.
                else {
                    my_display();
                    getTime_Distance(gps_lateness.lat, gps_lateness.lon);

                    //최초 시행 시..
                    if (count == 0) {
                        lati = gps_lateness.lat;
                        longi = gps_lateness.lon;
                        getTime_Distance(lati, longi);

                        final_time_distance = time_distance; //최초 예상 소요 시간 값
                        if(final_time_distance!=-10){
                        Log.d("최초 예상  소요 시간:", Double.toString(var_time));
                        }
                    }

                    if(count_helper==0){
                        final_time_distance = time_distance; //최초 예상 소요 시간 값
                        if(final_time_distance!=-10) {
                            Log.d("최초 예상  소요 시간:", Double.toString(var_time));
                            count_helper++;
                        }
                    }

                    var_time = time_distance; //계속 갱신
                    Log.d("현재 예상  소요 시간:", Double.toString(var_time));

                    Log.d("현재 남은  시간     :", Double.toString(send_distance_time()));

                    //시간 설명:
                    //send_distance_time()=> time_appointment :약속시간까지 남은 시간.
                    //final_time_distance: 최초 예상 소요 시간.
                    //var_time =>time_distance : 현재 예상 소요 시간.

                    //모든 값이 비정상인 경우 =>Error
                    if((send_distance_time()==var_time)&&(var_time==-10)){
                        Log.d("ㄴ.","Error");
                        isStop = true;
                    }


                    //처음부터 이미 늦은 경우.
                    else if ((final_time_distance!=-10)&&send_distance_time()< final_time_distance) {
                        Toast.makeText(getApplicationContext(), "Be Hurry for the Schedule!!", Toast.LENGTH_LONG).show();
                        Log.d("ㄷ.", "Case1: 넌 이미 늦었다...");
                        isStop = true;
                    }
                    //여유가 있는 경우.
                    else if(send_distance_time()>var_time+15){
                        //Toast.makeText(getApplicationContext(), "일찍 도착하셨네요 :)", Toast.LENGTH_LONG).show();
                        Log.d("ㄷ.", "Case2: noti를 기다리자.");
                    }
                    //출발 15분 전인 경우.
                    else if(  ((send_distance_time()-var_time)<15) && (count_helper==0)||(count_helper==1)  ){
                        mBuilder.build().vibrate=vibrate;
                        mNotificationManager.notify(1, mBuilder.build()); //1=NOSOUND NOTIFICATION ID 값.
                        count_helper=10;//noti는 한번만 발생하도록.
                        Log.d("ㄷ.", "Case3: noti 발생 with 진동!!");
                    }
                    //예상 소요 시간에 비해 남은 시간이 거의 없는 경우.
                    else if((send_distance_time()<1.5)&&(var_time>1.5)){
                        Toast.makeText(getApplicationContext(), "일정에 늦으셨습니다 :(", Toast.LENGTH_LONG).show();
                        isStop=true;
                        Log.d("ㄷ.", "Case4: 너무 촉박해");
                    }
                    //남은 시간에 비해 예상 소요시간이 거의 없는 경우.
                    else if((send_distance_time()>1.5)&&(var_time<1.5)){
                        Toast.makeText(getApplicationContext(), "거의 도착하셨습니다 :)", Toast.LENGTH_LONG).show();
                        isStop=true;
                        Log.d("ㄷ.", "Case5: 여유 있게 왔군");
                    }
                    else if(send_distance_time()>1){
                        Log.d("ㄷ.", "Case6: 기다리자..");
                    }
                    else{
                        isStop=true;
                    }
                    // Log로 Count 찍어보기
                    Log.d("COUNT", count + "");
                }
            }
            });
            // Sleep을 통해 3분씩 쉬도록 한다.
            try {
                Thread.sleep(1000*60*1);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            handler.post(new Runnable() { @Override public void run() {
                Toast.makeText(getApplicationContext(), "서비스가 종료되었습니다.", Toast.LENGTH_SHORT).show();
            }
            });
        }
    }


    //gps의 위도, 경도 구하는 함수
    public void my_display(){
        // check if GPS enabled
        if(gps_lateness.isGetLocation()) {
            //Log.d("앞: gps is null? ", Boolean.toString(gps_lateness == null));
            gps_lateness.getLocation();
            double latitude = gps_lateness.getLatitude();
            double longitude = gps_lateness.getLongitude();
            // \n is for new line

            if ((latitude ==0)&&(longitude==0)){
                gps_lateness.getLocation();
                Toast.makeText(getApplicationContext(), "location==null임.. <0,0>", Toast.LENGTH_LONG).show();
            }

            if ((latitude != 0) && (longitude != 0)) {
                getTime_Distance(latitude,longitude);
                //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            }
        }
        else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps_lateness.showSettingsAlert();
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ///예상 이동 시간.
    public void getTime_Distance(double lati, double longi) {
        str_from = "" + lati + "," + "+" + longi + "";

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + str_from + "&destinations=" + str_to + "&mode=transit&language=fr-FR&avoid=tolls&key=AIzaSyDepLWIg1PnSeYnMkMwPoTJRzS4VpMr7go";
        new GeoTask(PunctualService.this).execute(url);
        Log.d("1Here's latitude is ", Double.toString(lati));
        Log.d("1Here's longitude is ", Double.toString(longi));
    }
    @Override
    public void setDouble(String result) {
        String res[]=result.split(",");
        time_distance=Double.parseDouble(res[0])/60;         //Double min
        //거리: int dist=Integer.parseInt(res[1])/1000;
        //단위 변경:
        //tv_result1.setText("Duration= " + (int) (min / 60) + " hr " + (int) (min % 60) + " mins");
        //tv_result2.setText("Distance= " + dist + " kilometers");
        Log.d("setDouble 예상 시간(분):", Double.toString(time_distance));
    }

    //남은 시간.
    public double send_distance_time(){
        try{
            String Datestr = str_time_appointment;

            //현재 시간 date
            Date curDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

            //약속 시간을 Date로 parsing 후 time 가져오기
            Date reqDate = sdf.parse(Datestr);
            long reqDateTime =reqDate.getTime();

            //현재시간을 약속 시간의 형태로 format 후 값 가져오기
            curDate = sdf.parse(sdf.format(curDate));
            long curDateTime = curDate.getTime();

            //분으로 표현
            time_appointment= (reqDateTime-curDateTime)/60/1000;
            Log.d("send_distance 남은 시간(분):", Double.toString(time_appointment));

            return time_appointment;

        }catch ( Exception e ){
            e.printStackTrace();
            return -10;
        }
    }

    @Override
    public void onDestroy() {
        // 서비스가 종료될 때 실행
        Log.d("PunctualService", "서비스의 onDestroy");
        super.onDestroy();
        isStop = true;
    }
}
