package com.example.taskgame.domain.models;

import android.os.Parcelable;
import android.os.Parcel;
import androidx.annotation.NonNull;
import com.example.taskgame.domain.enums.Title;
import com.example.taskgame.domain.enums.EquipmentType;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
    private String Username;
    private String Email;
    private String Password;
    private int Avatar;
    private Title Title;
    private int Level;
    private int PowerPoints;
    private int Experience;
    private int Coins;
    private List<Integer> Badges;
    private List<Equipment> Equipment;
    public User(){}
    public User(String username, String email, String password, int avatar){
        this.Username = username;
        this.Email = email;
        this.Password = password;
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
        Password = in.readString();
        Avatar = in.readInt();
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public int getAvatar() {
        return Avatar;
    }

    public void setAvatar(int avatar) {
        Avatar = avatar;
    }

    public Title getTitle() {
        return Title;
    }

    public void setTitle(Title title) {
        Title = title;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public int getPowerPoints() {
        return PowerPoints;
    }

    public void setPowerPoints(int powerPoints) {
        PowerPoints = powerPoints;
    }

    public int getExperience() {
        return Experience;
    }

    public void setExperience(int experience) {
        Experience = experience;
    }

    public int getCoins() {
        return Coins;
    }

    public void setCoins(int coins) {
        Coins = coins;
    }

    public List<Integer> getBadges() {
        return Badges;
    }

    public void setBadges(List<Integer> badges) {
        Badges = badges;
    }

    public List<Equipment> getEquipment() {
        return Equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        Equipment = equipment;
    }

    @Override
    public String toString() {
        return "User{" +
                "Username='" + Username + '\'' +
                ", Email='" + Email + '\'' +
                ", Password='" + Password + '\'' +
                ", Avatar=" + Avatar +
                ", Title=" + Title +
                ", Level=" + Level +
                ", PowerPoints=" + PowerPoints +
                ", Experience=" + Experience +
                ", Coins=" + Coins +
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
        dest.writeString(Password);
        dest.writeInt(Avatar);
        dest.writeString(Title != null ? Title.name() : null); // enum kao string
        dest.writeInt(Level);
        dest.writeInt(PowerPoints);
        dest.writeInt(Experience);
        dest.writeInt(Coins);
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
