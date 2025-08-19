package com.example.test2.Student2.category;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Category.class}, version = 1)
public abstract class CategoryDatabase extends RoomDatabase {
    public abstract CategoryDAO categoryDAO();
}
