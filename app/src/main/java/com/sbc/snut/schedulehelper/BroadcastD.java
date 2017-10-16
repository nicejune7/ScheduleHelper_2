package com.sbc.snut.schedulehelper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

/**
 * Created by Administrator on 2017-07-26.
 */

public class BroadcastD extends BroadcastReceiver {

    //알람 시간이 되었을때 onReceive를 호출함
    @Override
    public void onReceive(Context context, Intent intent) {

        //필요한 변수들
        SharedPreferences sp_once = context.getSharedPreferences("sp_once",Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_once_editor = sp_once.edit();

        //intent로 받아오는 변수들.
        String link = intent.getStringExtra("key_http");
        int intent_number= intent.getIntExtra("key_intent_num",-10);
        int intent_code= intent.getIntExtra("key_code",-1); //3의 배수
        String text="Check this Web site periodically :) "+(intent_code+1)+"번쨰 입력한 사이트입니다.";


        //이 Class에서 저장하는 변수들.
        int count=sp_once.getInt("http_count",0);
//        Log.d(""+(intent_code+1)+"번째, count:ㄱ", Integer.toString(count));


        //오늘 요일: which_day
        Calendar judge_cal = Calendar.getInstance();
        int which_day = judge_cal.get(Calendar.DAY_OF_WEEK);
//        int which_hour= judge_cal.get(Calendar.HOUR_OF_DAY);

        ////////////////////////////////////////////////////////////////////////q부팅시 SharedPreferences초기화해야함
        if((intent.getAction()!=null)) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                sp_once_editor.clear();
                sp_once_editor.commit();
            }
        }
        ////////////////////////////////////////////////////////////////////////본격 Coding/////////////////////////////////
        //1. 일,월,목 NOTI 뜨는 날.
        if((which_day==1)||(which_day==2)||(which_day==5)) {

            //2. 새로운 http 주소를 입력한 경우,
//            Log.d("intent_code 현재 값:ㄴ", Integer.toString(intent_code));//제일 처음엔 -1 나와야돼.
//            Log.d("intent_code 저장 값:ㄴ", Integer.toString(sp_once.getInt("intent_code", -1)));
            if (intent_code - sp_once.getInt("intent_code", -1)==1) {
//            Log.d("50 Line 참조", "새로 만든 http 입니다.");

                //3. 월요일, 목요일에 http 입력시, noti 최대 1개 뜨도록 "Setting"
                if ( ((count == 0) && (which_day == 2)) || ((count <= 1) && (which_day == 5)) ) {
                    if (count < 2) {
                        count++;
                    }
//                    Log.d("현재 시간",Integer.toString(which_hour));

                    sp_once_editor.putInt("http_count", count);
                    sp_once_editor.commit();
//                    Log.d("count 값 1부터 시작:ㄴ", Integer.toString(sp_once.getInt("http_count", 0)));
                }
                //4. 일요일, 월요일, 목요일에 http 입력시, 실질적으로 noti 1개 띄워.
                else if ((which_day == 1) || ((sp_once.getInt("http_count", 0) == 1) && (which_day == 2)) || ((sp_once.getInt("http_count", 0) == 2) && (which_day == 5))) {

//                   Log.d("NEW!! count 값 1 또는 2임:ㄷ", Integer.toString(sp_once.getInt("http_count", 0)));

                    NotificationCompat.Builder notificationBuilder =
                            new NotificationCompat.Builder(context)
                                    .setAutoCancel(true)
                                    .setSmallIcon(R.mipmap.ic_launcher_schedulehelper_round)
                                    .setContentTitle(text);
                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    // pending implicit intent to view url
                    Intent resultIntent = new Intent(Intent.ACTION_VIEW);
                    resultIntent.setData(Uri.parse(link));
                    PendingIntent pending = PendingIntent.getActivity(context, ((intent_number) | (intent_number + 1) | (intent_number + 2)), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    notificationBuilder.setContentIntent(pending);
                    // using the same tag and Id causes the new notification to replace an existing one
                    mNotificationManager.notify(String.valueOf(System.currentTimeMillis()), ((intent_number) | (intent_number + 1) | (intent_number + 2)), notificationBuilder.build());

                    count=0;
                    sp_once_editor.putInt("intent_code",intent_code);
                    sp_once_editor.commit();
                    sp_once_editor.putInt("http_count",count);
                    sp_once_editor.commit();
                }
            } else {
                //               Log.d("기존", "그냥띄워");
                //기존:그냥 noti 띄워.
                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(context)
                                .setAutoCancel(true)
                                .setSmallIcon(R.mipmap.ic_launcher_schedulehelper_round)
                                .setContentTitle(text);
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // pending implicit intent to view url
                Intent resultIntent = new Intent(Intent.ACTION_VIEW);
                resultIntent.setData(Uri.parse(link));
                PendingIntent pending = PendingIntent.getActivity(context, ((intent_number) | (intent_number + 1) | (intent_number + 2)), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(pending);
                // using the same tag and Id causes the new notification to replace an existing one
                mNotificationManager.notify(String.valueOf(System.currentTimeMillis()), ((intent_number) | (intent_number + 1) | (intent_number + 2)), notificationBuilder.build());

                count=0;
                sp_once_editor.putInt("http_count",count);
                sp_once_editor.commit();
            }
        }

        else {
//            Log.d("Case","나머지 경우:화,수,금,토에 기능 수행");
//            Log.d("count==0? 처음엔 0이어야함.",Boolean.toString(count==0));
            if((which_day==3)||(which_day==4)){//화,수
                count++;
                sp_once_editor.putInt("http_count",count);
                sp_once_editor.commit();
                if(sp_once.getInt("http_count",-10)==2){
                    count=0;
                    sp_once_editor.putInt("intent_code",intent_code);
                    sp_once_editor.putInt("http_count",count);
                    sp_once_editor.commit();
                }
            }
            else if((which_day==6)||(which_day==7)) {//금,토
                count++;
                sp_once_editor.putInt("http_count", count);
                sp_once_editor.commit();

                if (sp_once.getInt("http_count", -10) == 3) {
                    count = 0;
                    sp_once_editor.putInt("intent_code", intent_code);
                    sp_once_editor.putInt("http_count", count);
                    sp_once_editor.commit();
                }
            }
        }
    }
}
