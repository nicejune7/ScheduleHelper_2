package com.sbc.snut.schedulehelper;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;


public class ShareActivity extends AppCompatActivity  {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);




        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);

        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ Environment.getExternalStorageDirectory().getPath() + "/Documents/" + "schedule" + ".ics"));


        Intent chooser = Intent.createChooser(intent, "공유");

        startActivity(chooser);

    }

}
