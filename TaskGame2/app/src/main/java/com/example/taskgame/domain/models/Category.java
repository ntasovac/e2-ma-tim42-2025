// com.example.taskgame.domain.models.Category
package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Category {
    private long id;
    private String name;
    private int color;

    // REQUIRED by Firestore
    public Category() {}

    public Category(long id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
