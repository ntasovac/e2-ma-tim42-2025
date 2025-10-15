package com.example.taskgame.domain.models;

import android.os.Parcelable;
import android.os.Parcel;

/*public class User implements Parcelable {
    private Long Id;

    private int PP;

    private int armorPP;

    private int XP;

    private int Level;
    private String Username;
    private String Email;
    private String Password;
    private int Avatar;

    private int Coins;

    public User(){
        Level = 1;
        PP = 0;
        Coins = 0;
    }
    public User(Long id, String username, String email, String password, int avatar){
        this.Id = id;
        this.Username = username;
        this.Email = email;
        this.Password = password;
        this.Avatar = avatar;
        Level = 1;
        PP = 0;
        Coins = 0;
    }
    protected User(Parcel in){
        Id = in.readLong();
        Username = in.readString();
        Email = in.readString();
        Password = in.readString();
        Avatar = in.readInt();
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public int getCoins() {
        return Coins;
    }

    public void setCoins(int coins) {
        Coins = coins;
    }

    public void increaseCoins(int Inccoins) {
        Coins += Inccoins;
    }

    public int getPP() {
        return PP;
    }

    public void setPP(int pp) {
        PP = pp;
    }

    public int getArmorPP() {
        return armorPP;
    }

    public void setArmorPP(int pp) {
        armorPP = pp;
    }

    public int getXP() {
        return XP;
    }

    public void setXP(int xp) {
        XP = xp;
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
    @Override
    public String toString(){
        return "User{" +
                "Username='" + Username + '\'' +
                ", Email='" + Email + '\'' +
                ", Password='" + Password + '\'' +
                ", Avatar='" + Avatar + '\'' +
                '}';
    }
    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeLong(Id);
        dest.writeString(Username);
        dest.writeString(Email);
        dest.writeString(Password);
        dest.writeInt(Avatar);
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
*/

import android.os.Parcelable;
import android.os.Parcel;
import androidx.annotation.NonNull;
import com.example.taskgame.domain.enums.Title;
import com.example.taskgame.domain.enums.EquipmentType;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
    private Long Id;
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
    private int LevelThreshold;
    private List<Integer> Badges;
    private List<Equipment> Equipment;
    private  List<User> Friends;
    private List<User> FriendRequests;
    private String Alliance;
    private boolean IsAllianceOwner;
    private String FCMToken;

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
        this.SuccessfulAttackChance = 0;
        this.AttackCount = 5;
        this.LevelThreshold = 200;
        this.Badges = new ArrayList<>();
        this.Equipment = new ArrayList<>();
        this.Friends = new ArrayList<>();
        this.FriendRequests = new ArrayList<>();
        this.Alliance = "";
        this.IsAllianceOwner = false;
        this.FCMToken = "";

        this.Id = System.currentTimeMillis();
    }
    protected User(Parcel in){
        Username = in.readString();
        Email = in.readString();
        Avatar = in.readInt();

    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
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
    @PropertyName("levelThreshold")
    public int getLevelThreshold() {
        return LevelThreshold;
    }
    @PropertyName("levelThreshold")
    public void setLevelThreshold(int levelThreshold) {
        LevelThreshold = levelThreshold;
    }

    @PropertyName("badges")
    public List<Integer> getBadges() { return Badges; }
    @PropertyName("badges")
    public void setBadges(List<Integer> badges) { this.Badges = badges; }

    @PropertyName("equipment")
    public List<Equipment> getEquipment() { return Equipment; }
    @PropertyName("equipment")
    public void setEquipment(List<Equipment> equipment) { this.Equipment = equipment; }
    @PropertyName("friends")
    public List<User> getFriends() {
        return Friends;
    }
    @PropertyName("friends")
    public void setFriends(List<User> friends) {
        Friends = friends;
    }

    @PropertyName("friendRequests")
    public List<User> getFriendRequests() {
        return FriendRequests;
    }

    @PropertyName("friendRequests")
    public void setFriendRequests(List<User> friendRequests) {
        this.FriendRequests = friendRequests;
    }

    public String getFCMToken() {
        return FCMToken;
    }

    public void setFCMToken(String FCMToken) {
        this.FCMToken = FCMToken;
    }

    public String getAlliance() {
        return Alliance;
    }

    public void setAlliance(String alliance) {
        Alliance = alliance;
    }

    public boolean isAllianceOwner() {
        return IsAllianceOwner;
    }

    public void setAllianceOwner(boolean allianceOwner) {
        IsAllianceOwner = allianceOwner;
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
        dest.writeInt(LevelThreshold);
        dest.writeList(Badges != null ? Badges : new ArrayList<>());
        dest.writeList(Equipment != null ? Equipment : new ArrayList<>());
        dest.writeList(Friends != null ? Friends : new ArrayList<>());
        dest.writeList(FriendRequests != null ? FriendRequests : new ArrayList<>());
        dest.writeString(Alliance);
        dest.writeBoolean(IsAllianceOwner);
        dest.writeString(FCMToken);
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