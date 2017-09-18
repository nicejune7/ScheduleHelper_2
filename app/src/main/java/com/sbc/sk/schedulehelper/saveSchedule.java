package com.sbc.sk.schedulehelper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class saveSchedule extends Service {
    public int count;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate()
    {
        count = 0;
        Toast.makeText(getApplicationContext(),"save 서비스 실행",Toast.LENGTH_LONG).show();
        Log.d("test", "save서비스의 onCreate");
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        count++;
        Log.d("test", "save서비스의 onStartCommand"+count);
        if(count>=2)
        {

            //Toast.makeText(getApplicationContext(),"this is need",Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), saveScheduleActivity.class);

            PendingIntent p = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);

            try {

                p.send();

            } catch (PendingIntent.CanceledException e) {

                e.printStackTrace();

            }
            count = 0;
            onDestroy();



        }

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {

        count = 0;
        Log.d("test", "save서비스의 onDestroy");
        Toast.makeText(getApplicationContext(),"save destroy",Toast.LENGTH_LONG).show();
        super.onDestroy();

    }

}
