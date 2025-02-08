package com.example.taskapplication;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import android.content.ContentValues;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.taskapplication.activities.TaskDetailsActivity;
import com.example.taskapplication.adapters.TaskAdapter;
import com.example.taskapplication.database.AppDatabase;
import com.example.taskapplication.database.TaskEntity;
import com.example.taskapplication.utils.TaskImporter;
import com.example.taskapplication.utils.TaskStatusUpdater;
import com.example.taskapplication.utils.TaskExporter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.taskapplication.providers.TaskContentProvider;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private static final int REQUEST_IMPORT_TASKS = 1;
    private static final int REQUEST_ADD_TASK = 2;
    private static final int REQUEST_EDIT_TASK = 3;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleTaskUpdater();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this);
        recyclerView.setAdapter(adapter);

        database = AppDatabase.getDatabase(this);

        loadTasks();

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, TaskDetailsActivity.class);
            startActivityForResult(intent, REQUEST_ADD_TASK);
        });

        FloatingActionButton fabTestProvider = findViewById(R.id.fabTestProvider);
        fabTestProvider.setOnClickListener(view -> testContentProvider());
    }

    private void scheduleTaskUpdater() {
        PeriodicWorkRequest taskUpdaterRequest = new PeriodicWorkRequest.Builder(
                TaskStatusUpdater.class,
                1,
                TimeUnit.HOURS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TaskStatusUpdater",
                ExistingPeriodicWorkPolicy.REPLACE,
                taskUpdaterRequest
        );
    }


    private void loadTasks() {
        new Thread(() -> {
            List<TaskEntity> tasks = database.taskDao().getNonCompletedTasks();
            runOnUiThread(() -> adapter.setTaskList(tasks));
        }).start();
    }

    @Override
    public void onTaskClick(TaskEntity task) {
        Intent intent = new Intent(MainActivity.this, TaskDetailsActivity.class);
        intent.putExtra("taskId", task.id);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }

    @Override
    public void onTaskDelete(TaskEntity task) {
        new Thread(() -> {
            int rowsDeleted = database.taskDao().deleteTask(task);
            runOnUiThread(() -> {
                showDeleteConfirmation(rowsDeleted);
                loadTasks();
            });
        }).start();
    }

    private void showDeleteConfirmation(int rowsDeleted) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Task Deleted")
                .setMessage(rowsDeleted + " task(s) deleted successfully.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMPORT_TASKS && data != null) {
                Uri uri = data.getData();
                TaskImporter.importTasks(this, uri, database);
                new Handler(Looper.getMainLooper()).postDelayed(this::loadTasks, 1000);
            }
            else if (requestCode == REQUEST_ADD_TASK) {
                new Handler(Looper.getMainLooper()).postDelayed(this::loadTasks, 500);
            }
            else if (requestCode == REQUEST_EDIT_TASK) {
                loadTasks();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_export) {
            TaskExporter.exportTasks(this, database);
            return true;
        }
        if (item.getItemId() == R.id.action_import) {
            selectFile();
            return true;
        }
        if (item.getItemId() == R.id.action_refresh) {
            loadTasks();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Tasks Refreshed")
                    .setMessage("The task list has been updated.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/html");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(Intent.createChooser(intent, "Select Task File"), REQUEST_IMPORT_TASKS);
    }

    private void testContentProvider() {
        new Thread(() -> {
            Log.d("ContentProviderTest", "Starting Content Provider Tests...");
            try {
                ContentValues values = new ContentValues();
                values.put("shortName", "Meeting");
                values.put("description", "Discuss project updates");
                values.put("startTime", "15:00");
                values.put("duration", 1);
                values.put("status", "recorded");
                values.put("location", "Office");

                Uri newTaskUri = getContentResolver().insert(TaskContentProvider.CONTENT_URI, values);
                Log.d("ContentProviderTest", "Inserted Task URI: " + newTaskUri);

                assert newTaskUri != null;
                int taskId = (int) ContentUris.parseId(newTaskUri);
                Log.d("ContentProviderTest", "Extracted Task ID: " + taskId);

                Thread.sleep(10000);

                ContentValues updateValues = new ContentValues();
                updateValues.put("shortName", "Updated");

                Uri taskUri = Uri.withAppendedPath(TaskContentProvider.CONTENT_URI, String.valueOf(taskId));
                int rowsUpdated = getContentResolver().update(taskUri, updateValues, null, null);
                Log.d("ContentProviderTest", "Rows Updated: " + rowsUpdated);

                loadTasks();
                Thread.sleep(10000);

                Uri deleteUri = Uri.withAppendedPath(TaskContentProvider.CONTENT_URI, String.valueOf(taskId));
                int rowsDeleted = getContentResolver().delete(deleteUri, null, null);
                Log.d("ContentProviderTest", "Rows Deleted: " + rowsDeleted);
                loadTasks();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


}
