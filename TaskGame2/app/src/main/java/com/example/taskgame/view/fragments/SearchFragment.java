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
import android.widget.EditText;
import android.widget.Toast;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.adapters.FriendListAdapter;
import com.example.taskgame.view.viewmodels.SearchViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private FriendListAdapter adapter;
    private EditText editSearchQuery;

    public SearchFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editSearchQuery = view.findViewById(R.id.edit_search_query);
        Button buttonSearch = view.findViewById(R.id.button_search);
        Button buttonAddFriend = view.findViewById(R.id.add_friend_button);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_search_results);

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(), user ->{
            if(user != null) {

                List<String> emails = new ArrayList<>();
                for (User friend: user.getFriends()) {
                    emails.add(friend.getEmail());
                }

                adapter = new FriendListAdapter(new ArrayList<>(), FriendListAdapter.Mode.SEARCH, user.getEmail(), emails, null, (index, item)->{
                    viewModel.sendFriendRequest(user, item, task->{
                        if(task.isSuccessful()){
                            Toast.makeText(getContext(), "Friend request sent!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getContext(), "Error with sending friend request!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                viewModel.getSearchResults().observe(getViewLifecycleOwner(), users -> {
                    adapter.updateData(users);
                    if (users.isEmpty()) {
                        Toast.makeText(getContext(), "No users found", Toast.LENGTH_SHORT).show();
                    }
                });

                buttonSearch.setOnClickListener(v -> {
                    String query = editSearchQuery.getText().toString().trim();
                    if (TextUtils.isEmpty(query)) {
                        Toast.makeText(getContext(), "Enter a username", Toast.LENGTH_SHORT).show();
                    } else {
                        viewModel.searchUsers(query);
                    }
                });

                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        });


    }
}
