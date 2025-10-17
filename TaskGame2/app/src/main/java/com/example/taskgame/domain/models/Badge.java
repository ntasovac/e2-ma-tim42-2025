package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Badge {

    //private String id;          // Firestore document ID
    //private String userId;      // User who owns the badge
    private String name;        // e.g. "Special Mission Badge"
    private int count;          // How many times earned

    // 🔹 Required empty constructor for Firestore
    public Badge() {}

    public Badge(String userId, String name) {
        //this.userId = userId;
        this.name = name;
        this.count = 1;
    }

    public Badge(String name, int count) {
        //this.id = id;
        //this.userId = userId;
        this.name = name;
        this.count = count;
    }

    public Badge(String name) {
        //this.id = id;
        //this.userId = userId;
        this.name = name;
        //this.count = count;
    }

    // ✅ Getters
   // public String getId() { return id; }
    //public String getUserId() { return userId; }
    public String getName() { return name; }
    public int getCount() { return count; }

    // ✅ Setters
    //public void setId(String id) { this.id = id; }
    //public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setCount(int count) { this.count = count; }

    public void increment() {
        this.count++;
    }
}
