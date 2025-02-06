package com.example.taskapplication.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.taskapplication.database.AppDatabase;
import com.example.taskapplication.database.TaskEntity;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class TaskExporter {

    public static void exportTasks(Context context, AppDatabase database) {
        new Thread(() -> {
            List<TaskEntity> tasks = database.taskDao().getNonCompletedTasks();

            if (tasks.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "No tasks to export.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(directory, "tasks_export.html");

            try (FileWriter writer = new FileWriter(file)) {
                writer.append("<html><body><h1>Task Export</h1><ul>");

                for (TaskEntity task : tasks) {
                    writer.append("<li>")
                            .append("ID: ").append(String.valueOf(task.id)).append("<br>")
                            .append("Name: ").append(task.shortName).append("<br>")
                            .append("Description: ").append(task.description).append("<br>")
                            .append("Status: ").append(task.status).append("<br>")
                            .append("Start Time: ").append(task.startTime).append("<br>")
                            .append("Duration: ").append(String.valueOf(task.duration)).append(" hours<br>")
                            .append("Location: ").append(task.location.isEmpty() ? "N/A" : task.location)
                            .append("</li><br>");
                }

                writer.append("</ul></body></html>");
                writer.flush();

                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Tasks exported to Downloads folder.", Toast.LENGTH_LONG).show()
                );

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
