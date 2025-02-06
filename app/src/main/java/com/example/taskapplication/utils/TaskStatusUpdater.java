package com.example.taskapplication.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.taskapplication.database.AppDatabase;
import com.example.taskapplication.database.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskStatusUpdater extends Worker {

    public TaskStatusUpdater(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        List<TaskEntity> tasks = database.taskDao().getNonCompletedTasks();

        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        for (TaskEntity task : tasks) {
            try {
                if (currentTime.compareTo(task.startTime) > 0 && "recorded".equals(task.status)) {
                    task.status = "in-progress";
                } else if (currentTime.compareTo(addHours(task.startTime, task.duration)) > 0 && "in-progress".equals(task.status)) {
                    task.status = "expired";
                }
                database.taskDao().updateTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Result.success();
    }

    private String addHours(String startTime, int hoursToAdd) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = formatter.parse(startTime);
            if (date != null) {
                date.setTime(date.getTime() + hoursToAdd * 3600 * 1000);
                return formatter.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return startTime;
    }
}
