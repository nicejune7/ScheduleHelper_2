package com.sbc.snut.schedulehelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CalendarFragment extends Fragment implements MonthLoader.MonthChangeListener, WeekView.EventClickListener, WeekView.EventLongPressListener {
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    private WeekView mWeekView;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // App bar의 Title 수정
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("일정");
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        dbHelper = new DatabaseHelper(getActivity(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();

        // WeekView 구현
        mWeekView = (WeekView) view.findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);

        return view;
    }

    // 표시 할 일정 등록
    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        SharedPreferences pref = getActivity().getSharedPreferences("pref", MODE_PRIVATE);
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
            try{
                c2.moveToNext();

                Calendar startTime = Calendar.getInstance();
                startTime.set(Calendar.HOUR_OF_DAY, c2.getInt(4));
                startTime.set(Calendar.MINUTE, c2.getInt(5));
                startTime.set(Calendar.MONTH, newMonth-1);
                startTime.set(Calendar.DATE, c2.getInt(3));
                startTime.set(Calendar.YEAR, c2.getInt(1));
                Calendar endTime = (Calendar) startTime.clone();
                endTime.add(Calendar.HOUR, 1);
                WeekViewEvent event = new WeekViewEvent(traveler, c2.getString(0), startTime, endTime);
                event.setColor(getResources().getColor(R.color.default_colorPrimary));
                events.add(event);
                c2.close();
            } catch(CursorIndexOutOfBoundsException e) {
                // CursorIndexOutOfBoundsException NULL
            }

        }
        return events;
    }

    @Override
    public void onEventClick(final WeekViewEvent event, RectF eventRect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("\"" + event.getName() + "\"" + "일정을")
                .setItems(R.array.edit_calendar_list_item, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item

                        if (which == 0) {
                            // 기존 일정 확인하기
                            String SELECT_1_SQL = "select ifnull (latitude, -1), ifnull (longitude, -1) from " + Const.TABLE_NAME + " where scid = ?";
                            String[] args3 = {String.valueOf(event.getId())};
                            Cursor c3 = db.rawQuery(SELECT_1_SQL, args3);

                            c3.moveToNext();
                            String latitude_str = c3.getString(0);
                            String longitude_str = c3.getString(1);
                            c3.close();

                            float latitude_float = Float.parseFloat(latitude_str);
                            float longitude_float = Float.parseFloat(longitude_str);

                            if (latitude_float == -1) {
                                // 위치정보 추가하기
                                Intent intent = new Intent(getActivity(), AddLocationDialogActivity.class);
                                long put_sc_id = event.getId();
                                String put_event_name = event.getName();
                                intent.putExtra("sc_id", put_sc_id);
                                intent.putExtra("event_name", put_event_name);
                                startActivity(intent);
                            } else {
                                // 이미 위치정보가 존재
                                Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "일정 " + event.getName() + "는 이미 설정되어 있습니다!", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();

                                Toast.makeText(getActivity(), "latitude : " + latitude_str + "\nlongitude : " + longitude_str, Toast.LENGTH_LONG).show();
                            }
                        }
                        else if (which == 1) {
                            // 일정 삭제하기
                            String DELETE_SQL = "delete from " + Const.TABLE_NAME + " where scid = ?";
                            String[] args1 = {String.valueOf(event.getId())};
                            db.execSQL(DELETE_SQL, args1);
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "일정 " + event.getName() + " 삭제완료!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            FragmentManager manager = getFragmentManager();
                            manager.beginTransaction().replace(R.id.content_main, new CalendarFragment()).commit();

                        }
                        else {
                            Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "지원하지 않는 기능입니다.", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

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
