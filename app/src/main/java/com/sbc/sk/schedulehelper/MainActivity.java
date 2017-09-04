package com.sbc.sk.schedulehelper;

import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Messenger mService;
    private boolean mBound;
    private NotificationManagerCompat mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        boolean FirstExcute = true;
        SharedPreferences pref = getSharedPreferences("FirstExcute", MODE_PRIVATE);
        FirstExcute = pref.getBoolean("FirstExcute", true);
        if (FirstExcute == true) {
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putBoolean("FirstExcute", false);
            prefEditor.commit();

            Intent intent = new Intent(this, FirstExcuteActivity.class);
            startActivity(intent);
        } else {
            // 초기 화면을 CalendarFragment로 설정.
            FragmentManager manager = getFragmentManager();
            manager.beginTransaction().replace(R.id.content_main, new CalendarFragment()).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            sendMsg(1,1);
            return true;
        } else if (id == 0) {
            mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher_schedulehelper_round)
                    .setContentText("contenttext\n1\n2\n3\n4\n5\n6")
                    .setContentTitle("오늘의 일정 브리핑");

            mNotificationManager.notify(0, builder.build());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager manager = getFragmentManager();
        if (id == R.id.nav_calendar) {
            manager.beginTransaction().replace(R.id.content_main, new CalendarFragment()).commit();
        } else if (id == R.id.nav_memo) {
            manager.beginTransaction().replace(R.id.content_main, new MemoFragment()).commit();
        } else {
            Snackbar.make(getWindow().getDecorView().getRootView(), "아직 지원되지 않는 기능입니다.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        bindService(new Intent(this, MessagingService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            //getActivity().unbindService(mConnection);
            unbindService(mConnection);
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
                Toast.makeText(this, "Error on sendMsg", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
