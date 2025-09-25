package com.example.taskgame.view.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentFriendsBinding;
import com.example.taskgame.domain.models.Equipment;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.adapters.FriendListAdapter;
import com.example.taskgame.view.adapters.OwnedEquipmentListAdapter;
import com.example.taskgame.view.viewmodels.FriendsViewModel;
import com.example.taskgame.view.viewmodels.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private FragmentFriendsBinding binding;
    private FriendListAdapter adapter;
    private FriendsViewModel viewModel;
    public static ArrayList<User> friends = new ArrayList<>();

    public FriendsFragment() {
    }

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.searchButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_friendsFragment_to_searchFragment);
        });

        binding.allianceCard.findViewById(R.id.alliance_card);
        binding.allianceName.findViewById(R.id.alliance_name);
        binding.allianceNameInput.findViewById(R.id.alliance_name_input);
        binding.createAllianceButton.findViewById(R.id.create_alliance_button);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {

                if(user.getAlliance().equals("")){
                    binding.createAllianceButton.setVisibility(View.VISIBLE);
                    binding.allianceNameInput.setVisibility(View.VISIBLE);
                    binding.createAllianceButton.setOnClickListener(v->{
                        String allianceName = binding.allianceNameInput.getText().toString().trim();
                        if (allianceName.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "Please enter a name for the alliance",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        viewModel.createAlliance(allianceName, task->{
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(), "Alliance created", Toast.LENGTH_SHORT).show();
                                binding.allianceName.setText(task.getResult());
                                binding.createAllianceButton.setVisibility(View.GONE);
                                binding.allianceNameInput.setVisibility(View.GONE);
                                binding.allianceName.setVisibility(View.VISIBLE);
                                binding.allianceCard.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(getContext(), "Error while creating alliance", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }else{
                    binding.allianceName.setText(user.getAlliance());
                    binding.allianceName.setVisibility(View.VISIBLE);
                    binding.allianceCard.setVisibility(View.VISIBLE);
                }

                friends = new ArrayList<>(user.getFriends());
                for (User friend : friends) {
                    Log.d("EquipmentLog", "Name: " + friend.getUsername());
                }


                adapter = new FriendListAdapter(friends, FriendListAdapter.Mode.FRIENDS, null, null,  (index, item) -> {

                    viewModel.inviteFriend(index, item, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Invitation successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }, null);
                RecyclerView recyclerView = binding.recyclerFriends;
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
