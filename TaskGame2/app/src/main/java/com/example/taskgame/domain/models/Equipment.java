package com.example.taskgame.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.taskgame.domain.enums.EquipmentType;

public class Equipment implements Parcelable {
    private EquipmentType Type;
    private String Name;
    private int Price;
    private String Description;
    private int EffectAmount;
    private int UsageCount;
    private int Image;
    private boolean IsActivated;
    private boolean IsEffectPermanent;

    public Equipment(){}

    public Equipment(EquipmentType type, String name, int price, String description, int effectAmount, int usageCount, int image, boolean isActivated, boolean isEffectPermanent) {
        Type = type;
        Name = name;
        Price = price;
        Description = description;
        EffectAmount = effectAmount;
        UsageCount = usageCount;
        Image = image;
        IsActivated = isActivated;
        IsEffectPermanent = isEffectPermanent;
    }

    public EquipmentType getType() {
        return Type;
    }

    public void setType(EquipmentType type) {
        Type = type;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
        Price = price;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public int getEffectAmount() {
        return EffectAmount;
    }

    public void setEffectAmount(int effectAmount) {
        EffectAmount = effectAmount;
    }

    public int getUsageCount() {
        return UsageCount;
    }

    public void setUsageCount(int usageCount) {
        UsageCount = usageCount;
    }

    public int getImage() {
        return Image;
    }

    public void setImage(int image) {
        Image = image;
    }

    public boolean isActivated() {
        return IsActivated;
    }

    public void setActivated(boolean activated) {
        IsActivated = activated;
    }

    public boolean isEffectPermanent() {
        return IsEffectPermanent;
    }

    public void setEffectPermanent(boolean effectPermanent) {
        IsEffectPermanent = effectPermanent;
    }

    protected Equipment(Parcel in) {
        String typeName = in.readString();
        Type = typeName != null ? EquipmentType.valueOf(typeName) : null;

        Name = in.readString();
        Price = in.readInt();
        Description = in.readString();
        EffectAmount = in.readInt();
        UsageCount = in.readInt();
        Image = in.readInt();
        IsActivated = in.readByte() != 0; // boolean -> byte
        IsEffectPermanent = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Type != null ? Type.name() : null);
        dest.writeString(Name);
        dest.writeInt(Price);
        dest.writeString(Description);
        dest.writeInt(EffectAmount);
        dest.writeInt(UsageCount);
        dest.writeInt(Image);
        dest.writeByte((byte) (IsActivated ? 1 : 0));
        dest.writeByte((byte) (IsEffectPermanent ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Equipment> CREATOR = new Creator<Equipment>() {
        @Override
        public Equipment createFromParcel(Parcel in) {
            return new Equipment(in);
        }

        @Override
        public Equipment[] newArray(int size) {
            return new Equipment[size];
        }
    };
}
