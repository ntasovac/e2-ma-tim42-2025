// src/main/java/com/example/test2/task/Task.java
package com.example.test2.Student2.task;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task")
public class Task {

    public enum Difficulty { VERY_EASY, EASY, HARD, EXTREME }
    public enum Importance { NORMAL, IMPORTANT, VERY_IMPORTANT, SPECIAL }
    public enum RepeatUnit { DAY, WEEK }
    public enum Status { ACTIVE, PAUSED, DONE, CANCELED }

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String description;

    // store FK, not the whole object
    private int categoryId;

    private boolean recurring;
    private Integer repeatInterval;   // nullable
    private RepeatUnit repeatUnit;    // nullable
    private String repeatStart;
    private String repeatEnd;
    private Long executeAt;           // epoch millis (nullable)

    private Difficulty difficulty;
    private Importance importance;

    private Status status = Status.ACTIVE;

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public Integer getRepeatInterval() { return repeatInterval; }
    public void setRepeatInterval(Integer repeatInterval) { this.repeatInterval = repeatInterval; }

    public RepeatUnit getRepeatUnit() { return repeatUnit; }
    public void setRepeatUnit(RepeatUnit repeatUnit) { this.repeatUnit = repeatUnit; }

    public String getRepeatStart() { return repeatStart; }
    public void setRepeatStart(String repeatStart) { this.repeatStart = repeatStart; }

    public String getRepeatEnd() { return repeatEnd; }
    public void setRepeatEnd(String repeatEnd) { this.repeatEnd = repeatEnd; }

    public Long getExecuteAt() { return executeAt; }
    public void setExecuteAt(Long executeAt) { this.executeAt = executeAt; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Importance getImportance() { return importance; }
    public void setImportance(Importance importance) { this.importance = importance; }

    public static int xpForDifficulty(Task.Difficulty d) {
        if (d == null) return 0;
        switch (d) {
            case VERY_EASY: return 1;
            case EASY:      return 3;
            case HARD:      return 7;
            case EXTREME:   return 20;
            default:        return 0;
        }
    }

    public static int xpForImportance(Task.Importance i) {
        if (i == null) return 0;
        switch (i) {
            case NORMAL:         return 1;
            case IMPORTANT:      return 3;
            case VERY_IMPORTANT: return 10;
            case SPECIAL:        return 100;
            default:             return 0;
        }
    }
}
