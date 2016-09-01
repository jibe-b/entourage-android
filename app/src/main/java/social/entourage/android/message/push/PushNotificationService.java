package social.entourage.android.message.push;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;

import social.entourage.android.DrawerActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.tape.Events;
import social.entourage.android.map.tour.join.received.TourJoinRequestReceivedActivity;
import social.entourage.android.message.MessageActivity;
import social.entourage.android.tools.BusProvider;

public class PushNotificationService extends IntentService {

    private static final int MIN = 2;
    private static final int MAX = 1000;

    public static final String PUSH_MESSAGE = "social.entourage.android.PUSH_MESSAGE";

    private static final String KEY_SENDER = "sender";
    private static final String KEY_OBJECT = "object";
    private static final String KEY_CONTENT = "content";

    private int notificationId;

    public PushNotificationService() {
        super("PushNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        notificationId = new Random().nextInt(MAX - MIN + 1) + MIN;
        Log.d("notification", "" + notificationId);
        Message message = getMessageFromNotification(intent.getExtras());
        BusProvider.getInstance().post(new Events.OnPushNotificationReceived(message));
        displayPushNotification(message);
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void displayPushNotification(Message message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_map);
        builder.setContentIntent(createMessagePendingIntent(message));
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_entourage));
        builder.setContentTitle(message.getAuthor());
        builder.setContentText(message.getObject());
        builder.setSubText(message.getMessage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            PushNotificationContent content = message.getContent();
            if (content != null && PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(content.getType())) {
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.push_notification);
                String notificationText = getString(R.string.tour_join_request_received_message, message.getAuthor());
                if (content.isEntourageRelated()) {
                    notificationText = getString(R.string.entourage_join_request_received, message.getAuthor());
                }
                remoteViews.setTextViewText(R.id.push_notification_text, notificationText);
                builder.setContent(remoteViews);
            }
        }

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    private PendingIntent createMessagePendingIntent(Message message) {
        Bundle args = new Bundle();
        args.putSerializable(PUSH_MESSAGE, message);
        Intent messageIntent;
        String messageType = "";
        if (message.getContent() != null) {
            messageType = message.getContent().getType();
        }
        if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(messageType)) {
            messageIntent = new Intent(this, TourJoinRequestReceivedActivity.class);
        }
        else if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(messageType) || PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED.equals(messageType)) {
            messageIntent = new Intent(this, DrawerActivity.class);
        }
        else if (PushNotificationContent.TYPE_ENTOURAGE_INVITATION.equals(messageType)) {
            messageIntent = new Intent(this, DrawerActivity.class);
        }
        else {
            messageIntent = new Intent(this, MessageActivity.class);
        }
        if (messageType != null) {
            messageIntent.setAction(messageType);
        }
        messageIntent.putExtras(args);
        return PendingIntent.getActivity(this, notificationId, messageIntent, 0);
    }

    @Nullable
    private Message getMessageFromNotification(Bundle args) {
        Log.d("notification", KEY_SENDER+"= "+args.getString(KEY_SENDER)+"; "+KEY_OBJECT+"= "+args.getString(KEY_OBJECT)+"; "+KEY_CONTENT+"= "+args.getString(KEY_CONTENT));
        return new Message(args.getString(KEY_SENDER), args.getString(KEY_OBJECT), args.getString(KEY_CONTENT));
    }

}
