package com.example.taskapplication.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String shortName;
    public String description;
    public String startTime;
    public int duration;
    public String status;
    public String location;
}
