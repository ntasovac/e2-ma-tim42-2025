package com.example.test2.Student2.category;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface CategoryDAO {

    @Upsert
    void upsert(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM category")
    List<Category> getAll();

    @Query("SELECT * FROM Category WHERE colorHex = :color")
    Category getByColor(String color);

    @Query("DELETE FROM Category")
    void deleteAll();

    @Query("SELECT * FROM Category WHERE id = :id LIMIT 1")
    Category getById(int id);

}
