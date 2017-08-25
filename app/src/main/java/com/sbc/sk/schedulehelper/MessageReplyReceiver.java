package com.sbc.sk.schedulehelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

public class MessageReplyReceiver extends BroadcastReceiver{

    private static final String TAG = MessageReplyReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.example.android.messagingservice.ACTION_MESSAGE_REPLY".equals(intent.getAction())) {
            int conversationId = intent.getIntExtra("conversation_id", -1);
            CharSequence reply = getMessageText(intent);

            // AnalysisReply 서비스 실행
            Intent i1 = new Intent(context, AnalysisReply.class);
            i1.putExtra("reply", reply);
            context.startService(i1);

            if (conversationId != -1) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(conversationId);
                Toast.makeText(context, "처리되었습니다!", Toast.LENGTH_SHORT).show();
                Intent i2 = new Intent(context, MessagingService.class);
                context.startService(i2);
            }
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(
                    "extra_remote_reply");
        }
        return null;
    }
}
