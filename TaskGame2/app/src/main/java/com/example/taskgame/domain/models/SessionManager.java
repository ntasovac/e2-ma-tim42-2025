package com.example.taskgame.domain.models;

import android.util.Log;

import com.example.taskgame.domain.enums.Title;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static SessionManager instance;
    private String userId;
    private int userLevel;

    private User user;

    private int PP;

    private int aditionalAttacks;

    private int totalPP;

    private int XP;

    private int Coins;

    private List<Equipment> Equipment;

    private int equipmentPP;

    public int getAditionalAttacks() {
        return aditionalAttacks;
    }

    public void setAditionalAttacks(int aditionalAttacks) {
        this.aditionalAttacks = aditionalAttacks;
    }

    private double bonusCoinPercent;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
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

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserLevel(int level) {
        this.userLevel = level;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserXP(int xp) {
        this.XP = xp;
    }

    public int getUserXP() {
        return XP;
    }

    public double getBonusCoinPercent() { return bonusCoinPercent; }
    public void setBonusCoinPercent(double bonusCoinPercent) { this.bonusCoinPercent = bonusCoinPercent; }

    public void setUserPP(int pp) {
        this.PP = pp;
    }

    public int getUserPP() {
        return PP;
    }
    public int getUserEquipmentPP() {
        return equipmentPP;
    }

    public void setUserEquipmentPP(int eqPP) {
         this.equipmentPP = eqPP;
    }

    public void setUserTotalPP(int pp) {
        this.totalPP = pp;
    }

    public int getUserTotalPP() {
        return totalPP;
    }

    public List<Equipment> getEquipment() { return Equipment; }

    public void setEquipment(List<Equipment> equipment) { this.Equipment = equipment; }

    public void increaseUserXP(int xp) {
        this.XP += xp;
    }

    public void increaseUserLevelandPP() {
        if(this.userLevel <= 0){
            this.userLevel = 1;
            this.PP = 40;
            this.user.setTitle(Title.BRONZE);
        } else {
            this.userLevel += 1;

            this.PP = (int) (40.0 * Math.pow(7.0 / 4.0, userLevel - 1));



            Title title = user.getTitle();
            Title currentTitle = user.getTitle();
            Title[] titles = Title.values();
            int nextIndex = currentTitle.ordinal() + 1;
            if(title != Title.CHALLENGER) {
                Title nextTitle = titles[nextIndex];
                this.user.setTitle(nextTitle);

                Log.d("TitleSystem", "🏅 Title upgraded from "
                        + title.name()
                        + " → " + nextTitle.name()
                        + " for user: " + user.getUsername());
                //transaction.update(userRef, "title", nextTitle.name());
            }

        }
        System.out.println("🔄 Increase Level user " + userId + " -> Level: " + userLevel + ", PP: " + PP);

    }

    public void setUserData(User user){
        this.userLevel = user.getLevel();
        this.userId = user.getId().toString();
        this.XP = user.getExperience();
        this.PP = user.getPowerPoints();
        this.Coins = user.getCoins();
        this.Equipment = new ArrayList<>(user.getEquipment());
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean CheckLvLUp(){
        int xp = this.getUserXP();
        int level = this.getUserLevel();

        //user level krece od 0, pa je zato na 0 stepen = 200
        double totalXPforLevel = 200 * Math.pow(5.0 / 2.0, level);
        //return true;
        return xp > totalXPforLevel;
    }

    public double getTotalXPforLEVEL(){
        int level = this.getUserLevel();
        return  200 * Math.pow(5.0 / 2.0, level);
    }
}
