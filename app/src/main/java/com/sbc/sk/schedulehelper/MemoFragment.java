package com.sbc.sk.schedulehelper;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MemoFragment extends Fragment {
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;

    public MemoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // App bar의 Title 수정
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("메모");
        View view = inflater.inflate(R.layout.fragment_memo, container, false);

        //db = MainActivity.returnDB();
        dbHelper = new DatabaseHelper(getActivity(), Const.DATABASE_NAME, null, Const.DATABASE_VERSION);
        db = dbHelper.getWritableDatabase();

        ListView list = (ListView) view.findViewById(R.id.listview_memo);

        Cursor cursor = executeRawQueryParam();
        getActivity().startManagingCursor(cursor);

        String[] columns = new String[] {"memoid", "contents"};
        int[] to = new int[] { R.id.tv_single_1, R.id.tv_single_2};
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_memo_single, cursor, columns, to);

        list.setAdapter(mAdapter);

        return view;
    }

    private Cursor executeRawQueryParam() {
        String SQL = "select _id, memoid, contents from "
                + Const.TABLE_MEMO;

        Cursor c1 = db.rawQuery(SQL, null);

        return c1;
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
