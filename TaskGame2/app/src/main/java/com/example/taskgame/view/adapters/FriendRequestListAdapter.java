package com.example.taskgame.view.adapters;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskgame.R;
import com.example.taskgame.domain.models.User;

import java.util.List;

public class FriendRequestListAdapter extends  RecyclerView.Adapter<FriendRequestListAdapter.FriendRequestsViewHolder>{
    private final List<User> users;
    private OnAcceptClickListener acceptClickListener;
    private OnDeclineClickListener declineClickListener;

    public interface OnAcceptClickListener {
        void onAcceptClicked(int position, User user);
    }
    public interface OnDeclineClickListener {
        void onDeclineClicked(int position, User user);
    }
    public FriendRequestListAdapter(List<User> users, OnAcceptClickListener acceptClickListener, OnDeclineClickListener declineClickListener){
        this.users = users;
        this.acceptClickListener = acceptClickListener;
        this.declineClickListener = declineClickListener;
    }
    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.acceptClickListener = listener;
    }
    public void setOnAddFriendClickListener(OnDeclineClickListener listener) {
        this.declineClickListener = listener;
    }

    @NonNull
    @Override
    public FriendRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_card, parent, false);
        return new FriendRequestsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestsViewHolder holder, int position) {
        User user = users.get(position);

        holder.username.setText(user.getUsername());
        holder.profileImage.setImageResource(getAvatarDrawable(user.getAvatar()));
        holder.acceptButton.setOnClickListener(view -> {
            Log.d("FriendRequestListAdapter", "Accept button clicked for: " + user.getUsername());
            if(acceptClickListener != null){
                acceptClickListener.onAcceptClicked(holder.getAdapterPosition(), user);
            }else{
                Log.e("FriendRequestListAdapter", "acceptListener is NULL!");
            }
        });
        holder.declineButton.setOnClickListener(view -> {
            Log.d("FriendRequestListAdapter", "Decline button clicked for: " + user.getUsername());
            if(declineClickListener != null){
                declineClickListener.onDeclineClicked(holder.getAdapterPosition(), user);
            }else{
                Log.e("FriendRequestListAdapter", "declineListener is NULL!");
            }
        });


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class FriendRequestsViewHolder extends RecyclerView.ViewHolder {
        final CardView container;
        final ImageView profileImage;
        final TextView username;
        final ImageButton acceptButton;
        final ImageButton declineButton;

        FriendRequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.friend_request_card);
            profileImage = itemView.findViewById(R.id.request_profile_image);
            username = itemView.findViewById(R.id.request_username);
            acceptButton = itemView.findViewById(R.id.button_accept_request);
            declineButton = itemView.findViewById(R.id.button_decline_request);
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
    public void update(int index){
        users.remove(index);
    }

}
