package com.example.taskapplication.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.taskapplication.database.AppDatabase;
import com.example.taskapplication.database.TaskEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaskImporter {

    public static void importTasks(Context context, Uri fileUri, AppDatabase database) {
        new Thread(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder htmlContent = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    htmlContent.append(line);
                }
                reader.close();

                Pattern pattern = Pattern.compile(
                        "<li>ID: (\\d+)<br>Name: (.*?)<br>Description: (.*?)<br>Status: (.*?)<br>" +
                                "Start Time: (.*?)<br>Duration: (\\d+) hours<br>Location: (.*?)</li>"
                );

                Matcher matcher = pattern.matcher(htmlContent.toString());

                List<TaskEntity> importedTasks = new ArrayList<>();
                while (matcher.find()) {
                    TaskEntity task = new TaskEntity();
                    task.id = Integer.parseInt(matcher.group(1));
                    task.shortName = matcher.group(2);
                    task.description = matcher.group(3);
                    task.status = matcher.group(4);
                    task.startTime = matcher.group(5);
                    task.duration = Integer.parseInt(matcher.group(6));
                    task.location = matcher.group(7).equals("N/A") ? "" : matcher.group(7);

                    importedTasks.add(task);
                }

                database.taskDao().insertTasks(importedTasks);

                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Tasks imported successfully!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Import failed.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
