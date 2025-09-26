package com.example.taskgame.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.data.repositories.AllianceRepository;
import com.example.taskgame.domain.models.Alliance;
import com.example.taskgame.domain.models.User;
import com.example.taskgame.view.adapters.FriendListAdapter;

import java.util.ArrayList;

public class AllianceFragment extends Fragment {

    private AllianceRepository allianceRepository;
    private FriendListAdapter memberAdapter;

    private View ownerCard;
    private RecyclerView membersRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allianceRepository = new AllianceRepository();

        String allianceName = null;
        if (getArguments() != null) {
            allianceName = getArguments().getString("name");
        }

        ownerCard = view.findViewById(R.id.owner_card);
        membersRecycler = view.findViewById(R.id.members_recycler);

        memberAdapter = new FriendListAdapter(
                new ArrayList<>(),
                FriendListAdapter.Mode.ALLIANCE,
                "",
                new ArrayList<>(),
                null,
                null
        );

        membersRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        membersRecycler.setAdapter(memberAdapter);

        if (allianceName != null) {
            allianceRepository.getAllianceByName(allianceName)
                    .observe(getViewLifecycleOwner(), new Observer<Alliance>() {
                        @Override
                        public void onChanged(Alliance alliance) {
                            if (alliance == null) return;

                            User owner = alliance.getOwner();
                            TextView ownerName = ownerCard.findViewById(R.id.friend_username);
                            ImageView ownerImage = ownerCard.findViewById(R.id.friend_profile_image);

                            ownerName.setText(owner.getUsername());
                            ownerImage.setImageResource(
                                    FriendListAdapter.getAvatarDrawable(owner.getAvatar())
                            );

                            memberAdapter.updateData(alliance.getMembers());
                        }
                    });
        }
    }
}
