package com.example.taskgame.domain.models;

import java.util.Random;

public class Boss {
    private String userId;   // kome pripada boss
    private String name;
    private int level;       // nivo korisnika
    private int bossIndex;   // koji je ovo boss (1, 2, 3...)

    private double totalHP;
    private double hp;
    private int coins;
    private String status;   // "ACTIVE" ili "DEFEATED"
    private int availableAttacks; // koliko napada korisnik još ima (max 5)

    private double attackChance;

    // Firestore zahteva prazan konstruktor
    public Boss() {}

    public Boss(String userId, int userLevel) {
        this.userId = userId;
        this.level = userLevel;
        this.bossIndex = userLevel - 1;  // prvi boss se pojavljuje na levelu 2
        this.name = "Boss " + bossIndex;

        // HP formula
        if (bossIndex == 1) {
            this.hp = 200;
            this.totalHP = 200;
        } else {
            this.hp = 200 * Math.pow(5.0 / 2.0, bossIndex - 1);
            this.totalHP = this.hp;
        }

        // Coins formula
        if (bossIndex == 1) {
            this.coins = 200;
        } else {
            this.coins = (int) Math.round(200 * Math.pow(1.2, bossIndex - 1));
        }

        // Inicijalni status
        this.status = "ACTIVE";

        // Svaka borba počinje sa 5 pokušaja
        this.availableAttacks = 5;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getBossIndex() { return bossIndex; }
    public double getHp() { return hp; }
    public double getTotalHp() { return totalHP; }
    public int getCoins() { return coins; }
    public String getStatus() { return status; }
    public int getAvailableAttacks() { return availableAttacks; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setLevel(int level) { this.level = level; }
    public void setBossIndex(int bossIndex) { this.bossIndex = bossIndex; }
    public void setHp(double hp) { this.hp = hp; }
    public void setCoins(int coins) { this.coins = coins; }
    public void setStatus(String status) { this.status = status; }
    public void setAvailableAttacks(int availableAttacks) { this.availableAttacks = availableAttacks; }

    public double getAttackChance() { return attackChance; }
    public void setAttackChance(double attackChance) { this.attackChance = attackChance; }

    // Utility: oduzimanje HP-a
    public boolean takeDamage(double damage) {
        if (availableAttacks <= 0 || "DEFEATED".equals(this.status)) {
            return false; // nema više pokušaja ili je već mrtav
        }

        availableAttacks--; // svaki pokušaj troši napad

        double roll = new java.util.Random().nextDouble();

        if (roll <= this.attackChance) {
            // ✅ pogodak
            this.hp = Math.max(0, this.hp - damage);
            if (this.hp <= 0) {
                this.status = "DEFEATED";
            }
            return true; // pogodio
        } else {
            // ❌ promašaj
            return false;
        }
    }

    // Utility: da li je boss poražen
    public boolean isDefeated() {
        return "DEFEATED".equals(this.status);
    }

    // Utility: da li korisnik ima još napada
    public boolean canAttack() {
        return availableAttacks > 0 && !"DEFEATED".equals(this.status);
    }
}
