package com.example.taskgame.view.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.data.repositories.AllianceRepository;
import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendViewHolder> {
    public enum Mode { FRIENDS, SEARCH, ALLIANCE }

    private final Mode mode;
    private final List<User> users;
    private OnInviteClickListener inviteListener;
    private OnAddFriendClickListener addFriendListener;
    private final String currentUserEmail;
    private final List<String> currentFriendEmails;
    private final UserRepository userRepository;
    private final AllianceRepository allianceRepository;

    public interface OnInviteClickListener {
        void onInviteClicked(int position, User user);
    }
    public interface OnAddFriendClickListener {
        void onAddFriendClicked(int position, User user);
    }

    public FriendListAdapter(List<User> users, Mode mode, String currentUserEmail,
                             List<String> currentFriendEmails, OnInviteClickListener inviteClickListener, OnAddFriendClickListener addFriendClickListener) {
        this.users = users != null ? users : new ArrayList<>();
        this.mode = mode;
        this.currentUserEmail = currentUserEmail;
        this.currentFriendEmails = currentFriendEmails;
        this.inviteListener = inviteClickListener;
        this.addFriendListener = addFriendClickListener;
        this.userRepository = new UserRepository();
        this.allianceRepository = new AllianceRepository();
    }

    public void setOnInviteClickListener(OnInviteClickListener listener) {
        this.inviteListener = listener;
    }
    public void setOnAddFriendClickListener(OnAddFriendClickListener listener) {
        this.addFriendListener = listener;
    }

    public void updateData(List<User> newUsers) {
        users.clear();
        if (newUsers != null) {
            users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_preview_card, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User user = users.get(position);

        holder.username.setText(user.getUsername());
        holder.profileImage.setImageResource(getAvatarDrawable(user.getAvatar()));

        holder.container.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("friendEmail", user.getEmail());
            Navigation.findNavController(v)
                    .navigate(R.id.fragmentFriendsProfile, bundle);
        });


        if(mode == Mode.FRIENDS){
            userRepository.getCurrentUser().observeForever(currentUser -> {
                allianceRepository.getAllianceByName(currentUser.getAlliance()).observeForever(alliance -> {
                    if(alliance == null){

                        return;
                    }
                    boolean isInAlliance = false;
                    for (User member: alliance.getMembers()) {
                        if(user.getEmail().equals(member.getEmail())) {
                            isInAlliance = true;
                            break;
                        }
                    }
                    if (currentUser.isAllianceOwner() && !isInAlliance) {
                        holder.inviteButton.setVisibility(View.VISIBLE);
                        holder.inviteButton.setOnClickListener(v -> {
                            Log.d("FriendListAdapter", "Invite button clicked for: " + user.getUsername());
                            if (inviteListener != null) {
                                inviteListener.onInviteClicked(holder.getAdapterPosition(), user);
                                holder.inviteButton.setVisibility(View.GONE);
                            } else {
                                Log.e("FriendListAdapter", "inviteListener is NULL!");
                            }
                        });
                    }
                    holder.addFriendButton.setVisibility(View.GONE);
                });
            });
        }else if(mode == Mode.SEARCH){

            boolean isCurrentUser = user.getEmail().equals(currentUserEmail);
            boolean isAlreadyFriend = currentFriendEmails.contains(user.getEmail());
            holder.inviteButton.setVisibility(View.GONE);

            if(isCurrentUser || isAlreadyFriend){
                holder.addFriendButton.setVisibility(View.GONE);
            }else {
                holder.addFriendButton.setVisibility(View.VISIBLE);
                holder.addFriendButton.setOnClickListener(v -> {
                    if (addFriendListener != null)
                        addFriendListener.onAddFriendClicked(holder.getAdapterPosition(), user);
                });
            }
        }else{
            holder.addFriendButton.setVisibility(View.GONE);
            holder.inviteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        final CardView container;
        final ImageView profileImage;
        final TextView username;
        final Button inviteButton;
        final Button addFriendButton;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.friend_preview_card);
            profileImage = itemView.findViewById(R.id.friend_profile_image);
            username = itemView.findViewById(R.id.friend_username);
            inviteButton = itemView.findViewById(R.id.invite_button);
            addFriendButton = itemView.findViewById(R.id.add_friend_button);
        }
    }
    public static int getAvatarDrawable(int avatarNumber) {
        switch (avatarNumber) {
            case 1: return R.drawable.gojo;
            case 2: return R.drawable.eren;
            case 3: return R.drawable.mikasa;
            case 4: return R.drawable.kaneki;
            case 5: return R.drawable.hinata;
            default: return R.drawable.gojo;
        }
    }
}
