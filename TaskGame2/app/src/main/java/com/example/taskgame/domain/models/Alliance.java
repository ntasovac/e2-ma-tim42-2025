package com.example.taskgame.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Alliance implements Parcelable {
    private  String Name;
    private User Owner;
    private List<User> Members;

    public Alliance(String name, User owner){
        this.Name = name;
        this.Owner = owner;
        this.Members = new ArrayList<>();
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public User getOwner() {
        return Owner;
    }

    public void setOwner(User owner) {
        Owner = owner;
    }

    public List<User> getMembers() {
        return Members;
    }

    public void setMembers(List<User> members) {
        Members = members;
    }
    protected Alliance(Parcel in) {
        Name = in.readString();
        Owner = in.readParcelable(User.class.getClassLoader());
        Members = new ArrayList<>();
        in.readList(Members, User.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Name);
        dest.writeParcelable(Owner, flags);
        dest.writeList(Members);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Alliance> CREATOR = new Creator<Alliance>() {
        @Override
        public Alliance createFromParcel(Parcel in) {
            return new Alliance(in);
        }

        @Override
        public Alliance[] newArray(int size) {
            return new Alliance[size];
        }
    };
}
