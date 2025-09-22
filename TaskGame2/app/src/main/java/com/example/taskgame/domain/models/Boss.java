package com.example.taskgame.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Boss implements Parcelable {

    private int Level;
    private int CoinReward;
    private int XPThreshold;
    private double bonus;

    public Boss(){
        Level = 1;
        CoinReward = 200;
        XPThreshold = 200;
        bonus = 0;
    }
    protected Boss(Parcel in){
        Level = in.readInt();
        CoinReward = in.readInt();
        XPThreshold = in.readInt();
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public int getCoinReward() {
        return CoinReward;
    }

    public void setCoinReward(int coinReward) {
        CoinReward = coinReward;
    }

    public int getXPThreshold() {
        return XPThreshold;
    }

    public void setXPThreshold(int XPThreshold) {
        this.XPThreshold = XPThreshold;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(Level);
        dest.writeInt(CoinReward);
        dest.writeInt(XPThreshold);
        dest.writeDouble(bonus);
    }

    public static final Creator<Boss> CREATOR = new Creator<Boss>() {
        @Override
        public Boss createFromParcel(Parcel in) {
            return new Boss(in);
        }

        @Override
        public Boss[] newArray(int size) {
            return new Boss[size];
        }
    };
}
