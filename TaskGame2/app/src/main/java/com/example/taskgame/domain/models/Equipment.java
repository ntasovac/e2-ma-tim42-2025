package com.example.taskgame.domain.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.taskgame.domain.enums.EquipmentType;
import com.google.firebase.firestore.PropertyName;

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

    @PropertyName("type")
    public EquipmentType getType() { return Type; }
    @PropertyName("type")
    public void setType(EquipmentType type) { this.Type = type; }

    @PropertyName("name")
    public String getName() { return Name; }
    @PropertyName("name")
    public void setName(String name) { this.Name = name; }

    @PropertyName("price")
    public int getPrice() { return Price; }
    @PropertyName("price")
    public void setPrice(int price) { this.Price = price; }

    @PropertyName("description")
    public String getDescription() { return Description; }
    @PropertyName("description")
    public void setDescription(String description) { this.Description = description; }

    @PropertyName("effectAmount")
    public int getEffectAmount() { return EffectAmount; }
    @PropertyName("effectAmount")
    public void setEffectAmount(int effectAmount) { this.EffectAmount = effectAmount; }

    @PropertyName("usageCount")
    public int getUsageCount() { return UsageCount; }
    @PropertyName("usageCount")
    public void setUsageCount(int usageCount) { this.UsageCount = usageCount; }

    @PropertyName("image")
    public int getImage() { return Image; }
    @PropertyName("image")
    public void setImage(int image) { this.Image = image; }

    @PropertyName("isActivated")
    public boolean isActivated() { return IsActivated; }
    @PropertyName("isActivated")
    public void setActivated(boolean activated) { this.IsActivated = activated; }

    @PropertyName("isEffectPermanent")
    public boolean isEffectPermanent() { return IsEffectPermanent; }
    @PropertyName("isEffectPermanent")
    public void setEffectPermanent(boolean effectPermanent) { this.IsEffectPermanent = effectPermanent; }

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
