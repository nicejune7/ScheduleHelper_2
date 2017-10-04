package com.sbc.sk.schedulehelper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateFormat;

import java.util.Date;

public class BriefingService extends Service {
    private NotificationManagerCompat mNotificationManager;
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    public BriefingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM월 dd일");
        String curDateStr = simpleDateFormat.format(calendar.getTime());

        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle("오늘의 일정 브리핑")
                .setSummaryText(curDateStr);

        dbHelper = new DatabaseHelper(this, Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();

        SharedPreferences pref = this.getSharedPreferences("pref", MODE_PRIVATE);
        int sc_account = pref.getInt("sc_account", 0);

        for (int traveler = 0; traveler<=sc_account; traveler++) {
            String SELECT_SQL = "select "
                    + " sctitle, "
                    + " startyear, "
                    + " startmonth, "
                    + " startdate, "
                    + " starthour, "
                    + " startminute, "
                    + " endyear, "
                    + " endmonth, "
                    + " enddate, "
                    + " endhour, "
                    + " endminute "
                    + " from " + Const.TABLE_NAME
                    + " where scid = ?";
            String[] args1 = {String.valueOf(traveler)};

            Cursor c2 = db.rawQuery(SELECT_SQL, args1);
            try {
                c2.moveToNext();

                if (c2.getInt(4) <= 9) {
                    if (c2.getInt(5) <= 9) {
                        inboxStyle.addLine("0" + c2.getInt(4) + ":" + "0" + c2.getInt(5) + " " + c2.getString(0));
                    } else {
                        inboxStyle.addLine("0" + c2.getInt(4) + ":" + c2.getInt(5) + " " + c2.getString(0));
                    }
                } else {
                    if (c2.getInt(5) <= 9) {
                        inboxStyle.addLine(c2.getInt(4) + ":" + "0" + c2.getInt(5) + " " + c2.getString(0));
                    } else {
                        inboxStyle.addLine(c2.getInt(4) + ":" + c2.getInt(5) + " " + c2.getString(0));
                    }
                }

                c2.close();
            } catch (CursorIndexOutOfBoundsException e) {
                // CursorIndexOutOfBoundsException NULL
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setStyle(inboxStyle)
                .setSmallIcon(R.mipmap.ic_launcher_schedulehelper_round)
                .setContentTitle("오늘의 일정 브리핑")
                .setContentText("내용을 확인하려면 아래로 스와이프 하세요!");

        mNotificationManager.notify(0, builder.build());
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Do Nothing
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
