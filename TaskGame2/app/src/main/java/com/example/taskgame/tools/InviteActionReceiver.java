package com.example.taskgame.tools;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.taskgame.data.repositories.AllianceRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class InviteActionReceiver extends BroadcastReceiver {

    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;

    public InviteActionReceiver(){
        allianceRepository = new AllianceRepository();
        userRepository = new UserRepository();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String inviteId = intent.getStringExtra("inviteId");
        String receiverEmail = intent.getStringExtra("receiverEmail");
        String allianceName = intent.getStringExtra("allianceName");
        String senderEmail = intent.getStringExtra("senderEmail");
        String receiverName = intent.getStringExtra("receiverName");
        int id = intent.getIntExtra("notificationId", -1);

        if (inviteId == null) return;

        if ("ACCEPT_INVITE".equals(action)) {
            allianceRepository.acceptInvite(inviteId, receiverEmail, allianceName);

            FirebaseFirestore.getInstance()
                    .collection("invites")
                    .document(inviteId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (senderEmail != null) {
                            userRepository.sendAcceptedNotification(context, senderEmail, allianceName, receiverName);
                        }
                    });


            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && id!= -1) {
                manager.cancel(id);
            }
        }
        else if ("DECLINE_INVITE".equals(action)) {
            allianceRepository.declineInvite(inviteId, receiverEmail);
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && id!= -1) {
                manager.cancel(id);
            }
        }
    }
}

