package com.sbc.sk.schedulehelper;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Iterator;

public class MessagingService extends Service {
    private NotificationManagerCompat mNotificationManager;
    private final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    public MessagingService() {
        // Must to be NULL
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            // 서비스가 종료 후 다시 시작했을 때.
            //return Service.START_STICKY;
        } else {
            sendMsg(1,1);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    // Creates an intent that will be triggered when a message is marked as read.
    private Intent getMessageReadIntent(int id) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction("com.example.android.messagingservice.ACTION_MESSAGE_READ")
                .putExtra("conversation_id", id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction("com.example.android.messagingservice.ACTION_MESSAGE_REPLY")
                .putExtra("conversation_id", conversationId);
    }

    private void sendNotification(int howManyConversations, int messagesPerConversation) {
        Conversations.Conversation[] conversations = Conversations.getUnreadConversations(
                howManyConversations, messagesPerConversation);
        for (Conversations.Conversation conv : conversations) {
            sendNotificationForConversation(conv);
        }
    }

    private void sendNotificationForConversation(Conversations.Conversation conversation) {
        // A pending Intent for reads
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversation.getConversationId(),
                getMessageReadIntent(conversation.getConversationId()),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build a RemoteInput for receiving voice input in a Car Notification or text input on
        // devices that support text input (like devices on Android N and above).
        RemoteInput remoteInput = new RemoteInput.Builder("extra_remote_reply")
                .setLabel("Reply")
                .build();

        // Building a Pending Intent for the reply action to trigger
        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                conversation.getConversationId(),
                getMessageReplyIntent(conversation.getConversationId()),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build an Android N compatible Remote Input enabled action.
        NotificationCompat.Action actionReplyByRemoteInput = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher_schedulehelper_round, "전송", replyIntent)
                .addRemoteInput(remoteInput)
                .build();

        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConvBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(conversation.getParticipantName())
                        .setLatestTimestamp(conversation.getTimestamp())
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        // Note: Add messages from oldest to newest to the UnreadConversation.Builder
        StringBuilder messageForNotification = new StringBuilder();
        for (Iterator<String> messages = conversation.getMessages().iterator();
             messages.hasNext(); ) {
            String message = messages.next();
            unreadConvBuilder.addMessage(message);
            messageForNotification.append(message);
            if (messages.hasNext()) {
                messageForNotification.append("\n");
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_schedulehelper_round)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getApplicationContext().getResources(), R.mipmap.ic_launcher_schedulehelper_round))
                .setContentText(messageForNotification.toString())
                .setWhen(conversation.getTimestamp())
                .setContentTitle(conversation.getParticipantName())
                .setContentIntent(readPendingIntent)
                .extend(new NotificationCompat.CarExtender()
                        .setUnreadConversation(unreadConvBuilder.build())
                        .setColor(getApplicationContext().getResources()
                                .getColor(R.color.colorPrimary)))
                .addAction(actionReplyByRemoteInput);

        mNotificationManager.notify(conversation.getConversationId(), builder.build());
    }

    /**
     * Handler for incoming messages from clients.
     */
    private static class IncomingHandler extends Handler {
        private final WeakReference<MessagingService> mReference;

        IncomingHandler(MessagingService service) {
            mReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MessagingService service = mReference.get();
            switch (msg.what) {
                case 1:
                    int howManyConversations = msg.arg1 <= 0 ? 1 : msg.arg1;
                    int messagesPerConversation = msg.arg2 <= 0 ? 1 : msg.arg2;
                    if (service != null) {
                        service.sendNotification(howManyConversations, messagesPerConversation);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMsg(int howManyConversations, int messagesPerConversation) {
        Message msg = Message.obtain(null, 1, howManyConversations, messagesPerConversation);
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            Toast.makeText(this, "Error on sendMsg", Toast.LENGTH_SHORT).show();
        }
    }
}
