package com.example.taskgame.view.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.taskgame.R;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.Message;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.view.adapters.ChatAdapter;
import com.example.taskgame.view.viewmodels.AllianceViewModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private UserRepository userRepository;
    private ChatAdapter adapter;
    private CollectionReference messagesRef;
    private String allianceName;
    private AllianceViewModel allianceViewModel;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepository = new UserRepository();

        allianceName = (getArguments() != null)
                ? getArguments().getString("allianceName", "")
                : "";

        RecyclerView recycler = view.findViewById(R.id.chatRecycler);
        adapter = new ChatAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        messagesRef = db.collection("alliances")
                .document(allianceName)
                .collection("messages");

        messagesRef.orderBy("timestamp")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("ChatFragment", "Listen failed.", e);
                        return;
                    }
                    if (snapshots != null) {
                        List<Message> messageList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Message m = doc.toObject(Message.class);
                            if (m != null) {
                                messageList.add(m);
                            }
                        }
                        adapter.updateMessages(messageList);
                        recycler.scrollToPosition(messageList.size() - 1);
                    }
                });

        Button sendButton = view.findViewById(R.id.sendButton);
        EditText messageInput = view.findViewById(R.id.messageInput);
        userRepository.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            sendButton.setOnClickListener(v -> {

                String text = messageInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    userRepository.sendAllianceMessageNotification(allianceName, user.getEmail(), user.getUsername(), text);
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("sender", user.getUsername());
                    msg.put("text", text);
                    msg.put("timestamp", FieldValue.serverTimestamp());
                    messagesRef.add(msg);
                    messageInput.setText("");

                    allianceViewModel = new ViewModelProvider(this).get(AllianceViewModel.class);
                    allianceViewModel.applySpecialMissionAction(
                            SessionManager.getInstance().getUserId(),
                            "dailyMessage",   // or "task", "storePurchase", etc.
                            null            // optional difficulty if needed
                    );
                }
            });
        });
    }
}
