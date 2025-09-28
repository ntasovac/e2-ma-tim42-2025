package com.example.taskgame.domain.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Task implements Serializable {
    // Firestore-friendly POJO (no-arg ctor + getters/setters)
    private String id;

    private String status; // "ACTIVE", "DONE", "CANCELLED", "PAUSED"
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    private String name;
    private String description;

    private long categoryId;
    private String categoryName;
    private int categoryColor;

    // Scheduling
    private String frequency;   // "ONE_TIME" or "REPEATING"
    private Integer interval;   // null for ONE_TIME, else >=1
    private String unit;        // "DAY" or "WEEK" (for REPEATING)
    private long startDateUtc;  // epoch millis (00:00 local ok too)
    private Long endDateUtc;    // nullable
    private int timeOfDayMin;   // minutes since midnight (0..1439)

    // XP
    private int difficultyXp;
    private int importanceXp;
    private int totalXp;

    private long createdAtUtc;

    public Task() {} // required

    // getters/setters (generate or keep as below)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getCategoryColor() { return categoryColor; }
    public void setCategoryColor(int categoryColor) { this.categoryColor = categoryColor; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getInterval() { return interval; }
    public void setInterval(Integer interval) { this.interval = interval; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public long getStartDateUtc() { return startDateUtc; }
    public void setStartDateUtc(long startDateUtc) { this.startDateUtc = startDateUtc; }

    public Long getEndDateUtc() { return endDateUtc; }
    public void setEndDateUtc(Long endDateUtc) { this.endDateUtc = endDateUtc; }

    public int getTimeOfDayMin() { return timeOfDayMin; }
    public void setTimeOfDayMin(int timeOfDayMin) { this.timeOfDayMin = timeOfDayMin; }

    public int getDifficultyXp() { return difficultyXp; }
    public void setDifficultyXp(int difficultyXp) { this.difficultyXp = difficultyXp; }

    public int getImportanceXp() { return importanceXp; }
    public void setImportanceXp(int importanceXp) { this.importanceXp = importanceXp; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public long getCreatedAtUtc() { return createdAtUtc; }
    public void setCreatedAtUtc(long createdAtUtc) { this.createdAtUtc = createdAtUtc; }
}
