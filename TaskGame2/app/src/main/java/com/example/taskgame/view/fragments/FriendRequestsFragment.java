package com.example.taskgame.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.adapters.FriendListAdapter;
import com.example.taskgame.view.adapters.FriendRequestListAdapter;
import com.example.taskgame.view.viewmodels.FriendRequestsViewModel;
import com.example.taskgame.view.viewmodels.SearchViewModel;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsFragment extends Fragment {

    private FriendRequestsViewModel viewModel;
    private FriendRequestListAdapter adapter;

    public FriendRequestsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton acceptFriendRequest = view.findViewById(R.id.button_accept_request);
        ImageButton declineFriendRequest = view.findViewById(R.id.button_decline_request);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerFriendRequests);

        viewModel = new ViewModelProvider(this).get(FriendRequestsViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(), user ->{
            if(user != null) {

                adapter = new FriendRequestListAdapter(user.getFriendRequests(), (index, item) ->{
                    viewModel.acceptFriendRequest(item, task -> {
                        if(task.isSuccessful()){
                            viewModel.addFriend(item, user, task1 -> {
                                if(task1.isSuccessful()){
                                    Toast.makeText(getContext(), "Sender got recipient", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getContext(), "Sender didn't get recipient", Toast.LENGTH_SHORT).show();
                                }
                            });
                            adapter.update(index);
                            Toast.makeText(getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }, (index, item) ->{
                    viewModel.declineFriendRequest(item, task -> {
                        if(task.isSuccessful()){
                            adapter.update(index);
                            Toast.makeText(getContext(), "Friend request decline", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        });


    }
}