package com.example.taskgame.domain.models;

import android.os.Parcelable;
import android.os.Parcel;

public class User implements Parcelable {
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
