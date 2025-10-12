package com.example.taskgame.domain.models;

public class SessionManager {
    private static SessionManager instance;
    private String userId;
    private int userLevel;

    private int PP;

    private int totalPP;

    private int XP;

    private int Coins;

    private int equipmentPP;
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

    public void increaseUserXP(int xp) {
        this.XP += xp;
    }

    public void increaseUserLevelandPP() {
        if(this.userLevel <= 1){
            this.userLevel = 2;
            this.PP = 40;
        } else {
            this.userLevel += 1;

            this.PP = (int) (40.0 * Math.pow(7.0 / 4.0, userLevel - 2));
        }
        System.out.println("🔄 Increase Level user " + userId + " -> Level: " + userLevel + ", PP: " + PP);

    }

    public void setUserData(User user){
        this.userLevel = user.getLevel();
        this.userId = user.getId().toString();
        this.XP = user.getXP();
        this.PP = user.getPP();
    }

    public boolean CheckLvLUp(){
        int xp = this.getUserXP();
        int level = this.getUserLevel();
        double totalXPforLevel = 200 * Math.pow(5.0 / 2.0, level - 1);
        //return true;
        return xp > totalXPforLevel;
    }
}
