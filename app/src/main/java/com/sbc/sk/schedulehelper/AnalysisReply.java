package com.sbc.sk.schedulehelper;

/*
0번기능 : 메모 등록
1번기능 : 스케줄 등록
2번기능 : 스케줄 등록 및 카톡 공유기능

4번기능 : 근수형 function 1
5번기능 : 지각알려줌.
*/

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class AnalysisReply extends Service {
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    public CharSequence command;
    public int fn_number;
    public CharSequence sc_title;
    public int sc_year;
    public int sc_month;
    public int sc_day;
    public int sc_hour;
    public int sc_min;
    public CharSequence memo;

    //////////////////////////////////////////////////////////////////////////////////////////////
    //function4를 위한 변수들.
    private static final long A_WEEK = 1000 * 60 * 60 * 24 * 7; //일주일 치 milli second
    public  int http_counting=0;
    public  int[] intent_timing_var = new int[2];
    private AlarmHATT[] http_array= new AlarmHATT[30];
    //////////////////////////////////////////////////////////////////////////////////////////////

    public AnalysisReply() {
        // This should be NULL
    }

    @Override
    public void onCreate() {

        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CharSequence reply;
        int y;
        reply = intent.getCharSequenceExtra("reply");
        y = Analysis_command(reply);
        if (y == 2) {
            Intent i = new Intent(getApplicationContext(), ShareActivity.class);

            PendingIntent p = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
            try {

                p.send();

            } catch (PendingIntent.CanceledException e) {

                e.printStackTrace();

            }
        }
        stopSelf(startId);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int Analysis_command(CharSequence input) {
        dbHelper = new DatabaseHelper(getApplicationContext(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();

        //db = MainActivity.returnDB();

        String input_s = input.toString();
        char fn_character = input_s.charAt(0);


////////////////////////////////////////////////////////////////////////
        //앞 String 4글자를 취해서 http이면 별도로 처리하자. 물론 4글자 이상이어야함..
        //소문자여야됨. (Http는 http랑 대소문자 다름.. 어차피 Url 복붙시 모두 소문자.)
        if (input_s.length() >= 4) {
            String fn_http = input_s.substring(0, 4);//★★★
            if (fn_http.equals("http") == true) {

                //전역변수들
                SharedPreferences sp = getSharedPreferences("sp", MODE_PRIVATE);
                http_counting = sp.getInt("http_counting", 0);
                intent_timing_var[0] = sp.getInt("intent_timing_var[0]", 0);
                intent_timing_var[1] = sp.getInt("intent_timing_var[1]", 0);

                //변수 불러와
                int[] Var_saved = new int[2];
                Var_saved = Var_upgrade(0, 100, http_counting);
                int http_code = Var_saved[0];
                int http_intent_number = Var_saved[1];

                //TO-DO SOME THING
                Intent boosted_intent = new Intent(getApplicationContext(), Boosted_http.class);
                boosted_intent.putExtra("Boosted_http_code", http_code);
                boosted_intent.putExtra("Boosted_links", input_s);
                getApplicationContext().sendBroadcast(boosted_intent);

                http_array[http_code] = new AlarmHATT(getApplicationContext());
                http_array[http_code].Alarm(input_s, http_code, http_intent_number);
//                Log.d("Analysis", "Log시작");
//                Log.d("Var_saved", Double.toString((double) Var_saved[0]));  //code 초기값 = 0
//                Log.d("Var_saved 100~ 위", Double.toString((double) Var_saved[1])); // 0부터 시작
//                Log.d("http 관련 count(전역)", Double.toString((double) http_counting));
//                Log.d("http code 관련(지역)", Double.toString((double) http_code));
//                Log.d("차이:분", Double.toString((double) intent_timing_var[0]));
//                Log.d("차이:시간", Double.toString((double) intent_timing_var[1]));
//                Log.d("Analysis", "Log끝");

                //변수 Edit
                SharedPreferences.Editor sp_editor = sp.edit();
                sp_editor.putInt("http_counting", http_counting);
                sp_editor.putInt("intent_timing_var[0]", intent_timing_var[0]);
                sp_editor.putInt("intent_timing_var[1]", intent_timing_var[1]);
                sp_editor.commit();

                if (http_counting == 29) {
                    Toast.makeText(getApplicationContext(), "죄송합니다. Web 알림기능은 최대 30개까지 지원합니다", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "따라서 모든 Web 알림기능은 자동 초기화됩니다.", Toast.LENGTH_SHORT).show();
                    sp_editor.clear();
                    sp_editor.commit();
                }
            }
        }
/////////////////////////////////////////////////////////////////////////////////

        if (fn_character == '0'
                | fn_character == '1'
                | fn_character == '2'
                | fn_character == '3'
                | fn_character == '4'
                | fn_character == '5'
                | fn_character == '6'
                | fn_character == '7'
                | fn_character == '8'
                | fn_character == '9') {
            fn_number = 1;

            try{
            String sc_date, sc_time, sc_title_s;
            String[] s_array;
            s_array = input_s.split("\\.");

            sc_date = s_array[0];
            sc_time = s_array[1];
            sc_title_s = s_array[2];

            sc_year = Integer.parseInt(sc_date.substring(0, 2));
            sc_month = Integer.parseInt(sc_date.substring(2, 4));
            sc_day = Integer.parseInt(sc_date.substring(4, 6));
            sc_hour = Integer.parseInt(sc_time.substring(0, 2));
            sc_min = Integer.parseInt(sc_time.substring(2, 4));
            sc_title = sc_title_s;

            memo = null;

            insertRecord((String) sc_title, (sc_year + 2000), sc_month, sc_day, sc_hour, sc_min, (sc_year + 2000), sc_month, sc_day, (sc_hour + 1), sc_min);
            return 1;
            }
            catch (java.lang.ArrayIndexOutOfBoundsException error1){
                fn_number = 0;

                sc_year = 0;
                sc_month = 0;
                sc_day = 0;

                sc_hour = 0;
                sc_min = 0;

                sc_title = null;

                memo = input;

                insertMemo((String) memo);
                Toast.makeText(getApplicationContext(), "그러나 일정 추가 기능에 부적절한 형식이니\n올바른 양식을 다시 참조하여 주십시오 :)", Toast.LENGTH_LONG).show();
            }return -1;
        }

        else if (fn_character == '#') {
            fn_number = 2;
            try {
                String sc_date, sc_time, sc_title_s;
                String[] s_array;
                s_array = input_s.split("\\.");

                sc_date = s_array[1];
                sc_time = s_array[2];
                sc_title_s = s_array[3];

                sc_year = Integer.parseInt(sc_date.substring(0, 2));
                sc_month = Integer.parseInt(sc_date.substring(2, 4));
                sc_day = Integer.parseInt(sc_date.substring(4, 6));

                sc_hour = Integer.parseInt(sc_time.substring(0, 2));
                sc_min = Integer.parseInt(sc_time.substring(2, 4));

                sc_title = sc_title_s;

                memo = null;

                insertRecord((String) sc_title, (sc_year + 2000), sc_month, sc_day, sc_hour, sc_min, (sc_year + 2000), sc_month, sc_day, (sc_hour + 1), sc_min);

                sendRecord((String) sc_title, (sc_year + 2000), sc_month, sc_day, sc_hour, sc_min, (sc_year + 2000), sc_month, sc_day, (sc_hour + 1), sc_min);
                return 2;

            } catch (java.lang.ArrayIndexOutOfBoundsException error2) {
                fn_number = 0;

                sc_year = 0;
                sc_month = 0;
                sc_day = 0;

                sc_hour = 0;
                sc_min = 0;

                sc_title = null;

                memo = input;

                insertMemo((String) memo);

                Toast.makeText(getApplicationContext(), "그러나 일정 공유 기능에 부적절한 형식이니\n올바른 양식을 다시 참조하여 주십시오 :)", Toast.LENGTH_LONG).show();
            }
        return -1;
        }

        else if((input_s.length()==1) &&(fn_character == '%')) {
            Intent i = new Intent(getApplicationContext(), SettingActivity.class);
            PendingIntent p = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
            try {
                p.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
            return 4;
        }
         else if((input_s.length()==1)&&(fn_character=='@')){
            Intent intent = new Intent(
                    getApplicationContext(),//현재제어권자
                    PunctualService.class); // 이동할 컴포넌트
            startService(intent); // 서비스 시작

            return 5;
            }

         else {
            fn_number = 0;

            sc_year = 0;
            sc_month = 0;
            sc_day = 0;

            sc_hour = 0;
            sc_min = 0;

            sc_title = null;

            memo = input;

            insertMemo((String) memo);
            return 0;
        }
    }

    /*  이거 필요 없는 듯? (by 현수)
        public String getAllMessages() {
            return "----- AnalysisReply -----\n"
                    + "fn_number : " + fn_number + "\n\n"
                    + "sc_year : " + sc_year + "\n"
                    + "sc_month : " + sc_month + "\n"
                    + "sc_day : " + sc_day + "\n\n"
                    + "sc_hour : " + sc_hour + "\n"
                    + "sc_min : " + sc_min + "\n\n"
                    + "sc_title : " + sc_title + "\n"
                    + "memo : " + memo + "\n";
        }
    */
    public void insertRecord(
            String sctitle,
            int startyear,
            int startmonth,
            int startdate,
            int starthour,
            int startminute,
            int endyear,
            int endmonth,
            int enddate,
            int endhour,
            int endminute
    ) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        int sc_id = pref.getInt("sc_id", 0);
        int sc_account = pref.getInt("sc_account", 0);

        String INSERT_SQL = "insert into " + Const.TABLE_NAME + "("
                + "scid, "
                + "sctitle, "
                + "startyear, "
                + "startmonth, "
                + "startdate, "
                + "starthour, "
                + "startminute, "
                + "endyear, "
                + "endmonth, "
                + "enddate, "
                + "endhour, "
                + "endminute"
                + ") "
                + "values ("
                + sc_id + ", "
                + "'" + sctitle + "', "
                + startyear + ", "
                + startmonth + ", "
                + startdate + ", "
                + starthour + ", "
                + startminute + ", "
                + endyear + ", "
                + endmonth + ", "
                + enddate + ", "
                + endhour + ", "
                + endminute + ");";
        db.execSQL(INSERT_SQL);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("sc_id", sc_id + 1);
        editor.putInt("sc_account", sc_account + 1);
        editor.commit();
    }

    public void insertMemo(String contents) {
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        int memo_id = pref.getInt("memo_id", 0);
//        int memo_account = pref.getInt("memo_account", 0); //없어도될듯.

/*
        Calendar judge_cal = Calendar.getInstance();
        int which_year  = judge_cal.get(Calendar.YEAR)-2000;
        int which_month = judge_cal.get(Calendar.MONTH);
        int which_date  = judge_cal.get(Calendar.DATE);
*/
        //String memo_info = ""+memo_id +"번   "+which_year+"/"+which_month+"/"+which_date;
        String INSERT_MEMO_SQL = "insert into " + Const.TABLE_MEMO + "("
                + "memoid, "
                + "contents) "
                + "values ("
                //+""+memo_id +"---"+which_year+"/"+which_month+"/"+which_date + ", "
                +memo_id+", "
                + "'" + contents + "');";
        db.execSQL(INSERT_MEMO_SQL);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("memo_id", memo_id + 1);
//        editor.putInt("memo_account", memo_account + 1);
        editor.commit();
    }

    public void sendRecord(
            String sctitle,
            int startyear,
            int startmonth,
            int startdate,
            int starthour,
            int startminute,
            int endyear,
            int endmonth,
            int enddate,
            int endhour,
            int endminute
    ) {
        String contents = "BEGIN:VCALENDAR\n" +
                "PRODID:-//Daum Calendar Android//iCal4j 1.0//EN\n" +
                "VERSION:2.0\n" +
                "CALSCALE:GREGORIAN\n" +
                "BEGIN:VEVENT\n" +
                "DTSTAMP:20170608T124027Z\n" +
                "SUMMARY:" + sctitle + "\n" +
                "DTSTART;TZID=Asia/Seoul:" +
                String.format("%02d", startyear) + String.format("%02d", startmonth) + String.format("%02d", startdate) +
                "T" +
                String.format("%02d", starthour) + String.format("%02d", startminute) + "00" +
                "\n" +
                "DTEND;TZID=Asia/Seoul:" +
                String.format("%02d", endyear) + String.format("%02d", endmonth) + String.format("%02d", enddate) +
                "T" +
                String.format("%02d", endhour) + String.format("%02d", endminute) + "00" +
                "\n" +
                "END:VEVENT\n" +
                "END:VCALENDAR\n";
        //MakeFileActivity m1 = new MakeFileActivity();
        makefile(contents);
    }

    public void makefile(String s) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Documents/" + "schedule" + ".ics");
            fout.write(s.getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


/////////////////////////////여기서부터 끝까지는 Alarm과 BroadcastD 관련///////////////////////
    public class AlarmHATT {
        private Context context;
        private String input_s;
        private int http_code;
        private int http_intent_number;

        public AlarmHATT(Context context) {
            this.context = context;
        }

        public void Alarm(String string, int code, int intent_number) {

            this.input_s = string;
            this.http_code = code;
//            Log.d("Anal_AlarmHatt code 몇?", Double.toString((double) this.http_code));
            this.http_intent_number = intent_number;

            //알람시간 calendar에 set해주기

//
            Calendar calendar1 = Calendar.getInstance();
            //calendar1.setTimeInMillis(System.currentTimeMillis());

            calendar1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar1.set(Calendar.HOUR_OF_DAY, 7+intent_timing_var[1]);//j
            calendar1.set(Calendar.MINUTE, intent_timing_var[0]);//i
            calendar1.set(Calendar.SECOND, 00);
//
            Calendar calendar2 = Calendar.getInstance();
            //calendar2.setTimeInMillis(System.currentTimeMillis());

            calendar2.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            calendar2.set(Calendar.HOUR_OF_DAY, 6+intent_timing_var[1]);
            calendar2.set(Calendar.MINUTE, intent_timing_var[0]);
            calendar2.set(Calendar.SECOND, 00);
//
            Calendar calendar3 = Calendar.getInstance();
            //calendar3.setTimeInMillis(System.currentTimeMillis());

            calendar3.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendar3.set(Calendar.HOUR_OF_DAY, 20+intent_timing_var[1]);
            calendar3.set(Calendar.MINUTE, intent_timing_var[0]);
            calendar3.set(Calendar.SECOND, 00);
//
            Intent intent = new Intent(getApplicationContext(),BroadcastD.class);
            intent.putExtra("key_http", this.input_s);                     //String:url 주소 전달.
            intent.putExtra("key_code", this.http_code);                   //int값 전달.
            intent.putExtra("key_intent_num", this.http_intent_number); //int값 전달.

            PendingIntent sender1 = PendingIntent.getBroadcast(getApplicationContext(), (this.http_intent_number), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent sender2 = PendingIntent.getBroadcast(getApplicationContext(), (this.http_intent_number + 1), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent sender3 = PendingIntent.getBroadcast(getApplicationContext(), (this.http_intent_number + 2), intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am1 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            AlarmManager am2 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            AlarmManager am3 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            //알람 예약

            am1.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), A_WEEK, sender1);
            am2.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), A_WEEK, sender2);
            am3.setRepeating(AlarmManager.RTC_WAKEUP, calendar3.getTimeInMillis(), (2 * A_WEEK), sender3); //격주로 noti 발생하도록!
        }
    }
///////////////////////////////// noti timing 다르게 하려고 변수값 조정, 필요한 변수 공급//
    public int[] Var_upgrade(int inta, int intb, int intc) {
        int[] save = new int[2];
        save[0] = inta + intc;
        save[1] = intb + (3 * intc);

        http_counting++;
        intent_timing_var[0]+=10;

        if(intent_timing_var[1]==3){
            intent_timing_var[1]=0;
            intent_timing_var[0]=5;
        }

        if(intent_timing_var[0]>=50){
            intent_timing_var[0]=0;
            intent_timing_var[1]+=1;
        }
        return save;
    }
}