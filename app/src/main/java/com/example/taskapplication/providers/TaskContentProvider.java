package com.example.taskapplication.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.taskapplication.database.AppDatabase;
import com.example.taskapplication.database.TaskEntity;

public class TaskContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.taskmanager.provider";
    private static final String TABLE_NAME = "tasks";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    private AppDatabase database;

    @Override
    public boolean onCreate() {
        database = AppDatabase.getDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return database.getOpenHelper().getReadableDatabase()
                .query("SELECT * FROM " + TABLE_NAME);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        TaskEntity task = new TaskEntity();
        if (values != null) {
            task.shortName = values.getAsString("shortName");
            task.description = values.getAsString("description");
            task.startTime = values.getAsString("startTime");
            task.duration = values.getAsInteger("duration");
            task.status = values.getAsString("status");
            task.location = values.getAsString("location");
        }
        long id = database.taskDao().insertTask(task);
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        assert selectionArgs != null;
        int taskId = Integer.parseInt(selectionArgs[0]);
        TaskEntity task = new TaskEntity();
        task.id = taskId;
        return database.taskDao().deleteTask(task);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
