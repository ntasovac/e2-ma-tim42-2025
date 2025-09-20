package com.example.taskgame.domain.models;

import android.os.Parcelable;
import android.os.Parcel;
import androidx.annotation.NonNull;
import com.example.taskgame.domain.enums.Title;
import com.example.taskgame.domain.enums.EquipmentType;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
    private String Username;
    private String Email;
    private int Avatar;
    private Title Title;
    private int Level;
    private int PowerPoints;
    private int Experience;
    private int Coins;
    private int SuccessfulAttackChance;
    private int AttackCount;
    private List<Integer> Badges;
    private List<Equipment> Equipment;
    public User(){}
    public User(String username, String email, int avatar){
        this.Username = username;
        this.Email = email;
        this.Avatar = avatar;
        this.Title = Title.ROOKIE;
        this.Level = 0;
        this.PowerPoints = 0;
        this.Experience = 0;
        this.Coins = 0;
        this.Badges = new ArrayList<>();
        this.Equipment = new ArrayList<>();
    }
    protected User(Parcel in){
        Username = in.readString();
        Email = in.readString();
        Avatar = in.readInt();
    }

    @PropertyName("username")
    public String getUsername() { return Username; }
    @PropertyName("username")
    public void setUsername(String username) { this.Username = username; }

    @PropertyName("email")
    public String getEmail() { return Email; }
    @PropertyName("email")
    public void setEmail(String email) { this.Email = email; }

    @PropertyName("avatar")
    public int getAvatar() { return Avatar; }
    @PropertyName("avatar")
    public void setAvatar(int avatar) { this.Avatar = avatar; }

    @PropertyName("title")
    public Title getTitle() { return Title; }
    @PropertyName("title")
    public void setTitle(Title title) { this.Title = title; }

    @PropertyName("level")
    public int getLevel() { return Level; }
    @PropertyName("level")
    public void setLevel(int level) { this.Level = level; }

    @PropertyName("powerPoints")
    public int getPowerPoints() { return PowerPoints; }
    @PropertyName("powerPoints")
    public void setPowerPoints(int powerPoints) { this.PowerPoints = powerPoints; }

    @PropertyName("experience")
    public int getExperience() { return Experience; }
    @PropertyName("experience")
    public void setExperience(int experience) { this.Experience = experience; }

    @PropertyName("coins")
    public int getCoins() { return Coins; }
    @PropertyName("coins")
    public void setCoins(int coins) { this.Coins = coins; }

    @PropertyName("successfulAttackChance")
    public int getSuccessfulAttackChance() { return SuccessfulAttackChance; }
    @PropertyName("successfulAttackChance")
    public void setSuccessfulAttackChance(int chance) { this.SuccessfulAttackChance = chance; }

    @PropertyName("attackCount")
    public int getAttackCount() { return AttackCount; }
    @PropertyName("attackCount")
    public void setAttackCount(int count) { this.AttackCount = count; }

    @PropertyName("badges")
    public List<Integer> getBadges() { return Badges; }
    @PropertyName("badges")
    public void setBadges(List<Integer> badges) { this.Badges = badges; }

    @PropertyName("equipment")
    public List<Equipment> getEquipment() { return Equipment; }
    @PropertyName("equipment")
    public void setEquipment(List<Equipment> equipment) { this.Equipment = equipment; }


    @Override
    public String toString() {
        return "User{" +
                "Username='" + Username + '\'' +
                ", Email='" + Email + '\'' +
                ", Avatar=" + Avatar +
                ", Title=" + Title +
                ", Level=" + Level +
                ", PowerPoints=" + PowerPoints +
                ", Experience=" + Experience +
                ", Coins=" + Coins +
                ", SuccessfulAttackChance=" + SuccessfulAttackChance +
                ", AttackCount=" + AttackCount +
                ", Badges=" + (Badges != null ? Badges.toString() : "[]") +
                ", Equipment=" + (Equipment != null ? Equipment.toString() : "[]") +
                '}';
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(Username);
        dest.writeString(Email);
        dest.writeInt(Avatar);
        dest.writeString(Title != null ? Title.name() : null); // enum kao string
        dest.writeInt(Level);
        dest.writeInt(PowerPoints);
        dest.writeInt(Experience);
        dest.writeInt(Coins);
        dest.writeInt(SuccessfulAttackChance);
        dest.writeInt(AttackCount);
        dest.writeList(Badges != null ? Badges : new ArrayList<>());
        dest.writeList(Equipment != null ? Equipment : new ArrayList<>());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
