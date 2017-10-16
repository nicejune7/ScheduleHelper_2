package com.sbc.sk.schedulehelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class FirstExcute3Fragment extends Fragment {
    private Messenger mService;
    private boolean mBound;

    public FirstExcute3Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_first_excute3, container, false);

        View view = inflater.inflate(R.layout.fragment_first_excute3, container, false);
        Button btn_firstexcute_finish = (Button) view.findViewById(R.id.btn_firstexcute_finish);
        btn_firstexcute_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg(1,1);
                getActivity().finish();
            }
        });
        return view;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(new Intent(getActivity(), MessagingService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            //getActivity().unbindService(mConnection);
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    private void sendMsg(int howManyConversations, int messagesPerConversation) {
        if (mBound) {
            Message msg = Message.obtain(null, 1,
                    howManyConversations, messagesPerConversation);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Toast.makeText(getActivity(), "Error on sendMsg", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
