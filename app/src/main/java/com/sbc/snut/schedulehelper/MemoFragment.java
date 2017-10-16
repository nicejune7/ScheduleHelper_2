package com.sbc.snut.schedulehelper;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

        //날짜



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
        final SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.fragment_memo_single, cursor, columns, to);

        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor c = (Cursor)mAdapter.getItem(position);
                final String getMemoId = c.getString(1);
                final String getMemoContents = c.getString(2);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("\"" + getMemoContents + "\"" + "메모를 삭제하시겠습니까?")
                        .setItems(R.array.edit_memo_list_item, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                if (which == 0) {
                                    // 메모 삭제하기
                                    String DELETE_SQL = "delete from " + Const.TABLE_MEMO + " where memoid = ?";
                                    String[] args1 = {getMemoId};
                                    db.execSQL(DELETE_SQL, args1);
                                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), "일정 " + getMemoContents + " 삭제완료!", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    FragmentManager manager = getFragmentManager();
                                    manager.beginTransaction().replace(R.id.content_main, new MemoFragment()).commit();
                                }
                                else if (which == 1) {
                                    // DO NOTHING n Close AlertDialog
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
        });

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
