package com.example.taskgame.domain.models;

public class Potion {
    private String userId;
    private String id;
    private String name;
    private String description;
    private String type;  // e.g. "HEALTH", "STAMINA", etc.
    private int value;    // healing value or buff strength

    public Potion() {
        // Firestore needs empty constructor
    }

    public Potion(String userId, String name) {
        this.userId = userId;
        this.id = "potion_" + System.currentTimeMillis();
        this.name = name;
        this.description = "Restores health or grants bonuses during fights.";
        this.type = "HEALTH";
        this.value = 50; // default healing value
    }

    // ✅ Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    @Override
    public String toString() {
        return "Potion{" +
                "userId='" + userId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
