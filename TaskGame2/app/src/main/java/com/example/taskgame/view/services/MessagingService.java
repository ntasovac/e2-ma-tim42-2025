package com.example.taskgame.view.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.taskgame.R;
import com.example.taskgame.tools.InviteActionReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String inviteId = remoteMessage.getData().get("inviteId");
        String receiverEmail = remoteMessage.getData().get("receiverEmail");
        String allianceName = remoteMessage.getData().get("allianceName");
        String receiverName = remoteMessage.getData().get("receiverName");
        String senderEmail = remoteMessage.getData().get("senderEmail");

        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getTitle()
                : remoteMessage.getData().get("title");
        String body  = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getBody()
                : remoteMessage.getData().get("body");


        if ("invite".equals(type)) {
            showInviteNotification(receiverEmail, receiverName, title, body,
                    inviteId, allianceName, senderEmail);
        } else if ("inviteAccepted".equals(type)) {
            showSimpleNotification(title, body);
        } else if ("allianceMessage".equals(type)) {
            showSimpleNotification(title, body);
        } else {
            showSimpleNotification(title != null ? title : "Notification",
                    body != null ? body : "");
        }
    }

    private void showSimpleNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "default_channel_id";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void showInviteNotification(String receiverEmail, String receiverName, String title, String message, String inviteId, String allianceName, String senderEmail) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (inviteId == null) {
            inviteId = String.valueOf(System.currentTimeMillis());
        }

        String channelId = "default_channel_id";
        int notificationId = inviteId.hashCode();

        Intent acceptIntent = new Intent(this, InviteActionReceiver.class);
        acceptIntent.setAction("ACCEPT_INVITE");
        acceptIntent.putExtra("inviteId", inviteId);
        acceptIntent.putExtra("receiverEmail", receiverEmail);
        acceptIntent.putExtra("allianceName", allianceName);
        acceptIntent.putExtra("notificationId", notificationId);
        acceptIntent.putExtra("receiverName", receiverName);
        acceptIntent.putExtra("senderEmail", senderEmail);
        PendingIntent acceptPending = PendingIntent.getBroadcast(
                this,
                0,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent declineIntent = new Intent(this, InviteActionReceiver.class);
        declineIntent.setAction("DECLINE_INVITE");
        declineIntent.putExtra("inviteId", inviteId);
        declineIntent.putExtra("receiverEmail", receiverEmail);
        declineIntent.putExtra("notificationId", notificationId);
        PendingIntent declinePending = PendingIntent.getBroadcast(
                this,
                1,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_check, "Accept", acceptPending)
                .addAction(R.drawable.ic_close, "Decline", declinePending);

        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid ->
                            Log.d("FCM", "Token saved to Firestore"))
                    .addOnFailureListener(e ->
                            Log.w("FCM", "Failed to save token", e));
        }
    }
}
