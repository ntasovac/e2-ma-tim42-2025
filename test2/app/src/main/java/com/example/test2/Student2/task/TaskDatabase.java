package com.example.test2.Student2.task;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Task.class}, version = 2)
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDAO taskDAO();
}
