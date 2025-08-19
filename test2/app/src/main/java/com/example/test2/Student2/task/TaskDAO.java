package com.example.test2.Student2.task;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface TaskDAO {

    @Upsert
    void upsert(Task task);

    @Delete
    void delete(Task task);

    @Query("SELECT * FROM Task")
    List<Task> getAll();

    @Query("SELECT * FROM Task WHERE id = :id LIMIT 1")
    Task getById(int id);

    @Query("DELETE FROM Task")
    void deleteAll();

    @Query("UPDATE task SET status = :status WHERE id = :id")
    void updateStatus(int id, Task.Status status);

    @Query("SELECT * FROM task WHERE recurring = 0 ORDER BY executeAt ASC")
    List<Task> getOneOff();

    @Query("SELECT * FROM task WHERE recurring = 1 ORDER BY COALESCE(repeatStart, executeAt) ASC")
    List<Task> getRecurring();


}
