package com.example.taskgame.view.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.taskgame.data.repositories.UserRepository;
import com.example.taskgame.domain.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class SearchViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData;
    private final MutableLiveData<List<User>> searchResults = new MutableLiveData<>();
    private ListenerRegistration listenerRegistration;

    public SearchViewModel() {
        userRepository = new UserRepository();
        userLiveData = userRepository.getCurrentUser();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            listenerRegistration = db.collection("users")
                    .document(firebaseUser.getUid())
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            Log.w("SearchVM", "Listen failed.", e);
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            User user = snapshot.toObject(User.class);
                            if (user != null) {
                                userLiveData.setValue(user);
                            }
                        }
                    });
        }
    }

    public LiveData<List<User>> getSearchResults() {
        return searchResults;
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }
    public void searchUsers(String query) {
        userRepository.searchUsersByUsername(query, result -> {
            searchResults.setValue(result);
        });
    }

    public void sendFriendRequest(User sender, User recipient, OnCompleteListener<Object> listener){
        userRepository.sendFriendRequest(sender, recipient, listener);
    }
}
