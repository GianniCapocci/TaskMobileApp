package com.example.taskapplication.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.taskapplication.R;
import com.example.taskapplication.database.AppDatabase;
import com.example.taskapplication.database.TaskEntity;

public class TaskDetailsActivity extends AppCompatActivity {

    private EditText editShortName, editDescription, editStartTime, editDuration, editLocation;
    private Button btnSave, btnComplete, btnViewLocation;
    private AppDatabase database;
    private TaskEntity task;
    private boolean isNewTask = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add/Edit Task");
        }

        editShortName = findViewById(R.id.editShortName);
        editDescription = findViewById(R.id.editDescription);
        editStartTime = findViewById(R.id.editStartTime);
        editDuration = findViewById(R.id.editDuration);
        editLocation = findViewById(R.id.editLocation);
        btnSave = findViewById(R.id.btnSave);
        btnComplete = findViewById(R.id.btnComplete);
        btnViewLocation = findViewById(R.id.btnViewLocation);
        database = AppDatabase.getDatabase(this);

        int taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId != -1) {
            isNewTask = false;
            new Thread(() -> {
                task = database.taskDao().findById(taskId);
                runOnUiThread(this::populateTaskDetails);
            }).start();
        } else {
            task = new TaskEntity();
        }

        btnSave.setOnClickListener(view -> saveTask());
        btnComplete.setOnClickListener(view -> completeTask());
        btnViewLocation.setOnClickListener(view -> openLocationInMaps());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateTaskDetails() {
        editShortName.setText(task.shortName);
        editDescription.setText(task.description);
        editStartTime.setText(task.startTime);
        editDuration.setText(String.valueOf(task.duration));
        editLocation.setText(task.location);
    }

private void saveTask() {
    task.shortName = editShortName.getText().toString();
    task.description = editDescription.getText().toString();
    task.startTime = editStartTime.getText().toString();
    task.duration = Integer.parseInt(editDuration.getText().toString());
    task.location = editLocation.getText().toString();
    task.status = isNewTask ? "recorded" : task.status;

    new Thread(() -> {
        if (isNewTask) {
            database.taskDao().insertTask(task);
        } else {
            database.taskDao().updateTask(task);
        }
        runOnUiThread(() -> {
            Toast.makeText(TaskDetailsActivity.this, "Task saved.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }).start();
}

    private void completeTask() {
        task.status = "completed";
        saveTask();
    }

    private void openLocationInMaps() {
        if (!task.location.isEmpty()) {
            Uri locationUri = Uri.parse("geo:0,0?q=" + task.location);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "No location provided.", Toast.LENGTH_SHORT).show();
        }
    }
}
