package com.example.taskapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long insertTask(TaskEntity task);

    @Delete
    int deleteTask(TaskEntity task);

    @Update
    void updateTask(TaskEntity task);

    @Query("SELECT * FROM tasks WHERE status != 'completed' " +
            "ORDER BY " +
            "CASE " +
            "    WHEN status = 'expired' THEN 1 " +
            "    WHEN status = 'in-progress' THEN 2 " +
            "    WHEN status = 'recorded' THEN 3 " +
            "END, " +
            "CASE " +
            "    WHEN status = 'in-progress' THEN (startTime + duration) " +
            "    ELSE startTime " +
            "END ASC")
    List<TaskEntity> getNonCompletedTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    TaskEntity findById(int taskId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTasks(List<TaskEntity> tasks);

}
