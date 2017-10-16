package com.sbc.snut.schedulehelper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Created by Administrator on 2017-08-05.
 */

public class Boosted_http extends BroadcastReceiver{

    // 부팅이 완료되었다는 브로드캐스트 정보를 수신하면 서비스를 자동으로 실행
    @Override
    public void onReceive(Context context, Intent intent) {

        //필요한 변수들
        SharedPreferences sp = context.getSharedPreferences("sp",Context.MODE_PRIVATE);
        SharedPreferences.Editor sp_editor = sp.edit();

        int intent_code=-10;            //초기 값 말도 안되게 설정
        String[] http_links = new String[30]; //최대 30개 받도록 설정

        //case1) 부팅하지 않고 Analysis 이용하는 경우//
        if((intent.getAction()==null)||((intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))==false)){

            intent_code = intent.getIntExtra("Boosted_http_code", -10);
            http_links[intent_code] = intent.getStringExtra("Boosted_links");

//            Log.d("Case1) Boost code는?", Double.toString((double) intent_code));
//            Log.d("link주소: ", http_links[intent_code]);

            sp_editor.putInt("intent_code1",intent_code);                 //총 count 계속 갱신해
            sp_editor.putString("http_links1"+intent_code ,http_links[intent_code]);
            sp_editor.commit();
        }

        //case2) 부팅시, 등록된 Web들을 모두 noti로 띄워줌과 동시에, Toast로 상황설명해줌.
        if((intent.getAction()!=null)){
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                Toast.makeText(context, "Scheudule Helper : 부팅 완료", Toast.LENGTH_SHORT).show(); //그냥 해둠..
//            Log.d("Case2) Boost code는?", Double.toString((double) intent_code));

                if(sp.getInt("intent_code1",-10)!=-10) {
                    for (int i = 0; i <= sp.getInt("intent_code1",-10); i++) {
                        String text = "Check this Web site periodically :) " + (i + 1) + "번쨰 입력한 사이트입니다.";

                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(context)
                                        .setAutoCancel(true)
                                        .setSmallIcon(R.mipmap.ic_launcher_schedulehelper_round)
                                        .setContentTitle(text);
                        NotificationManager mNotificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        // pending implicit intent to view url
                        Intent boostIntent = new Intent(Intent.ACTION_VIEW);
                        boostIntent.setData(Uri.parse(sp.getString("http_links1"+i," ")));
                        PendingIntent pending = PendingIntent.getActivity(context, i, boostIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notificationBuilder.setContentIntent(pending);
                        // using the same tag and Id causes the new notification to replace an existing one
                        mNotificationManager.notify(String.valueOf(System.currentTimeMillis()), i, notificationBuilder.build());
                    }
                    sp_editor.clear();  //SharedPreferences 초기화
                    sp_editor.commit();
                    Toast.makeText(context, "Scheudule Helper : Web notification 기능 초기화", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "Scheudule Helper : 중요한 사이트는 다시 등록해주세요", Toast.LENGTH_SHORT).show();
                }
            }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            else{
                Toast.makeText(context, "Scheudule Helper : Boosted_http_ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
