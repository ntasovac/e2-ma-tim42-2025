package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SpecialEquipment {
    private String id;              // Firestore document ID
    private String name;            // Name of the equipment
    private String type;            // "WEAPON" or "CLOTHING"
    private int bonusPP;            // Additional Power Points
    private double bonusCoinPercent; // e.g., 0.2 = +20% coins
    private String description;     // Short description

    // Required empty constructor for Firestore
    public SpecialEquipment() {}

    public SpecialEquipment(String id, String name, String type, int bonusPP, double bonusCoinPercent, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.bonusPP = bonusPP;
        this.bonusCoinPercent = bonusCoinPercent;
        this.description = description;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getBonusPP() { return bonusPP; }
    public double getBonusCoinPercent() { return bonusCoinPercent; }
    public String getDescription() { return description; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setBonusPP(int bonusPP) { this.bonusPP = bonusPP; }
    public void setBonusCoinPercent(double bonusCoinPercent) { this.bonusCoinPercent = bonusCoinPercent; }
    public void setDescription(String description) { this.description = description; }
}
